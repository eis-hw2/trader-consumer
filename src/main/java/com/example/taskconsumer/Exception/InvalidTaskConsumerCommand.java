package com.example.taskconsumer.Exception;

public class InvalidTaskConsumerCommand extends RuntimeException{
    public InvalidTaskConsumerCommand(String msg){
        super(msg);
    }
}
