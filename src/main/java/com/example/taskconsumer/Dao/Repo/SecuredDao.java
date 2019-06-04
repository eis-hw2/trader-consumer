package com.example.taskconsumer.Dao.Repo;

public abstract class SecuredDao<K, V> extends DynamicDao<K, V>{
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
