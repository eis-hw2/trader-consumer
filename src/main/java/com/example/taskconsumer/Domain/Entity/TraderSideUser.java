package com.example.taskconsumer.Domain.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document
public class TraderSideUser {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;
    private List<String> roles = new ArrayList<>();
    private Map<String, BrokerSideUser> brokerSideUsers = new HashMap<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Map<String, BrokerSideUser> getBrokerSideUsers() {
        return brokerSideUsers;
    }

    public void setBrokerSideUsers(Map<String, BrokerSideUser> brokerSideUsers) {
        this.brokerSideUsers = brokerSideUsers;
    }

    public BrokerSideUser getBrokerSideUser(Integer bid){
        return brokerSideUsers.get(bid.toString());
    }

}
