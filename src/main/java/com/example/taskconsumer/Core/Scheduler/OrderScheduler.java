package com.example.taskconsumer.Core.Scheduler;

import com.alibaba.fastjson.JSON;
import com.example.taskconsumer.Core.Task.OrderTask;
import com.example.taskconsumer.Util.DateUtil;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

@Component
public class OrderScheduler {
    class TaskFuturePair{
        OrderTask orderTask;
        ScheduledFuture future;

        TaskFuturePair(OrderTask o, ScheduledFuture f){
            orderTask = o;
            future = f;
        }
    }

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private static Logger logger  = LoggerFactory.getLogger("OrderScheduler");

    private Random random = new Random(Calendar.getInstance().getTimeInMillis());
    private Calendar randomBias(Calendar calendar){
        calendar.add(Calendar.MINUTE, random.nextInt(10)-5);
        calendar.add(Calendar.SECOND, random.nextInt(30)-15);
        return calendar;
    }
    /**
     * Key: GroupId, OrderToSendId
     * Value: ScheduledFuture, channel pair
     */
    private ConcurrentHashMap<String,ConcurrentHashMap<String, TaskFuturePair>> futures = new ConcurrentHashMap<>();

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    public ScheduledFuture schedule(OrderTask orderTask, Calendar calendar){
        logger.info("[OrderScheduler.schedule."+orderTask.getId()+"] Time: " + DateUtil.calendarToString(calendar, DateUtil.datetimeFormat));
        logger.info("[OrderScheduler.schedule."+orderTask.getId()+"] Order: " + JSON.toJSONString(orderTask.getOrder()));
        calendar = randomBias(calendar);
        ScheduledFuture future = schedule(orderTask, calendar.getTime());

        String groupId = orderTask.getOts().getGroupId();
        String otsId = orderTask.getId();
        ConcurrentHashMap<String, TaskFuturePair> group = futures.get(groupId);
        if (group == null)
            group = new ConcurrentHashMap<>();
        TaskFuturePair tfp = new TaskFuturePair(orderTask, future);
        group.put(otsId, tfp);

        futures.put(groupId, group);
        return future;
    }

    public ScheduledFuture schedule(Runnable task, Date datetime){
        return threadPoolTaskScheduler.schedule(task, datetime);
    }

    public List<String> cancel(String groupId){
        List<String> cancelledId = new ArrayList<>();

        ConcurrentHashMap<String, TaskFuturePair> group = futures.get(groupId);
        if (group == null)
            return cancelledId;
        group.entrySet().stream().forEach(e -> {
            TaskFuturePair tfp = e.getValue();
            if (tfp.future.isDone())
                return;
            if (tfp.future.cancel(false)) {
                logger.info("[OrderScheduler.cancel] Cancelled Future:" + e.getKey());
                cancelledId.add(e.getKey());
                /**
                 * Important!!
                 */
                tfp.orderTask.sendACK();
            }
        });
        return cancelledId;
    }
}
