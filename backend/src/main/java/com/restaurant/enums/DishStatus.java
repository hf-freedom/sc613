package com.restaurant.enums;

public enum DishStatus {
    PENDING("待制作"),
    COOKING("制作中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String description;

    DishStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
