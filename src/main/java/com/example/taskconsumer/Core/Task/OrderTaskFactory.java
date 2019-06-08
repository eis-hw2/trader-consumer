package com.example.taskconsumer.Core.Task;

import com.example.taskconsumer.Dao.Factory.DaoFactory;
import com.example.taskconsumer.Dao.Repo.OrderToSendDao;
import com.example.taskconsumer.Domain.Entity.Order;
import com.example.taskconsumer.Domain.Entity.OrderToSend;
import com.example.taskconsumer.Service.BrokerService;
import com.example.taskconsumer.Service.BrokerSideUserService;
import com.example.taskconsumer.Service.RedisService;
import com.example.taskconsumer.Service.TraderSideUserService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderTaskFactory {
    @Autowired
    private DaoFactory daoFactory;
    @Autowired
    private BrokerService brokerService;
    @Autowired
    private BrokerSideUserService brokerSideUserService;
    @Autowired
    private TraderSideUserService traderSideUserService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OrderToSendDao orderToSendDao;

    public OrderTask create(OrderToSend orderToSend, Channel channel, Delivery delivery){
        OrderTask orderTask = new OrderTask();

        orderTask.setOts(orderToSend);

        orderTask.setDaoFactory(daoFactory);
        /**
         * Important !
         * OtsId 由 trader-gateway 中的 sender 产生
         * 之后需要根据 OtsId 查询任务是否被消费过，防止重复消费
         */
        Order orderWithOtsId = orderToSend.getOrder();
        orderWithOtsId.setClientId(orderToSend.getId());

        orderTask.setOrder(orderWithOtsId);
        orderTask.setTraderSideUsername(orderToSend.getTraderSideUsername());
        orderTask.setBrokerId(orderToSend.getBrokerId());

        /**
         * Service and Dao
         */
        orderTask.setOrderToSendDao(orderToSendDao);
        orderTask.setBrokerService(brokerService);
        orderTask.setBrokerSideUserService(brokerSideUserService);
        orderTask.setTraderSideUserService(traderSideUserService);
        orderTask.setRedisService(redisService);

        /**
         * Distributed lock
         */
        orderTask.setId(orderToSend.getId());

        /**
         * For ACK
         */
        orderTask.setChannel(channel);
        orderTask.setDelivery(delivery);
        return orderTask;
    }
}
