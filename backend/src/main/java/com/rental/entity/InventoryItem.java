package com.rental.entity;

import com.rental.enums.ItemStatus;

import java.time.LocalDateTime;

public class InventoryItem {
    private String id;
    private String productId;
    private String serialNumber;
    private ItemStatus status;
    private String currentOrderId;
    private LocalDateTime maintenanceStart;
    private int maintenanceDays;
    private String maintenanceNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
    public String getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(String currentOrderId) { this.currentOrderId = currentOrderId; }
    public LocalDateTime getMaintenanceStart() { return maintenanceStart; }
    public void setMaintenanceStart(LocalDateTime maintenanceStart) { this.maintenanceStart = maintenanceStart; }
    public int getMaintenanceDays() { return maintenanceDays; }
    public void setMaintenanceDays(int maintenanceDays) { this.maintenanceDays = maintenanceDays; }
    public String getMaintenanceNote() { return maintenanceNote; }
    public void setMaintenanceNote(String maintenanceNote) { this.maintenanceNote = maintenanceNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}