package com.example.taskconsumer.Service.Impl;

import com.example.taskconsumer.Core.Scheduler.OrderScheduler;
import com.example.taskconsumer.Core.Task.OrderTaskFactory;
import com.example.taskconsumer.Core.MessageQueue.TaskConsumer;
import com.example.taskconsumer.Service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class SchedulerServiceImpl {
    @Autowired
    private OrderScheduler orderScheduler;
    @Autowired
    private OrderTaskFactory orderTaskFactory;
    @Autowired
    private RedisService redisService;

    @PostConstruct
    void init(){
        new Thread( () -> {
            try{
                TaskConsumer.listenToRabbitMQ(orderScheduler, orderTaskFactory, redisService);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}
