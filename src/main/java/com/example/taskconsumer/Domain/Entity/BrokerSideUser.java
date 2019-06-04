package com.example.taskconsumer.Domain.Entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class BrokerSideUser implements Serializable{
    private Integer brokerId;
    private String username;

    @JSONField(serialize = false)
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
