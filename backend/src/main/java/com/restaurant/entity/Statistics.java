package com.restaurant.entity;

import lombok.Data;

@Data
public class Statistics {
    private double averageWaitTimeMinutes;
    private double tableTurnoverRate;
    private double dishTimeoutRate;
    private int totalQueuesToday;
    private int completedTablesToday;
    private int totalDishesToday;
    private int timeoutDishesToday;
    private long totalWaitTimeSeconds;
}
