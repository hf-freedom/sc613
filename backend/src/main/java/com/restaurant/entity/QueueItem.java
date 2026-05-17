package com.restaurant.entity;

import com.restaurant.enums.QueueStatus;
import com.restaurant.enums.TableType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QueueItem {
    private String queueId;
    private String queueNumber;
    private TableType tableType;
    private int peopleCount;
    private QueueStatus status;
    private LocalDateTime createTime;
    private LocalDateTime callTime;
    private LocalDateTime confirmTime;
    private int confirmTimeoutSeconds;
}
