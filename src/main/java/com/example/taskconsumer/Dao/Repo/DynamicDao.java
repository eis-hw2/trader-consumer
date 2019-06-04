package com.example.taskconsumer.Dao.Repo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.taskconsumer.Domain.Entity.Broker;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public abstract class DynamicDao<K, V> {
    public abstract Logger getLogger();

    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    private Broker broker;

    public Broker getBroker() {
        return broker;
    }

    public HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public HttpEntity<Object> getHttpEntity(Object requestBody) {
        HttpEntity<Object> httpEntity = new HttpEntity<>(requestBody, getHttpHeaders());
        return httpEntity;
    }

    public String getWriteBaseUrl(){
        return getBroker().getWriteApi() + "/" + getType();
    }

    public String getReadBaseUrl(){
        return getBroker().getReadApi() + "/" + getType();
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public abstract String getType();
    public abstract Class<V> getValueClass();
    public abstract Class<V[]> getValueArrayClass();

    /**
     * upstream return value :
     * {
     *     "body": $ID,
     *     "description": "OK",
     *     "status": "200"
     * }
     */
    public Object create(V  value){
        String url = getWriteBaseUrl();
        getLogger().info("[Dao.create] " + url);
        getLogger().info("[Dao.create] " + JSON.toJSONString(value));
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().postForEntity(url, getHttpEntity(value), JSONObject.class);
        JSONObject rw = responseEntity.getBody();
        getLogger().info("[Dao.create] " + rw.toJSONString());
        return rw.get("body");
    }

    public V findById(String id) {
        String url = getReadBaseUrl() + "/" + id;
        getLogger().info("[Dao.findById] " + url);
        V res = null;
        try {
            ResponseEntity<JSONObject> responseEntity = getRestTemplate().getForEntity(url, JSONObject.class);
            res = responseEntity.getBody().toJavaObject(getValueClass());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        getLogger().info("[Dao.findById] " + JSON.toJSONString(res));
        return res;
    }

    public List<V> findAll(){
        String url = getReadBaseUrl();
        getLogger().info("[Dao.findAll] " + url);
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().getForEntity(url, JSONObject.class);
        V[] res = responseEntity.getBody()
                .getJSONObject("_embedded")
                .getJSONArray(getType())
                .toJavaObject(getValueArrayClass());
        getLogger().info("[Dao.findAll] " + JSON.toJSONString(res));
        return Arrays.asList(res);
    }
}
