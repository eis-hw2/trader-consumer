package com.example.taskconsumer.Domain.Entity;

import com.alibaba.fastjson.annotation.JSONField;

import javax.persistence.*;

@Entity
public class Broker {
    public static String NEW = "NEW";
    public static String DEAD = "DEAD";
    public static String ALIVE = "ALIVE";

    private static final String writePort = "30251";
    private static final String readPort = "30252";
    private static final String loginPort = "30253";
    private static final String webSocketPort = "30254";

    private static final String HTTP_PROTOCOL = "http://";
    private static final String WS_PROTOCOL = "ws://";

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private String url;
    private String description;
    private String status;
    public Broker(){}

    public Broker(String url){
        this.url = url;
        this.status = NEW;
    }

    @JSONField(serialize = false)
    @Transient
    public final String getWriteApi(){
        return HTTP_PROTOCOL + url + ":" + writePort;
    }

    @JSONField(serialize = false)
    @Transient
    public final String getReadApi(){
        return HTTP_PROTOCOL + url + ":" + readPort;
    }

    @JSONField(serialize = false)
    @Transient
    public final String getWebSocket(){
        return WS_PROTOCOL + url + ":" + webSocketPort;
    }

    @JSONField(serialize = false)
    @Transient
    public final String getLoginApi() { return HTTP_PROTOCOL + url + ":" + loginPort; }

    @Basic
    @Column(name = "url", nullable = false, length = 100)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Id
    @Column(name = "id", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "description", nullable = true, length = 100)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "status", nullable = true, length = 100)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Broker broker = (Broker) o;

        if (id != null ? !id.equals(broker.id) : broker.id != null) return false;
        if (url != null ? !url.equals(broker.url) : broker.url != null) return false;
        if (description != null ? !description.equals(broker.description) : broker.description != null) return false;
        if (status != null ? !status.equals(broker.status) : broker.status != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
