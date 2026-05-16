package com.rental.enums;

public enum CreditLevel {
    HIGH("高信用", 0.0, 0),
    MEDIUM("中信用", 1.0, 1),
    LOW("低信用", 2.0, 2);

    private String desc;
    private double depositMultiplier;
    private int level;

    CreditLevel(String desc, double depositMultiplier, int level) {
        this.desc = desc;
        this.depositMultiplier = depositMultiplier;
        this.level = level;
    }

    public String getDesc() {
        return desc;
    }

    public double getDepositMultiplier() {
        return depositMultiplier;
    }

    public int getLevel() {
        return level;
    }

    public static CreditLevel fromScore(int score) {
        if (score >= 700) {
            return HIGH;
        } else if (score >= 500) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}