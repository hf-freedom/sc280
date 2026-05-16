package com.rental.controller;

import com.rental.dto.*;
import com.rental.entity.InventoryItem;
import com.rental.entity.Product;
import com.rental.entity.RentalOrder;
import com.rental.entity.User;
import com.rental.service.RentalService;
import com.rental.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RentalController {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private RentalService rentalService;

    @GetMapping("/products")
    public Result<List<Product>> getProducts() {
        return Result.success(new ArrayList<>(dataStore.products.values()));
    }

    @GetMapping("/products/{id}")
    public Result<Product> getProduct(@PathVariable String id) {
        Product product = dataStore.products.get(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    @GetMapping("/products/{id}/availability")
    public Result<Map<String, Object>> checkAvailability(
            @PathVariable String id,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime.replace(" ", "T"));
            LocalDateTime end = LocalDateTime.parse(endTime.replace(" ", "T"));
            int available = rentalService.checkInventoryAvailability(id, start, end);
            Map<String, Object> result = new HashMap<>();
            result.put("availableCount", available);
            result.put("productId", id);
            result.put("startTime", startTime);
            result.put("endTime", endTime);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/inventory")
    public Result<List<InventoryItem>> getInventory(
            @RequestParam(required = false) String productId) {
        List<InventoryItem> items;
        if (productId != null) {
            items = dataStore.getItemsByProductId(productId);
        } else {
            items = new ArrayList<>(dataStore.inventoryItems.values());
        }
        return Result.success(items);
    }

    @PostMapping("/inventory/{id}/complete-maintenance")
    public Result<Void> completeMaintenance(@PathVariable String id) {
        try {
            rentalService.completeMaintenance(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/users")
    public Result<List<User>> getUsers() {
        return Result.success(new ArrayList<>(dataStore.users.values()));
    }

    @GetMapping("/users/{id}")
    public Result<User> getUser(@PathVariable String id) {
        User user = dataStore.users.get(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @GetMapping("/orders")
    public Result<List<RentalOrder>> getOrders() {
        return Result.success(new ArrayList<>(dataStore.orders.values()));
    }

    @GetMapping("/orders/{id}")
    public Result<RentalOrder> getOrder(@PathVariable String id) {
        RentalOrder order = dataStore.orders.get(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @PostMapping("/orders")
    public Result<RentalOrder> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            RentalOrder order = rentalService.createOrder(request);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/cancel")
    public Result<RentalOrder> cancelOrder(@PathVariable String id) {
        try {
            RentalOrder order = rentalService.cancelOrder(id);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/orders/renew")
    public Result<RentalOrder> renewOrder(@RequestBody RenewOrderRequest request) {
        try {
            RentalOrder order = rentalService.renewOrder(request);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/orders/return")
    public Result<RentalOrder> returnItem(@RequestBody ReturnItemRequest request) {
        try {
            RentalOrder order = rentalService.returnItem(request);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/alerts/expiring")
    public Result<List<RentalOrder>> getExpiringOrders() {
        return Result.success(rentalService.getExpiringOrders());
    }

    @GetMapping("/alerts/overdue")
    public Result<List<RentalOrder>> getOverdueOrders() {
        return Result.success(rentalService.getOverdueOrders());
    }

    @GetMapping("/alerts/maintenance-timeout")
    public Result<List<InventoryItem>> getMaintenanceTimeout() {
        return Result.success(rentalService.getMaintenanceTimeoutItems());
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", dataStore.products.size());
        stats.put("totalInventory", dataStore.inventoryItems.size());
        stats.put("totalUsers", dataStore.users.size());
        stats.put("totalOrders", dataStore.orders.size());

        long activeOrders = dataStore.orders.values().stream()
                .filter(o -> o.getStatus() == com.rental.enums.OrderStatus.CONFIRMED ||
                        o.getStatus() == com.rental.enums.OrderStatus.RENTING ||
                        o.getStatus() == com.rental.enums.OrderStatus.OVERDUE)
                .count();
        stats.put("activeOrders", activeOrders);

        long maintenanceCount = dataStore.inventoryItems.values().stream()
                .filter(i -> i.getStatus() == com.rental.enums.ItemStatus.MAINTENANCE)
                .count();
        stats.put("maintenanceCount", maintenanceCount);

        return Result.success(stats);
    }
}