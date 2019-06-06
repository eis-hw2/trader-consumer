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
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CancelTaskConsumer implements TaskConsumer{
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

            switch (type){
                case TaskConsumerCommand.CANCEL:
                    /**
                     * Cancel a group of order
                     */
                    String groupId = jsonMessge.getString("body");
                    logger.info("[CreateTaskConsumer.consume.cancel."+groupId+"] GroupId: " + groupId);
                    List<String> ids = orderScheduler.cancel(groupId);
                    List<OrderToSend> group = orderToSendDao.findByGroupId(groupId);

                    logger.info("[CreateTaskConsumer.consume.cancel."+groupId+"] All OtsId:");
                    group.stream().forEach(e -> {
                        logger.info(e.getId());
                    });

                    List<OrderToSend> filtered = group.stream()
                            .filter(e -> ids.contains(e.getId()))
                            .collect(Collectors.toList());

                    logger.info("[CreateTaskConsumer.consume.cancel."+groupId+"] Cancelled OtsId:");
                    filtered.stream().forEach(e -> {
                        logger.info(e.getId());
                        e.setStatus(OrderToSend.CANCELLED);
                    });
                    orderToSendDao.saveAll(filtered);
                    break;
                default:
                    throw new InvalidTaskConsumerCommand("Invalid Command in Cancel:"+type);

            }
        };
    }
}
