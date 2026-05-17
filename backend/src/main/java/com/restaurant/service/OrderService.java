package com.restaurant.service;

import com.restaurant.entity.*;
import com.restaurant.enums.DishStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final Map<String, Order> orderMap = new ConcurrentHashMap<>();

    @Autowired
    private DishService dishService;

    @Autowired
    private TableService tableService;

    @Autowired
    private StatisticsService statisticsService;

    public Order createOrder(String tableId, List<Map<String, Object>> dishItems) {
        Table table = tableService.getTableById(tableId);
        if (table == null) {
            return null;
        }

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setTableId(tableId);
        order.setTableNumber(table.getTableNumber());
        order.setQueueId(table.getCurrentQueueId());
        order.setCreateTime(LocalDateTime.now());
        order.setAllDishesCompleted(false);
        order.setPaid(false);

        List<String> insufficientStockDishes = new ArrayList<>();

        for (Map<String, Object> item : dishItems) {
            String dishId = (String) item.get("dishId");
            int quantity = (int) item.get("quantity");

            Dish dish = dishService.getDishById(dishId);
            if (dish == null) continue;

            if (!dishService.decreaseStock(dishId, quantity)) {
                insufficientStockDishes.add(dish.getName());
                continue;
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemId(UUID.randomUUID().toString());
            orderItem.setDishId(dishId);
            orderItem.setDishName(dish.getName());
            orderItem.setQuantity(quantity);
            orderItem.setCookingTimeMinutes(dish.getCookingTimeMinutes());
            orderItem.setStatus(DishStatus.PENDING);
            orderItem.setLocked(false);

            order.getItems().add(orderItem);
        }

        order.getItems().sort(Comparator.comparingInt(OrderItem::getCookingTimeMinutes));

        orderMap.put(order.getOrderId(), order);
        statisticsService.addTotalDishes(order.getItems().size());

        if (!insufficientStockDishes.isEmpty()) {
            System.out.println("库存不足的菜品: " + insufficientStockDishes);
        }

        return order;
    }

    public boolean startCooking(String orderId, String orderItemId) {
        Order order = orderMap.get(orderId);
        if (order == null) return false;

        for (OrderItem item : order.getItems()) {
            if (item.getOrderItemId().equals(orderItemId)) {
                if (item.isLocked() || item.getStatus() != DishStatus.PENDING) {
                    return false;
                }
                item.setStatus(DishStatus.COOKING);
                item.setStartTime(LocalDateTime.now());
                item.setLocked(true);
                return true;
            }
        }
        return false;
    }

    public boolean completeDish(String orderId, String orderItemId) {
        Order order = orderMap.get(orderId);
        if (order == null) return false;

        for (OrderItem item : order.getItems()) {
            if (item.getOrderItemId().equals(orderItemId)) {
                item.setStatus(DishStatus.COMPLETED);
                item.setCompleteTime(LocalDateTime.now());

                Duration duration = Duration.between(item.getStartTime(), item.getCompleteTime());
                long actualMinutes = duration.toMinutes();
                if (actualMinutes > item.getCookingTimeMinutes()) {
                    statisticsService.incrementTimeoutDishes();
                }

                checkAllDishesCompleted(order);
                return true;
            }
        }
        return false;
    }

    private void checkAllDishesCompleted(Order order) {
        boolean allCompleted = order.getItems().stream()
                .allMatch(item -> item.getStatus() == DishStatus.COMPLETED);
        if (allCompleted) {
            order.setAllDishesCompleted(true);
        }
    }

    public boolean payOrder(String orderId) {
        Order order = orderMap.get(orderId);
        if (order == null || order.isPaid()) {
            return false;
        }
        order.setPaid(true);
        tableService.checkoutTable(order.getTableId());
        statisticsService.incrementCompletedTables();
        return true;
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orderMap.values());
    }

    public Order getOrderById(String orderId) {
        return orderMap.get(orderId);
    }

    public Order getOrderByTableId(String tableId) {
        return orderMap.values().stream()
                .filter(o -> tableId.equals(o.getTableId()) && !o.isPaid())
                .findFirst()
                .orElse(null);
    }

    public List<OrderItem> getKitchenQueue() {
        return orderMap.values().stream()
                .flatMap(o -> o.getItems().stream())
                .filter(item -> item.getStatus() == DishStatus.PENDING || item.getStatus() == DishStatus.COOKING)
                .sorted(Comparator.comparingInt(OrderItem::getCookingTimeMinutes))
                .collect(Collectors.toList());
    }
}
