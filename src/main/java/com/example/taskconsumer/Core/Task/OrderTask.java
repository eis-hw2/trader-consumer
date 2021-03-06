package com.example.taskconsumer.Core.Task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.taskconsumer.Dao.Factory.DaoFactory;
import com.example.taskconsumer.Dao.Repo.AbstractOrderDao;
import com.example.taskconsumer.Dao.Repo.OrderToSendDao;
import com.example.taskconsumer.Dao.Repo.SecuredDao;
import com.example.taskconsumer.Domain.Entity.*;
import com.example.taskconsumer.Service.BrokerService;
import com.example.taskconsumer.Service.BrokerSideUserService;
import com.example.taskconsumer.Service.RedisService;
import com.example.taskconsumer.Service.TraderSideUserService;
import com.example.taskconsumer.Util.DateUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.Calendar;

public class OrderTask implements Runnable {
    private OrderToSend ots;

    private String id; // the same as otsId & the same as order's clientId
    private BrokerSideUserService brokerSideUserService;
    private TraderSideUserService traderSideUserService;
    private BrokerService brokerService;
    private RedisService redisService;
    private OrderToSendDao orderToSendDao;
    private Calendar timeToSend;

    private String traderSideUsername;
    private DaoFactory daoFactory;
    private Integer brokerId;
    private Order order;

    private Channel channel;
    private Delivery delivery;

    private static final Logger logger = LoggerFactory.getLogger("OrderTask");

    @Override
    public void run() {
        logger.info("[OrderTask.execute."+getId()+"] Time: " + DateUtil.calendarToString(Calendar.getInstance(), DateUtil.datetimeFormat));
        String id = getId();
        /**
         * id 在 redis 中存在
         * => 其他 worker 正在执行该任务
         */
        if (redisService.exists(id)){
            logger.info("[OrderTask.execute."+getId()+"] Task is running by other consumer");
            sendACK();
            return;
        }


        /**
         * 获取分布式锁失败
         * => 其他 worker 竞争到锁
         */
        boolean getLock = redisService.setIfAbsent(id, 1, 10000L);
        if (!getLock) {
            logger.info("[OrderTask.execute."+getId()+"] Task is running by other consumer");
            sendACK();
            return;
        }

        /**
         *  去 broker 检查该任务是否被执行过
         */
        Broker broker = brokerService.findById(brokerId);

        TraderSideUser traderSideUser = traderSideUserService.findByUsername(traderSideUsername);
        BrokerSideUser brokerSideUser = traderSideUser.getBrokerSideUser(brokerId);

        String token = brokerSideUserService.getToken(brokerSideUser.getUsername(), broker.getId());
        AbstractOrderDao orderDao = (AbstractOrderDao)daoFactory.createWithToken(broker, order.getType(), token);

        /**
         * 由于上游代码这里有bug
         * 暂时用 try catch 解决当 order 不存在查询爆 404 的问题
         *
         * 本应该是判断 order 为 null 时执行
         * 现在是捕捉到 404 null error 时执行
         */
        try {
            Order order = orderDao.findByClientId(id);
            if (order != null){
                logger.info("[OrderTask.execute."+id+"] Task has been consumed");
                sendACK();
                return;
            }
        }
        catch(HttpClientErrorException e){
            logger.error(e.getMessage());
            if (e.getMessage().equals("404 null")) {
                logger.info("[OrderTask.execute." + getId() + "] TraderSideUser: " + traderSideUsername);
                logger.info("[OrderTask.execute." + getId() + "] BrokerSideUser: " + brokerSideUser.getUsername());
                logger.info("[OrderTask.execute." + getId() + "] Token: " + token);
                logger.info("[OrderTask.execute." + getId() + "] Order: " + JSON.toJSONString(order));
                Order createdOrder = orderDao.create(order);

                ots.setOrder(createdOrder);
                ots.setStatus(OrderToSend.CREATED);
                ots.setBrokerOrderId(createdOrder.getId());
                orderToSendDao.save(ots);

                sendACK();
            }
            else{
                logger.info("[OrderTask.execute."+id+"] Task has been consumed");
                sendACK();
                return;
            }
        }
    }

    public void sendACK(){
        try{
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            logger.info("[OrderTask.execute."+getId()+"] ACK is sent");
        }
        catch (IOException e){
            logger.info("[OrderTask.execute."+getId()+"] Error: RabbitMQ ACK lost");
            e.printStackTrace();
        }
    }

    public OrderTask(){}


    public BrokerSideUserService getBrokerSideUserService() {
        return brokerSideUserService;
    }

    public void setBrokerSideUserService(BrokerSideUserService brokerSideUserService) {
        this.brokerSideUserService = brokerSideUserService;
    }

    public DaoFactory getDaoFactory() {
        return daoFactory;
    }

    public void setDaoFactory(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getTraderSideUsername() {
        return traderSideUsername;
    }

    public void setTraderSideUsername(String traderSideUsername) {
        this.traderSideUsername = traderSideUsername;
    }

    public TraderSideUserService getTraderSideUserService() {
        return traderSideUserService;
    }

    public void setTraderSideUserService(TraderSideUserService traderSideUserService) {
        this.traderSideUserService = traderSideUserService;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public BrokerService getBrokerService() {
        return brokerService;
    }

    public void setBrokerService(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RedisService getRedisService() {
        return redisService;
    }

    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    public OrderToSend getOts() {
        return ots;
    }

    public void setOts(OrderToSend ots) {
        this.ots = ots;
    }

    public OrderToSendDao getOrderToSendDao() {
        return orderToSendDao;
    }

    public void setOrderToSendDao(OrderToSendDao orderToSendDao) {
        this.orderToSendDao = orderToSendDao;
    }

    public Calendar getTimeToSend() {
        return timeToSend;
    }

    public void setTimeToSend(Calendar timeToSend) {
        this.timeToSend = timeToSend;
    }
}
