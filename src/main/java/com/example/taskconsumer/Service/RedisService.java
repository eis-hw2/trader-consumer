package com.example.taskconsumer.Service;

public interface RedisService {
    boolean set(final String key, Object value);

    boolean set(final String key, Object value, Long expiration);

    Object get(final String key);

    <T> T get(final String key, Class<T> clazz);

    boolean exists(final String key);

    void remove(final String key);

    boolean setIfAbsent(final String key, Object value, Long expiration);
}
