package com.rental.store;

import com.rental.entity.InventoryItem;
import com.rental.entity.Product;
import com.rental.entity.RentalOrder;
import com.rental.entity.User;
import com.rental.enums.CreditLevel;
import com.rental.enums.ItemStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataStore {

    public final Map<String, Product> products = new ConcurrentHashMap<>();
    public final Map<String, InventoryItem> inventoryItems = new ConcurrentHashMap<>();
    public final Map<String, User> users = new ConcurrentHashMap<>();
    public final Map<String, RentalOrder> orders = new ConcurrentHashMap<>();

    public String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    @PostConstruct
    public void initMockData() {
        initProducts();
        initInventory();
        initUsers();
    }

    private void initProducts() {
        Product p1 = new Product();
        p1.setId(generateId());
        p1.setName("单反相机");
        p1.setDescription("专业级单反相机，适合摄影爱好者");
        p1.setCategory("数码设备");
        p1.setDailyPrice(50.0);
        p1.setBaseDeposit(2000.0);
        p1.setTotalQuantity(5);
        p1.setCreatedAt(LocalDateTime.now());
        p1.setUpdatedAt(LocalDateTime.now());
        products.put(p1.getId(), p1);

        Product p2 = new Product();
        p2.setId(generateId());
        p2.setName("笔记本电脑");
        p2.setDescription("高性能商务笔记本");
        p2.setCategory("数码设备");
        p2.setDailyPrice(80.0);
        p2.setBaseDeposit(3000.0);
        p2.setTotalQuantity(3);
        p2.setCreatedAt(LocalDateTime.now());
        p2.setUpdatedAt(LocalDateTime.now());
        products.put(p2.getId(), p2);

        Product p3 = new Product();
        p3.setId(generateId());
        p3.setName("户外帐篷");
        p3.setDescription("4人露营帐篷，防水防风");
        p3.setCategory("户外用品");
        p3.setDailyPrice(30.0);
        p3.setBaseDeposit(500.0);
        p3.setTotalQuantity(10);
        p3.setCreatedAt(LocalDateTime.now());
        p3.setUpdatedAt(LocalDateTime.now());
        products.put(p3.getId(), p3);
    }

    private void initInventory() {
        int index = 1;
        for (Product product : products.values()) {
            for (int i = 0; i < product.getTotalQuantity(); i++) {
                InventoryItem item = new InventoryItem();
                item.setId(generateId());
                item.setProductId(product.getId());
                item.setSerialNumber("SN-" + String.format("%04d", index++));
                item.setStatus(ItemStatus.AVAILABLE);
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                inventoryItems.put(item.getId(), item);
            }
        }
    }

    private void initUsers() {
        User u1 = new User();
        u1.setId(generateId());
        u1.setName("张三");
        u1.setPhone("13800138001");
        u1.setCreditScore(750);
        u1.setCreditLevel(CreditLevel.HIGH);
        u1.setCreatedAt(LocalDateTime.now());
        u1.setUpdatedAt(LocalDateTime.now());
        users.put(u1.getId(), u1);

        User u2 = new User();
        u2.setId(generateId());
        u2.setName("李四");
        u2.setPhone("13800138002");
        u2.setCreditScore(600);
        u2.setCreditLevel(CreditLevel.MEDIUM);
        u2.setCreatedAt(LocalDateTime.now());
        u2.setUpdatedAt(LocalDateTime.now());
        users.put(u2.getId(), u2);

        User u3 = new User();
        u3.setId(generateId());
        u3.setName("王五");
        u3.setPhone("13800138003");
        u3.setCreditScore(400);
        u3.setCreditLevel(CreditLevel.LOW);
        u3.setCreatedAt(LocalDateTime.now());
        u3.setUpdatedAt(LocalDateTime.now());
        users.put(u3.getId(), u3);
    }

    public List<InventoryItem> getItemsByProductId(String productId) {
        List<InventoryItem> result = new ArrayList<>();
        for (InventoryItem item : inventoryItems.values()) {
            if (productId.equals(item.getProductId())) {
                result.add(item);
            }
        }
        return result;
    }

    public List<RentalOrder> getOrdersByProductId(String productId) {
        List<RentalOrder> result = new ArrayList<>();
        for (RentalOrder order : orders.values()) {
            if (productId.equals(order.getProductId())) {
                result.add(order);
            }
        }
        return result;
    }

    public List<RentalOrder> getActiveOrdersByProductId(String productId) {
        List<RentalOrder> result = new ArrayList<>();
        for (RentalOrder order : orders.values()) {
            if (productId.equals(order.getProductId()) &&
                    (order.getStatus() == com.rental.enums.OrderStatus.CONFIRMED ||
                            order.getStatus() == com.rental.enums.OrderStatus.RENTING ||
                            order.getStatus() == com.rental.enums.OrderStatus.OVERDUE)) {
                result.add(order);
            }
        }
        return result;
    }
}