package com.nickz.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Product {
    private int productId;
    private int orderId;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
}