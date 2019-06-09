package com.example.taskconsumer.Core.Scheduler;

import com.alibaba.fastjson.JSON;
import com.example.taskconsumer.Core.Task.OrderTask;
import com.example.taskconsumer.Dao.Factory.DaoFactory;
import com.example.taskconsumer.Dao.Repo.OrderDao.CancelOrderDao;
import com.example.taskconsumer.Dao.Repo.OrderToSendDao;
import com.example.taskconsumer.Domain.Entity.Broker;
import com.example.taskconsumer.Domain.Entity.Order;
import com.example.taskconsumer.Domain.Entity.OrderToSend;
import com.example.taskconsumer.Service.BrokerService;
import com.example.taskconsumer.Service.BrokerSideUserService;
import com.example.taskconsumer.Util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    @Autowired
    private DaoFactory daoFactory;
    @Autowired
    private BrokerSideUserService brokerSideUserService;
    @Autowired
    private BrokerService brokerService;
    @Autowired
    private OrderToSendDao orderToSendDao;

    private static Logger logger  = LoggerFactory.getLogger("OrderScheduler");

    private Random random = new Random(Calendar.getInstance().getTimeInMillis());
    private Calendar randomBias(Calendar calendar){
        //calendar.add(Calendar.MINUTE, random.nextInt(10)-5);
        calendar.add(Calendar.SECOND, random.nextInt(30)-15);
        return calendar;
    }
    /**
     * Key: GroupId, OrderToSendId
     * Value: ScheduledFuture, channel pair
     */
    private ConcurrentHashMap<String,ConcurrentHashMap<String, TaskFuturePair>> groups = new ConcurrentHashMap<>();

    /**
     * 回收 groups 中的“垃圾”
     * 若已经创建时间距现在超过一天，则视为垃圾
     */
    @Scheduled(cron = "0 0 0 * * ?")
    private void garbageCollection(){
        logger.info("[OrderScheduler.garbageCollection] GC Start");
        List<String> groupsToRemove = new ArrayList<>();
        groups.entrySet().stream().forEach(outter ->{

            ConcurrentHashMap<String, TaskFuturePair> group = outter.getValue();

            List<String> futuresToRemove = new ArrayList<>();
            /**
             * check which future should be removed
             */
            group.entrySet().stream().forEach(inner -> {
                TaskFuturePair tfp = inner.getValue();
                if (toBeRemoved(tfp)){
                    futuresToRemove.add(inner.getKey());
                }
            });
            /**
             * remove groups
             */
            futuresToRemove.stream().forEach(key -> {
                group.remove(key);
                logger.info("[OrderScheduler.garbageCollection] OtsId: " + key);
            });

            /**
             * check which group should be removed
             */
            if (outter.getValue().size() == 0)
                groupsToRemove.add(outter.getKey());
        });

        groupsToRemove.stream().forEach(key -> {
            groups.remove(key);
            logger.info("[OrderScheduler.garbageCollection] GroupId: " + key);

        });
        logger.info("[OrderScheduler.garbageCollection] GC End");
    }

    private boolean toBeRemoved(TaskFuturePair tfp){
        if (!tfp.future.isDone())
            return false;
        Calendar oneDayAgo = Calendar.getInstance();
        oneDayAgo.add(Calendar.DAY_OF_WEEK, -1);
        return tfp.orderTask.getTimeToSend().before(oneDayAgo);
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    public ScheduledFuture schedule(OrderTask orderTask, Calendar calendar){
        calendar = randomBias(calendar);
        logger.info("[OrderScheduler.schedule."+orderTask.getId()+"] Time: " + DateUtil.calendarToString(calendar, DateUtil.datetimeFormat));
        logger.info("[OrderScheduler.schedule."+orderTask.getId()+"] Order: " + JSON.toJSONString(orderTask.getOrder()));

        orderTask.setTimeToSend(calendar);

        ScheduledFuture future = schedule(orderTask, calendar.getTime());

        String groupId = orderTask.getOts().getGroupId();
        String otsId = orderTask.getId();
        ConcurrentHashMap<String, TaskFuturePair> group = groups.get(groupId);
        if (group == null)
            group = new ConcurrentHashMap<>();
        TaskFuturePair tfp = new TaskFuturePair(orderTask, future);
        group.put(otsId, tfp);

        groups.put(groupId, group);
        return future;
    }

    public ScheduledFuture schedule(Runnable task, Date datetime){
        return threadPoolTaskScheduler.schedule(task, datetime);
    }

    public List<String> cancel(String groupId){
        List<String> cancelledId = new ArrayList<>();

        ConcurrentHashMap<String, TaskFuturePair> group = groups.get(groupId);
        if (group == null)
            return cancelledId;
        group.entrySet().stream().forEach(e -> {
            TaskFuturePair tfp = e.getValue();
            if (tfp.future.isDone()) {
                /**
                 * Send Cancel Order
                 */
                // 1. create dao
                logger.info("[OrderScheduler.cancel."+e.getKey()+"] Send CancelOrder");
                String tsUsername = tfp.orderTask.getTraderSideUsername();
                Integer brokerId = tfp.orderTask.getBrokerId();
                Broker broker = brokerService.findById(brokerId);
                String token = brokerSideUserService.getToken(tsUsername, brokerId);

                CancelOrderDao cancelOrderDao = (CancelOrderDao)daoFactory.createWithToken(broker,"CancelOrder",token);

                // 2. send cancel order
                Order cancelOrder = tfp.orderTask.getOrder().createCancelOrder();
                Order createdCancelOrder = cancelOrderDao.create(cancelOrder);

                // 3. update ots
                OrderToSend ots = tfp.orderTask.getOts();
                ots.setCancelOrderId(createdCancelOrder.getId());
                orderToSendDao.save(ots);
                logger.info("[OrderScheduler.cancel."+e.getKey()+"] CancelOrder Id:" + ots.getCancelOrderId());
            }
            else if (tfp.future.cancel(false)) {
                logger.info("[OrderScheduler.cancel."+e.getKey()+"] Cancel future" );
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
