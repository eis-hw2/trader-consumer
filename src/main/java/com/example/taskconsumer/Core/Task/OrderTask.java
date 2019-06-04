package com.example.taskconsumer.Core.Task;

import com.alibaba.fastjson.JSON;
import com.example.taskconsumer.Dao.Factory.DaoFactory;
import com.example.taskconsumer.Dao.Repo.SecuredDao;
import com.example.taskconsumer.Domain.Entity.Broker;
import com.example.taskconsumer.Domain.Entity.BrokerSideUser;
import com.example.taskconsumer.Domain.Entity.Order;
import com.example.taskconsumer.Domain.Entity.TraderSideUser;
import com.example.taskconsumer.Service.BrokerService;
import com.example.taskconsumer.Service.BrokerSideUserService;
import com.example.taskconsumer.Service.RedisService;
import com.example.taskconsumer.Service.TraderSideUserService;
import com.example.taskconsumer.Util.DateUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;

public class OrderTask implements Runnable {
    private String id;
    private BrokerSideUserService brokerSideUserService;
    private TraderSideUserService traderSideUserService;
    private BrokerService brokerService;
    private RedisService redisService;

    private String traderSideUsername;
    private DaoFactory daoFactory;
    private Integer brokerId;
    private Order order;

    private Channel channel;
    private Delivery delivery;

    private static final Logger logger = LoggerFactory.getLogger("OrderTask");

    @Override
    public void run() {
        logger.info("[OrderTask.execute."+order.hashCode()+"] Time: " + DateUtil.calendarToString(Calendar.getInstance(), DateUtil.datetimeFormat));
        String id = getId();
        /**
         * id 在 redis 中存在
         * => 其他 work 正在执行该任务
         */
        if (redisService.exists(id))
            return;

        /**
         * 获取分布式锁失败
         * => 其他 worker 竞争到锁
         */
        boolean getLock = redisService.setIfAbsent(id, 1, 10000L);
        if (!getLock)
            return;

        TraderSideUser traderSideUser = traderSideUserService.findByUsername(traderSideUsername);
        BrokerSideUser brokerSideUser = traderSideUser.getBrokerSideUser(brokerId);
        Broker broker = brokerService.findById(brokerId);

        String token = brokerSideUserService.getToken(brokerSideUser.getUsername(), broker.getId());

        SecuredDao orderDao = daoFactory.createWithToken(broker, order.getType(), token);

        logger.info("[OrderTask.execute."+order.hashCode()+"] TraderSideUser: " + traderSideUsername);
        logger.info("[OrderTask.execute."+order.hashCode()+"] BrokerSideUser: " + brokerSideUser.getUsername());
        logger.info("[OrderTask.execute."+order.hashCode()+"] Token: " + token);
        logger.info("[OrderTask.execute."+order.hashCode()+"] Order: " + JSON.toJSONString(order));
        orderDao.create(order);
        try{
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
        catch (IOException e){
            logger.info("RabbitMQ ACK lost");
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
}
