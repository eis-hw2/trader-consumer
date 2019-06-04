package com.example.taskconsumer.Dao.Repo;

import com.example.taskconsumer.Domain.Entity.TraderSideUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TraderSideUserDao extends MongoRepository<TraderSideUser, String> {
    TraderSideUser findByUsername(String username);

}
