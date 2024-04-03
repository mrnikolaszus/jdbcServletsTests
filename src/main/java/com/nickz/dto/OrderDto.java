package com.nickz.dto;

import com.nickz.entity.OrderDetail;
import com.nickz.entity.OrderStatus;
import com.nickz.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private int orderId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private OrderDetail orderDetail;
    private List<Product> products;


}