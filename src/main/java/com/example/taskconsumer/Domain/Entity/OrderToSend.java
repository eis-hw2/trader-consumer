package com.example.taskconsumer.Domain.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class OrderToSend {
    public final static String INIT = "INIT";
    public final static String SCHEDULED = "SCHEDULED";
    public final static String CREATED = "CREATED";
    public final static String CANCELLED = "CANCELLED";
    
    private String groupId;
    @Id
    private String id;
    private String brokerOrderId;
    private Order order;
    private String traderSideUsername;
    private Integer brokerId;
    private String datetime;
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getTraderSideUsername() {
        return traderSideUsername;
    }

    public void setTraderSideUsername(String traderSideUsername) {
        this.traderSideUsername = traderSideUsername;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBrokerOrderId() {
        return brokerOrderId;
    }

    public void setBrokerOrderId(String brokerOrderId) {
        this.brokerOrderId = brokerOrderId;
    }
}
