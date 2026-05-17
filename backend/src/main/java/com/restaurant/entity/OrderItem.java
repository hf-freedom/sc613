package com.restaurant.entity;

import com.restaurant.enums.DishStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderItem {
    private String orderItemId;
    private String dishId;
    private String dishName;
    private int quantity;
    private int cookingTimeMinutes;
    private DishStatus status;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
    private boolean isLocked;
}
