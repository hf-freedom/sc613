package com.restaurant.scheduler;

import com.restaurant.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QueueScheduler {
    @Autowired
    private QueueService queueService;

    @Scheduled(fixedRate = 10000)
    public void checkQueueTimeouts() {
        queueService.checkTimeouts();
    }
}
