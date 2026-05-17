package com.restaurant.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Order {
    private String orderId;
    private String tableId;
    private String tableNumber;
    private String queueId;
    private List<OrderItem> items = new ArrayList<>();
    private LocalDateTime createTime;
    private boolean allDishesCompleted;
    private boolean isPaid;
}
