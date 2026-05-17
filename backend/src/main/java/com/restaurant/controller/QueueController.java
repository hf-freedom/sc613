package com.restaurant.controller;

import com.restaurant.common.Result;
import com.restaurant.entity.QueueItem;
import com.restaurant.enums.TableType;
import com.restaurant.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/queue")
@CrossOrigin
public class QueueController {
    @Autowired
    private QueueService queueService;

    @PostMapping("/take")
    public Result<QueueItem> takeQueue(@RequestParam int peopleCount) {
        QueueItem item = queueService.takeQueue(peopleCount);
        return Result.success(item);
    }

    @PostMapping("/call")
    public Result<QueueItem> callNextQueue(@RequestParam TableType tableType) {
        QueueItem item = queueService.callNextQueue(tableType);
        if (item == null) {
            return Result.error("没有等待的队列");
        }
        return Result.success(item);
    }

    @PostMapping("/confirm")
    public Result<Boolean> confirmQueue(@RequestParam String queueId) {
        boolean success = queueService.confirmQueue(queueId);
        return Result.success(success);
    }

    @GetMapping("/list")
    public Result<List<QueueItem>> getAllQueues() {
        return Result.success(queueService.getAllQueues());
    }

    @GetMapping("/byType")
    public Result<Map<TableType, List<QueueItem>>> getQueueByType() {
        return Result.success(queueService.getQueueByType());
    }

    @GetMapping("/{queueId}")
    public Result<QueueItem> getQueueById(@PathVariable String queueId) {
        return Result.success(queueService.getQueueById(queueId));
    }
}
