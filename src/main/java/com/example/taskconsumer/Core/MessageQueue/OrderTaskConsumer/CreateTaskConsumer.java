package com.example.taskconsumer.Core.MessageQueue.OrderTaskConsumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.taskconsumer.Core.MessageQueue.TaskConsumer;
import com.example.taskconsumer.Core.Scheduler.OrderScheduler;
import com.example.taskconsumer.Core.Task.OrderTaskFactory;
import com.example.taskconsumer.Dao.Repo.OrderToSendDao;
import com.example.taskconsumer.Domain.Entity.OrderToSend;
import com.example.taskconsumer.Domain.Entity.Util.TaskConsumerCommand;
import com.example.taskconsumer.Exception.InvalidTaskConsumerCommand;
import com.example.taskconsumer.Util.DateUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Calendar;

@Component
public class CreateTaskConsumer implements TaskConsumer{
    private final static Logger logger = LoggerFactory.getLogger("TaskConsumer");
    @Autowired
    OrderToSendDao orderToSendDao;
    @Autowired
    OrderScheduler scheduler;
    @Autowired
    OrderTaskFactory orderTaskFactory;
    @Autowired
    OrderScheduler orderScheduler;

    public DeliverCallback consume(Channel channel){
        return (consumerTag, delivery) -> {

            String message = new String(delivery.getBody(), "UTF-8");
            logger.info("[CreateTaskConsumer.consume] Raw Message: " + message);

            JSONObject jsonMessge = JSON.parseObject(message);
            String type = jsonMessge.getString("type");
            if (type == null)
                throw new InvalidTaskConsumerCommand("Invalid Command in Create: null");

            switch (type){
                case TaskConsumerCommand.CREATE:
                    /**
                     * Create an order
                     */

                    OrderToSend ots = jsonMessge.getObject("body", OrderToSend.class);
                    ots.setStatus(OrderToSend.SCHEDULED);
                    orderToSendDao.save(ots);

                    logger.info("[CreateTaskConsumer.consume.create."+ots.getId()+"] " + JSON.toJSONString(ots));

                    if (ots.getOrder().getTotalCount() == 0) {
                        logger.info("[CreateTaskConsumer.consume.create."+ots.getId()+"] TotalCount = 0");
                        return;
                    }
                    try{
                        Calendar calendar = DateUtil.stringToCalendar(ots.getDatetime(), DateUtil.datetimeFormat);
                        scheduler.schedule(orderTaskFactory.create(ots, channel, delivery), calendar);
                        logger.info("[CreateTaskConsumer.consume.create."+ots.getId()+"] Success");
                    }
                    catch(ParseException e){
                        logger.info("[CreateTaskConsumer.consume.create."+ots.getId()+"] Error");
                        e.printStackTrace();
                        return;
                    }
                    break;
                default:
                    throw new InvalidTaskConsumerCommand("Invalid Command in Create:"+type);

            }
        };
    }
}
