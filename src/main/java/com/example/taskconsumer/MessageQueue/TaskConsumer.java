package com.example.taskconsumer.MessageQueue;

import com.alibaba.fastjson.JSON;
import com.example.taskconsumer.Core.Scheduler.OrderScheduler;
import com.example.taskconsumer.Core.Task.OrderTaskFactory;
import com.example.taskconsumer.Domain.Entity.OrderToSend;
import com.example.taskconsumer.Service.RedisService;
import com.example.taskconsumer.Service.TraderSideUserService;
import com.example.taskconsumer.Util.DateUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Calendar;

public class TaskConsumer {
    private final static String QUEUE_NAME = "FutureTask";
    private final static String MQ_HOST = "47.106.8.44";
    private final static Logger logger = LoggerFactory.getLogger("TaskConsumer");
    private final static boolean AUTO_ACK = false;

    public static void listenToRabbitMQ(OrderScheduler scheduler,
                                        OrderTaskFactory orderTaskFactory,
                                        RedisService redisService) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(MQ_HOST);
        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            String message = new String(delivery.getBody(), "UTF-8");
            logger.info("[TaskConsumer.main] Message: " + message);

            OrderToSend orderToSend = JSON.parseObject(message, OrderToSend.class);
            if (orderToSend.getOrder().getTotalCount() == 0)
                return;
            try{
                Calendar calendar = DateUtil.stringToCalendar(orderToSend.getDatetime(), DateUtil.datetimeFormat);
                scheduler.schedule(orderTaskFactory.create(orderToSend, channel, delivery), calendar);
            }
            catch(ParseException e){
                e.printStackTrace();
                return;
            }
        };
        channel.basicConsume(QUEUE_NAME, AUTO_ACK, deliverCallback, consumerTag -> { });
    }

    public static void main(String[] argv) throws Exception {

    }
}
