package com.example.taskconsumer.Core.Task;

import com.example.taskconsumer.Dao.Factory.DaoFactory;
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

    public OrderTask create(OrderToSend orderToSend, Channel channel, Delivery delivery){
        OrderTask orderTask = new OrderTask();

        orderTask.setDaoFactory(daoFactory);
        orderTask.setOrder(orderToSend.getOrder());
        orderTask.setTraderSideUsername(orderToSend.getTraderSideUsername());
        orderTask.setBrokerId(orderToSend.getBrokerId());

        /**
         * Service
         */
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
