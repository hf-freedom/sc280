package com.rental.enums;

public enum ItemStatus {
    AVAILABLE("可租赁"),
    RENTED("已出租"),
    MAINTENANCE("维修中"),
    DAMAGED("已损坏");

    private String desc;

    ItemStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}