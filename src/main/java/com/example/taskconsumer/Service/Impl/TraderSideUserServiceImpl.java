package com.example.taskconsumer.Service.Impl;

import com.example.taskconsumer.Dao.Repo.TraderSideUserDao;
import com.example.taskconsumer.Domain.Entity.TraderSideUser;
import com.example.taskconsumer.Service.TraderSideUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraderSideUserServiceImpl implements TraderSideUserService {
    @Autowired
    private TraderSideUserDao traderSideUserDao;

    @Override
    public TraderSideUser findById(String id) {
        return traderSideUserDao.findById(id).get();
    }

    @Override
    public TraderSideUser findByUsername(String username) {
        return traderSideUserDao.findByUsername(username);
    }

    @Override
    public List<TraderSideUser> findAll() {
        return traderSideUserDao.findAll();
    }
}
