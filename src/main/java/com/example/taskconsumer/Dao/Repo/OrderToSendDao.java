package com.example.taskconsumer.Dao.Repo;

import com.example.taskconsumer.Domain.Entity.OrderToSend;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderToSendDao extends MongoRepository<OrderToSend, String> {
    List<OrderToSend> findByGroupId(String groupId);
}
