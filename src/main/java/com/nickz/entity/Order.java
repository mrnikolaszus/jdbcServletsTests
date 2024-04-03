package com.nickz.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Order {
    private int orderId;
    private LocalDateTime orderDate;
    private OrderStatus status;
}