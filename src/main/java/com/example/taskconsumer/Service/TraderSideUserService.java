package com.example.taskconsumer.Service;

import com.example.taskconsumer.Domain.Entity.TraderSideUser;

import java.util.List;

public interface TraderSideUserService {

    TraderSideUser findById(String id);

    TraderSideUser findByUsername(String username);

    List<TraderSideUser> findAll();
}
