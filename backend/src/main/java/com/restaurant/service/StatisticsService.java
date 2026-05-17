package com.restaurant.service;

import com.restaurant.entity.Statistics;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {
    private int totalQueuesToday = 0;
    private int completedTablesToday = 0;
    private int totalDishesToday = 0;
    private int timeoutDishesToday = 0;
    private long totalWaitTimeSeconds = 0;

    public Statistics getStatistics() {
        Statistics stats = new Statistics();
        stats.setTotalQueuesToday(totalQueuesToday);
        stats.setCompletedTablesToday(completedTablesToday);
        stats.setTotalDishesToday(totalDishesToday);
        stats.setTimeoutDishesToday(timeoutDishesToday);
        stats.setTotalWaitTimeSeconds(totalWaitTimeSeconds);

        if (totalQueuesToday > 0) {
            stats.setAverageWaitTimeMinutes((double) totalWaitTimeSeconds / totalQueuesToday / 60.0);
        }

        if (totalQueuesToday > 0) {
            stats.setTableTurnoverRate((double) completedTablesToday / totalQueuesToday);
        }

        if (totalDishesToday > 0) {
            stats.setDishTimeoutRate((double) timeoutDishesToday / totalDishesToday);
        }

        return stats;
    }

    public void incrementTotalQueues() {
        totalQueuesToday++;
    }

    public void incrementCompletedTables() {
        completedTablesToday++;
    }

    public void addTotalDishes(int count) {
        totalDishesToday += count;
    }

    public void incrementTimeoutDishes() {
        timeoutDishesToday++;
    }

    public void addWaitTime(long seconds) {
        totalWaitTimeSeconds += seconds;
    }

    public void resetDailyStatistics() {
        totalQueuesToday = 0;
        completedTablesToday = 0;
        totalDishesToday = 0;
        timeoutDishesToday = 0;
        totalWaitTimeSeconds = 0;
    }
}
