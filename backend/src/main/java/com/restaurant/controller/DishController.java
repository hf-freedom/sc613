package com.restaurant.controller;

import com.restaurant.common.Result;
import com.restaurant.entity.Dish;
import com.restaurant.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dish")
@CrossOrigin
public class DishController {
    @Autowired
    private DishService dishService;

    @GetMapping("/list")
    public Result<List<Dish>> getAllDishes() {
        return Result.success(dishService.getAllDishes());
    }

    @GetMapping("/{dishId}")
    public Result<Dish> getDishById(@PathVariable String dishId) {
        return Result.success(dishService.getDishById(dishId));
    }
}
