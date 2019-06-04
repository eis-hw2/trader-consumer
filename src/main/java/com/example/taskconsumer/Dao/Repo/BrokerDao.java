package com.example.taskconsumer.Dao.Repo;

import com.example.taskconsumer.Domain.Entity.Broker;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrokerDao extends JpaRepository<Broker, Integer>{

    @Override
    Optional<Broker> findById(Integer integer);

    @Override
    List<Broker> findAll();
}
