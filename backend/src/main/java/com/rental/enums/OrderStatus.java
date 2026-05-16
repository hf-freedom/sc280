package com.rental.enums;

public enum OrderStatus {
    PENDING("待确认"),
    CONFIRMED("已确认"),
    RENTING("租赁中"),
    RETURNED("已归还"),
    CANCELLED("已取消"),
    OVERDUE("已逾期");

    private String desc;

    OrderStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}