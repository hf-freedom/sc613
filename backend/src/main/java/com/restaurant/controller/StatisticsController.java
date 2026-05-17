package com.restaurant.controller;

import com.restaurant.common.Result;
import com.restaurant.entity.Statistics;
import com.restaurant.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin
public class StatisticsController {
    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    public Result<Statistics> getStatistics() {
        return Result.success(statisticsService.getStatistics());
    }

    @PostMapping("/reset")
    public Result<Void> resetStatistics() {
        statisticsService.resetDailyStatistics();
        return Result.success();
    }
}
