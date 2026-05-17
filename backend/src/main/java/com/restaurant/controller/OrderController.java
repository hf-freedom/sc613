package com.restaurant.controller;

import com.restaurant.common.Result;
import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import com.restaurant.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Order> createOrder(@RequestParam String tableId, @RequestBody List<Map<String, Object>> dishItems) {
        Order order = orderService.createOrder(tableId, dishItems);
        if (order == null) {
            return Result.error("创建订单失败");
        }
        return Result.success(order);
    }

    @PostMapping("/startCooking")
    public Result<Boolean> startCooking(@RequestParam String orderId, @RequestParam String orderItemId) {
        boolean success = orderService.startCooking(orderId, orderItemId);
        return Result.success(success);
    }

    @PostMapping("/completeDish")
    public Result<Boolean> completeDish(@RequestParam String orderId, @RequestParam String orderItemId) {
        boolean success = orderService.completeDish(orderId, orderItemId);
        return Result.success(success);
    }

    @PostMapping("/pay")
    public Result<Boolean> payOrder(@RequestParam String orderId) {
        boolean success = orderService.payOrder(orderId);
        return Result.success(success);
    }

    @GetMapping("/list")
    public Result<List<Order>> getAllOrders() {
        return Result.success(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    public Result<Order> getOrderById(@PathVariable String orderId) {
        return Result.success(orderService.getOrderById(orderId));
    }

    @GetMapping("/byTable/{tableId}")
    public Result<Order> getOrderByTableId(@PathVariable String tableId) {
        return Result.success(orderService.getOrderByTableId(tableId));
    }

    @GetMapping("/kitchenQueue")
    public Result<List<OrderItem>> getKitchenQueue() {
        return Result.success(orderService.getKitchenQueue());
    }
}
