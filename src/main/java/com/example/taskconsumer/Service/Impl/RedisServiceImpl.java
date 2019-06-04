package com.example.taskconsumer.Service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.example.taskconsumer.Service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {
    private static Logger logger = LoggerFactory.getLogger("RedisService");

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Object get(final String key) {
        JSONObject pair = new JSONObject();
        pair.put("Key", key);

        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        Object value = operations.get(key);

        pair.put("Value", value);
        logger.info("[RedisService.get] " + pair.toJSONString());
        return value;
    }

    @Override
    public <T> T get(final String key, Class<T> clazz) {
        T res = (T) get(key);
        return res;
    }

    @Override
    public boolean set(final String key, Object value) {
        JSONObject pair = new JSONObject();
        pair.put("Key", key);
        pair.put("Value", value);
        logger.info("[RedisService.set] " + pair.toJSONString());

        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean set(final String key, Object value, Long expiration) {
        JSONObject pair = new JSONObject();
        pair.put("Key", key);
        pair.put("Value", value);
        pair.put("Expiration", expiration);
        logger.info("[RedisService.set] " + pair.toJSONString());

        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expiration, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean setIfAbsent(final String key, Object value, Long expiration){
        JSONObject pair = new JSONObject();
        pair.put("Key", key);
        pair.put("Value", value);
        pair.put("Expiration", expiration);
        logger.info("[RedisService.set] " + pair.toJSONString());

        boolean result = false;
        try{
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            result = operations.setIfAbsent(key, value);
            redisTemplate.expire(key, expiration, TimeUnit.SECONDS);
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void remove(String key) {
        logger.info("[RedisService.remove] " + key);
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    @Override
    public boolean exists(String key) {
        logger.info("[RedisService.exists] " + key);
        return redisTemplate.hasKey(key);
    }
}

