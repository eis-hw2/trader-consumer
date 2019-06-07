package com.example.taskconsumer.Util;

import com.example.taskconsumer.Domain.Entity.Util.Type;

public class Util {
    public static Type typeConvert(String type){
        switch (type){
            case "MarketOrder":
                return Type.MarketOrder;
            case "LimitOrder":
                return Type.LimitOrder;
            case "StopOrder":
                return Type.StopOrder;
            default:
                return null;
        }
    }
}
