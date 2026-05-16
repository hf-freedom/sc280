package com.rental.service;

import com.rental.dto.CreateOrderRequest;
import com.rental.dto.RenewOrderRequest;
import com.rental.dto.ReturnItemRequest;
import com.rental.entity.InventoryItem;
import com.rental.entity.Product;
import com.rental.entity.RentalOrder;
import com.rental.entity.User;
import com.rental.enums.ItemStatus;
import com.rental.enums.OrderStatus;
import com.rental.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class RentalService {

    @Autowired
    private DataStore dataStore;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RentalOrder createOrder(CreateOrderRequest request) {
        Product product = dataStore.products.get(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        User user = dataStore.users.get(request.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        LocalDateTime startTime = parseDateTime(request.getStartTime());
        LocalDateTime endTime = parseDateTime(request.getEndTime());

        if (startTime.isAfter(endTime)) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }

        int rentalDays = (int) ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate()) + 1;

        int availableCount = checkInventoryAvailability(request.getProductId(), startTime, endTime);
        if (availableCount < request.getQuantity()) {
            throw new RuntimeException("库存不足，当前可租赁数量: " + availableCount);
        }

        List<String> itemIds = allocateInventoryItems(request.getProductId(), request.getQuantity());

        RentalOrder order = new RentalOrder();
        order.setId(dataStore.generateId());
        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setItemIds(itemIds);
        order.setQuantity(request.getQuantity());
        order.setStartTime(startTime);
        order.setEndTime(endTime);
        order.setRentalDays(rentalDays);
        order.setDailyPrice(product.getDailyPrice());
        order.setTotalAmount(product.getDailyPrice() * rentalDays * request.getQuantity());

        double deposit = product.getBaseDeposit() * request.getQuantity() * user.getCreditLevel().getDepositMultiplier();
        order.setDeposit(deposit);

        order.setPenaltyAmount(0.0);
        order.setRefundAmount(0.0);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setReturnedDamaged(false);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        dataStore.orders.put(order.getId(), order);

        for (String itemId : itemIds) {
            InventoryItem item = dataStore.inventoryItems.get(itemId);
            item.setStatus(ItemStatus.RENTED);
            item.setCurrentOrderId(order.getId());
            item.setUpdatedAt(LocalDateTime.now());
        }

        return order;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            if (dateTimeStr.length() <= 10) {
                LocalDate date = LocalDate.parse(dateTimeStr);
                return LocalDateTime.of(date, LocalTime.MIN);
            }
            return LocalDateTime.parse(dateTimeStr, FORMATTER);
        } catch (Exception e) {
            throw new RuntimeException("时间格式错误，请使用 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss");
        }
    }

    public int checkInventoryAvailability(String productId, LocalDateTime startTime, LocalDateTime endTime) {
        List<InventoryItem> allItems = dataStore.getItemsByProductId(productId);
        int availableCount = 0;

        for (InventoryItem item : allItems) {
            if (item.getStatus() == ItemStatus.MAINTENANCE || item.getStatus() == ItemStatus.DAMAGED) {
                continue;
            }

            boolean isOverlapping = false;

            if (item.getStatus() == ItemStatus.RENTED || item.getStatus() == ItemStatus.AVAILABLE) {
                for (RentalOrder order : dataStore.orders.values()) {
                    if (order.getItemIds().contains(item.getId())) {
                        if (order.getStatus() == OrderStatus.CONFIRMED ||
                                order.getStatus() == OrderStatus.RENTING ||
                                order.getStatus() == OrderStatus.OVERDUE) {

                            if (isTimeOverlapping(startTime, endTime, order.getStartTime(), order.getEndTime())) {
                                isOverlapping = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (!isOverlapping) {
                availableCount++;
            }
        }

        return availableCount;
    }

    private boolean isTimeOverlapping(LocalDateTime start1, LocalDateTime end1,
                                       LocalDateTime start2, LocalDateTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    private List<String> allocateInventoryItems(String productId, int quantity) {
        List<String> allocatedItems = new ArrayList<>();
        List<InventoryItem> allItems = dataStore.getItemsByProductId(productId);

        for (InventoryItem item : allItems) {
            if (allocatedItems.size() >= quantity) {
                break;
            }

            if (item.getStatus() == ItemStatus.AVAILABLE) {
                allocatedItems.add(item.getId());
            } else if (item.getStatus() == ItemStatus.RENTED) {
                boolean hasOverlap = false;
                for (RentalOrder order : dataStore.orders.values()) {
                    if (order.getItemIds().contains(item.getId()) &&
                            (order.getStatus() == OrderStatus.CONFIRMED ||
                                    order.getStatus() == OrderStatus.RENTING ||
                                    order.getStatus() == OrderStatus.OVERDUE)) {
                        hasOverlap = true;
                        break;
                    }
                }
                if (!hasOverlap) {
                    allocatedItems.add(item.getId());
                }
            }
        }

        return allocatedItems;
    }

    public RentalOrder renewOrder(RenewOrderRequest request) {
        RentalOrder order = dataStore.orders.get(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != OrderStatus.RENTING && order.getStatus() != OrderStatus.OVERDUE) {
            throw new RuntimeException("当前订单状态不支持续租");
        }

        LocalDateTime newEndTime = parseDateTime(request.getNewEndTime());

        if (newEndTime.isBefore(order.getEndTime())) {
            throw new RuntimeException("续租结束时间不能早于原结束时间");
        }

        int additionalDays = (int) ChronoUnit.DAYS.between(order.getEndTime().toLocalDate(), newEndTime.toLocalDate());
        if (additionalDays > 0) {
            int availableCount = checkInventoryAvailability(order.getProductId(), order.getEndTime(), newEndTime);
            if (availableCount < order.getQuantity()) {
                throw new RuntimeException("续租时间段库存不足，当前可租赁数量: " + availableCount);
            }
        }

        int additionalRentalDays = Math.max(additionalDays, 0);
        if (additionalRentalDays > 0) {
            double additionalAmount = order.getDailyPrice() * additionalRentalDays * order.getQuantity();
            order.setTotalAmount(order.getTotalAmount() + additionalAmount);
            order.setRentalDays(order.getRentalDays() + additionalRentalDays);
        }

        order.setEndTime(newEndTime);
        order.setStatus(OrderStatus.RENTING);
        order.setUpdatedAt(LocalDateTime.now());

        return order;
    }

    public RentalOrder cancelOrder(String orderId) {
        RentalOrder order = dataStore.orders.get(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("当前订单状态不支持取消");
        }

        long hoursUntilStart = ChronoUnit.HOURS.between(LocalDateTime.now(), order.getStartTime());
        double refundRatio = calculateRefundRatio(hoursUntilStart);

        order.setRefundAmount(order.getTotalAmount() * refundRatio);
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        for (String itemId : order.getItemIds()) {
            InventoryItem item = dataStore.inventoryItems.get(itemId);
            if (item != null) {
                item.setStatus(ItemStatus.AVAILABLE);
                item.setCurrentOrderId(null);
                item.setUpdatedAt(LocalDateTime.now());
            }
        }

        return order;
    }

    private double calculateRefundRatio(long hoursUntilStart) {
        if (hoursUntilStart >= 72) {
            return 1.0;
        } else if (hoursUntilStart >= 48) {
            return 0.8;
        } else if (hoursUntilStart >= 24) {
            return 0.5;
        } else {
            return 0.0;
        }
    }

    public RentalOrder returnItem(ReturnItemRequest request) {
        RentalOrder order = dataStore.orders.get(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != OrderStatus.RENTING && order.getStatus() != OrderStatus.OVERDUE) {
            throw new RuntimeException("当前订单状态不支持归还");
        }

        LocalDateTime actualReturnTime = LocalDateTime.now();
        order.setActualReturnTime(actualReturnTime);

        if (actualReturnTime.isAfter(order.getEndTime())) {
            long overdueDays = ChronoUnit.DAYS.between(order.getEndTime().toLocalDate(), actualReturnTime.toLocalDate());
            if (overdueDays > 0) {
                double penalty = overdueDays * order.getDailyPrice() * order.getQuantity() * 1.5;
                order.setPenaltyAmount(penalty);
            }
        }

        order.setReturnedDamaged(request.isDamaged());
        order.setDamageNote(request.getDamageNote());

        int maintenanceDays = request.getMaintenanceDays() != null ? request.getMaintenanceDays() : 3;

        for (String itemId : order.getItemIds()) {
            InventoryItem item = dataStore.inventoryItems.get(itemId);
            if (item != null) {
                item.setCurrentOrderId(null);

                if (request.isDamaged()) {
                    item.setStatus(ItemStatus.MAINTENANCE);
                    item.setMaintenanceStart(LocalDateTime.now());
                    item.setMaintenanceDays(maintenanceDays);
                    item.setMaintenanceNote(request.getDamageNote());
                } else {
                    item.setStatus(ItemStatus.AVAILABLE);
                }
                item.setUpdatedAt(LocalDateTime.now());
            }
        }

        order.setStatus(OrderStatus.RETURNED);
        order.setUpdatedAt(LocalDateTime.now());

        return order;
    }

    public List<RentalOrder> getExpiringOrders() {
        List<RentalOrder> expiringOrders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(24);

        for (RentalOrder order : dataStore.orders.values()) {
            if ((order.getStatus() == OrderStatus.RENTING || order.getStatus() == OrderStatus.OVERDUE)
                    && order.getEndTime().isAfter(now)
                    && order.getEndTime().isBefore(threshold)) {
                expiringOrders.add(order);
            }
        }
        return expiringOrders;
    }

    public List<RentalOrder> getOverdueOrders() {
        List<RentalOrder> overdueOrders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (RentalOrder order : dataStore.orders.values()) {
            if (order.getStatus() == OrderStatus.RENTING && order.getEndTime().isBefore(now)) {
                overdueOrders.add(order);
            }
        }
        return overdueOrders;
    }

    public List<InventoryItem> getMaintenanceTimeoutItems() {
        List<InventoryItem> timeoutItems = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (InventoryItem item : dataStore.inventoryItems.values()) {
            if (item.getStatus() == ItemStatus.MAINTENANCE && item.getMaintenanceStart() != null) {
                LocalDateTime expectedEnd = item.getMaintenanceStart().plusDays(item.getMaintenanceDays());
                if (expectedEnd.isBefore(now)) {
                    timeoutItems.add(item);
                }
            }
        }
        return timeoutItems;
    }

    public void updateOverdueOrders() {
        List<RentalOrder> overdueOrders = getOverdueOrders();
        for (RentalOrder order : overdueOrders) {
            order.setStatus(OrderStatus.OVERDUE);
            LocalDateTime now = LocalDateTime.now();
            long overdueDays = Math.max(1, ChronoUnit.DAYS.between(order.getEndTime().toLocalDate(), now.toLocalDate()));
            double penalty = overdueDays * order.getDailyPrice() * order.getQuantity() * 1.5;
            order.setPenaltyAmount(penalty);
            order.setUpdatedAt(now);
        }
    }

    public void completeMaintenance(String itemId) {
        InventoryItem item = dataStore.inventoryItems.get(itemId);
        if (item == null) {
            throw new RuntimeException("库存物品不存在");
        }
        if (item.getStatus() != ItemStatus.MAINTENANCE) {
            throw new RuntimeException("物品不在维修状态");
        }
        item.setStatus(ItemStatus.AVAILABLE);
        item.setMaintenanceStart(null);
        item.setMaintenanceDays(0);
        item.setUpdatedAt(LocalDateTime.now());
    }
}