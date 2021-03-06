package com.example.taskconsumer.Dao.Repo.OrderDao;

import com.example.taskconsumer.Dao.Repo.AbstractOrderDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("LimitOrderDao")
@Scope("prototype")
public class LimitOrderDao extends AbstractOrderDao {

    private static Logger logger = LoggerFactory.getLogger("LimitiOrderDao");

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getType() {
        return "limitOrders";
    }


}
