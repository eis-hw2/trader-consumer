package com.example.taskconsumer.Domain.Entity;

public class OrderToSend {
    private String id;
    private Order order;
    private String traderSideUsername;
    private Integer brokerId;
    private String datetime;

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
}
