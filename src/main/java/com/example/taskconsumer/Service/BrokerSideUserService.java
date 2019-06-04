package com.example.taskconsumer.Service;

public interface BrokerSideUserService {
    String login(String traderSideUsername, Integer brokerId);

    String getToken(String traderSideUsername, Integer brokerId);
}
