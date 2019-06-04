package com.example.taskconsumer.Service;


import com.example.taskconsumer.Domain.Entity.Broker;

import java.util.List;

public interface BrokerService {

    List<Broker> findAll();

    Broker findById(Integer id);
}
