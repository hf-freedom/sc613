package com.restaurant.entity;

import lombok.Data;

@Data
public class Dish {
    private String dishId;
    private String name;
    private int cookingTimeMinutes;
    private int stock;
    private double price;
}
