package com.rental.service;

import com.rental.entity.InventoryItem;
import com.rental.entity.RentalOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private RentalService rentalService;

    @Scheduled(cron = "0 0 * * * ?")
    public void scanExpiringOrders() {
        List<RentalOrder> expiringOrders = rentalService.getExpiringOrders();
        if (!expiringOrders.isEmpty()) {
            logger.info("扫描到 {} 个即将到期的订单", expiringOrders.size());
            for (RentalOrder order : expiringOrders) {
                logger.info("订单 {} 即将到期，用户: {}, 结束时间: {}",
                        order.getId(), order.getUserId(), order.getEndTime());
            }
        }
    }

    @Scheduled(cron = "0 30 * * * ?")
    public void scanOverdueOrders() {
        rentalService.updateOverdueOrders();
        List<RentalOrder> overdueOrders = rentalService.getOverdueOrders();
        if (!overdueOrders.isEmpty()) {
            logger.info("扫描到 {} 个逾期订单", overdueOrders.size());
            for (RentalOrder order : overdueOrders) {
                logger.info("逾期订单 {}: 用户 {}, 应归还时间: {}, 违约金: {}",
                        order.getId(), order.getUserId(), order.getEndTime(), order.getPenaltyAmount());
            }
        }
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void scanMaintenanceTimeout() {
        List<InventoryItem> timeoutItems = rentalService.getMaintenanceTimeoutItems();
        if (!timeoutItems.isEmpty()) {
            logger.info("扫描到 {} 个维修超时的物品", timeoutItems.size());
            for (InventoryItem item : timeoutItems) {
                logger.info("维修超时物品 {}: 序列号 {}, 商品ID {}",
                        item.getId(), item.getSerialNumber(), item.getProductId());
            }
        }
    }
}