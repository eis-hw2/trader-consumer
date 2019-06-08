package com.example.taskconsumer.Dao.Repo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.taskconsumer.Domain.Entity.Order;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractOrderDao extends SecuredDao<String, Order>{

    @Override
    public HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("token", getToken());
        return headers;
    }

    @Override
    public Class<Order> getValueClass() {
        return Order.class;
    }

    @Override
    public Class<Order[]> getValueArrayClass() {
        return Order[].class;
    }

    public List<Order> findByTraderName(String traderName){
        String url = getReadBaseUrl() + "/search/traderName?traderName=" + traderName;
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().getForEntity(url, JSONObject.class);
        Order[] res = responseEntity.getBody()
                .getJSONObject("_embedded")
                .getJSONArray(getType())
                .toJavaObject(getValueArrayClass());
        System.out.println(JSON.toJSONString(res));
        return Arrays.asList(res);
    }

    @Override
    public Order create(Order  value){
        Logger logger = getLogger();
        String url = getWriteBaseUrl();
        logger.info("[OrderDao.create] URL: " + url);
        logger.info("[OrderDao.create] Order:" + JSON.toJSONString(value));
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().postForEntity(url, getHttpEntity(value), JSONObject.class);
        JSONObject rw = responseEntity.getBody();
        int status = rw.getInteger("status");
        logger.info("[OrderDao.create] Status: " + status);
        logger.info("[OrderDao.create] Response: " + rw.toJSONString());
        Order res;
        if (status != 200)
            res = Order.ERROR_ORDER;
        else
            res = rw.getObject("body", Order.class);
        return res;
    }

    public Order findByClientId(String clientId){
        if (getType().equals("CancelOrder")){
            throw new RuntimeException("CancelOrder has no clientId.");
        }

        Logger logger = getLogger();
        String url = getReadBaseUrl() + "/search/clientId?clientId="+clientId;
        logger.info("[OrderDao.findByClientId] URL: " + url);
        logger.info("[OrderDao.findByClientId] ClientId:" + clientId);
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().getForEntity(url, JSONObject.class);

        JSONObject rw = responseEntity.getBody();
        int status = rw.getInteger("status");
        /**
         * TODO
         * 临时补丁。这个api上游搜索如果找不到会爆404
         */
        if (status == 404){
            return null;
        }
        logger.info("[OrderDao.create] Status: " + status);
        logger.info("[OrderDao.create] Response: " + rw.toJSONString());

        Order res = rw
                .getJSONObject("_embedded")
                .getJSONObject(getType())
                .toJavaObject(getValueClass());
        return res;
    }
}
