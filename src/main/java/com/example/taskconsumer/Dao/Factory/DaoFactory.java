package com.example.taskconsumer.Dao.Factory;

import com.example.taskconsumer.Dao.Repo.DynamicDao;
import com.example.taskconsumer.Dao.Repo.SecuredDao;
import com.example.taskconsumer.Domain.Entity.Broker;
import com.example.taskconsumer.Domain.Entity.Order;
import com.example.taskconsumer.Util.LRUCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DaoFactory {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LRUCache<String, DynamicDao> daoCache;

    @Bean
    public LRUCache<String, DynamicDao> daoCache(){
        return new LRUCache<>(20);
    }

    public DynamicDao create(Broker broker, String type){
        String key = broker.getUrl() + type;
        DynamicDao dao = daoCache.get(key);
        if (dao != null) {
            return dao;
        }
        else {
            dao = (DynamicDao)applicationContext.getBean(type + "Dao");
            dao.setBroker(broker);
            daoCache.put(key, dao);
            return dao;
        }
    }

    public SecuredDao<String, Order> createWithToken(Broker broker, String type, String token){
        SecuredDao dao = (SecuredDao)applicationContext.getBean(type + "Dao");
        dao.setBroker(broker);
        dao.setToken(token);
        return dao;
    }
}
