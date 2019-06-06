package com.example.taskconsumer.Core.Scheduler;

import com.alibaba.fastjson.JSON;
import com.example.taskconsumer.Core.Task.OrderTask;
import com.example.taskconsumer.Util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class OrderScheduler {
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private static Logger logger  = LoggerFactory.getLogger("OrderScheduler");

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    public ScheduledFuture schedule(OrderTask orderTask, Calendar calendar){
        logger.info("[OrderScheduler.schedule] Time: " + DateUtil.calendarToString(calendar, DateUtil.datetimeFormat));
        logger.info("[OrderScheduler.schedule] Order: " + JSON.toJSONString(orderTask.getOrder()));
        return schedule(orderTask, calendar.getTime());
    }

    public ScheduledFuture schedule(Runnable task, Date datetime){
        return threadPoolTaskScheduler.schedule(task, datetime);
    }
}
