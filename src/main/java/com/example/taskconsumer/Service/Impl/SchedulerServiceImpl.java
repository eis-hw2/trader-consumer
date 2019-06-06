package com.example.taskconsumer.Service.Impl;

import com.example.taskconsumer.Core.MessageQueue.MQListener;
import com.example.taskconsumer.Core.MessageQueue.OrderTaskConsumer.CancelTaskConsumer;
import com.example.taskconsumer.Core.MessageQueue.OrderTaskConsumer.CreateTaskConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class SchedulerServiceImpl {
    @Autowired
    CancelTaskConsumer cancelTaskConsumer;
    @Autowired
    CreateTaskConsumer createTaskConsumer;
    @Autowired
    MQListener mqListener;

    @PostConstruct
    void init(){
        new Thread( () -> {
            try{
                mqListener.listenCreate(createTaskConsumer);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();

        new Thread( () -> {
            try{
                mqListener.listenCancel(cancelTaskConsumer);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}
