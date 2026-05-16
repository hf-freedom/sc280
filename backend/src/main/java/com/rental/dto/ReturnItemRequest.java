package com.rental.dto;

import javax.validation.constraints.NotBlank;

public class ReturnItemRequest {
    @NotBlank(message = "订单ID不能为空")
    private String orderId;
    private boolean damaged;
    private String damageNote;
    private Integer maintenanceDays;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public boolean isDamaged() { return damaged; }
    public void setDamaged(boolean damaged) { this.damaged = damaged; }
    public String getDamageNote() { return damageNote; }
    public void setDamageNote(String damageNote) { this.damageNote = damageNote; }
    public Integer getMaintenanceDays() { return maintenanceDays; }
    public void setMaintenanceDays(Integer maintenanceDays) { this.maintenanceDays = maintenanceDays; }
}