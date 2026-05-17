package com.restaurant.service;

import com.restaurant.entity.Dish;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DishService {
    private final Map<String, Dish> dishMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initDishes() {
        createDish("宫保鸡丁", 15, 50, 38.0);
        createDish("红烧肉", 25, 30, 58.0);
        createDish("清蒸鱼", 20, 20, 88.0);
        createDish("炒青菜", 8, 100, 18.0);
        createDish("番茄炒蛋", 10, 80, 22.0);
        createDish("麻婆豆腐", 12, 60, 25.0);
        createDish("酸辣汤", 10, 50, 15.0);
        createDish("米饭", 2, 200, 3.0);
    }

    private void createDish(String name, int cookingTime, int stock, double price) {
        Dish dish = new Dish();
        dish.setDishId(UUID.randomUUID().toString());
        dish.setName(name);
        dish.setCookingTimeMinutes(cookingTime);
        dish.setStock(stock);
        dish.setPrice(price);
        dishMap.put(dish.getDishId(), dish);
    }

    public List<Dish> getAllDishes() {
        return new ArrayList<>(dishMap.values());
    }

    public Dish getDishById(String dishId) {
        return dishMap.get(dishId);
    }

    public boolean decreaseStock(String dishId, int quantity) {
        Dish dish = dishMap.get(dishId);
        if (dish == null || dish.getStock() < quantity) {
            return false;
        }
        dish.setStock(dish.getStock() - quantity);
        return true;
    }

    public void increaseStock(String dishId, int quantity) {
        Dish dish = dishMap.get(dishId);
        if (dish != null) {
            dish.setStock(dish.getStock() + quantity);
        }
    }
}
