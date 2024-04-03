package com.nickz.service;

import com.nickz.dto.OrderDto;
import com.nickz.entity.Order;
import com.nickz.entity.OrderDetail;
import com.nickz.entity.Product;
import com.nickz.exception.*;
import com.nickz.repository.OrderDetailRepository;
import com.nickz.repository.OrderRepository;
import com.nickz.repository.ProductRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
    }

    public OrderDto getOrderById(int orderId) {
        return orderRepository.findById(orderId).map(order -> {
            List<Product> products;
            try {
                products = productRepository.findByOrderId(orderId);
            } catch (SQLException e) {
                throw new OrderNotFoundException("Failed to find products for order ID: " + orderId);
            }
            OrderDetail orderDetail;

            try {
                orderDetail = orderDetailRepository.findByOrderId(orderId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return new OrderDto(order.getOrderId(), order.getOrderDate(), order.getStatus(), orderDetail, products);
        }).orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
    }

    public List<OrderDto> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            List<OrderDto> orderDtos = new ArrayList<>();
            for (Order order : orders) {
                OrderDto orderDto = getOrderById(order.getOrderId());
                orderDtos.add(orderDto);
            }
            return orderDtos;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve all orders", e);
        }
    }

    public void createOrder(OrderDto orderDto) {
        try {
            Order order = new Order();
            order.setOrderDate(orderDto.getOrderDate());
            order.setStatus(orderDto.getStatus());
            int orderId = orderRepository.create(order);

            OrderDetail orderDetail = orderDto.getOrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetailRepository.create(orderDetail);

            List<Product> products = orderDto.getProducts();
            for (Product product : products) {
                product.setOrderId(orderId);
                productRepository.create(product);
            }
        } catch (SQLException e) {
            throw new OrderCreationException("Failed to create order", e);
        }
    }

    public void updateOrder(OrderDto orderDto) {
        try {
            Order order = new Order();
            order.setOrderId(orderDto.getOrderId());
            order.setOrderDate(orderDto.getOrderDate());
            order.setStatus(orderDto.getStatus());
            orderRepository.update(order);
            OrderDetail orderDetail = orderDto.getOrderDetail();
            orderDetail.setOrderId(orderDto.getOrderId());
            orderDetailRepository.update(orderDetail);
            List<Product> products = orderDto.getProducts();
            for (Product product : products) {
                product.setOrderId(orderDto.getOrderId());

                productRepository.update(product);
            }
        } catch (Exception e) {
            throw new OrderUpdateException("Failed to update order with ID: " + orderDto.getOrderId(), e);
        }
    }

    public void deleteOrder(int orderId) {
        try {
            orderRepository.delete(orderId);
        } catch (DatabaseOperationException  e) {
            throw new OrderDeletionException("Failed to delete order with ID: " + orderId, e);
        }
    }
}
