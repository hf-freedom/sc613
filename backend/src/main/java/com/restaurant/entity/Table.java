package com.restaurant.entity;

import com.restaurant.enums.TableStatus;
import com.restaurant.enums.TableType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Table {
    private String tableId;
    private String tableNumber;
    private TableType tableType;
    private TableStatus status;
    private String currentQueueId;
    private LocalDateTime occupyTime;
    private LocalDateTime checkoutTime;
}
