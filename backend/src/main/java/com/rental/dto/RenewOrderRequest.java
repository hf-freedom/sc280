package com.rental.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class RenewOrderRequest {
    @NotBlank(message = "订单ID不能为空")
    private String orderId;

    @NotNull(message = "新的结束时间不能为空")
    private String newEndTime;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getNewEndTime() { return newEndTime; }
    public void setNewEndTime(String newEndTime) { this.newEndTime = newEndTime; }
}