package com.rental.entity;

import com.rental.enums.CreditLevel;

import java.time.LocalDateTime;

public class User {
    private String id;
    private String name;
    private String phone;
    private int creditScore;
    private CreditLevel creditLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getCreditScore() { return creditScore; }
    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
        this.creditLevel = CreditLevel.fromScore(creditScore);
    }
    public CreditLevel getCreditLevel() { return creditLevel; }
    public void setCreditLevel(CreditLevel creditLevel) { this.creditLevel = creditLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}