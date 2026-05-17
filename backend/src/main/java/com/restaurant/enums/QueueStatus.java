package com.restaurant.enums;

public enum QueueStatus {
    WAITING("等待中"),
    CALLED("已叫号"),
    CONFIRMED("已确认"),
    SKIPPED("已过号"),
    CANCELLED("已取消");

    private final String description;

    QueueStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
