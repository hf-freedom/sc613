package com.restaurant.enums;

public enum TableStatus {
    AVAILABLE("空闲"),
    OCCUPIED("用餐中"),
    CLEANING("待清理");

    private final String description;

    TableStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
