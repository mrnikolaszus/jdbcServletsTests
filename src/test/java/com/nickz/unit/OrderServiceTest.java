package com.nickz.unit;

import com.nickz.dto.OrderCreateDto;
import com.nickz.dto.OrderDto;
import com.nickz.entity.Order;
import com.nickz.entity.OrderDetail;
import com.nickz.entity.OrderStatus;
import com.nickz.entity.Product;
import com.nickz.repository.OrderDetailRepository;
import com.nickz.repository.OrderRepository;
import com.nickz.repository.ProductRepository;
import com.nickz.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;


    @Test
    void getOrderById_ExistingOrder_ReturnsOrder() throws SQLException {
        int orderId = 1;
        Order mockOrder = new Order();
        mockOrder.setOrderId(orderId);
        mockOrder.setOrderDate(LocalDateTime.now());
        mockOrder.setStatus(OrderStatus.completed);
        OrderDetail mockOrderDetail = new OrderDetail();
        List<Product> mockProducts = new ArrayList<>();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderDetailRepository.findByOrderId(orderId)).thenReturn(mockOrderDetail);
        when(productRepository.findByOrderId(orderId)).thenReturn(mockProducts);
        OrderDto result = orderService.getOrderById(orderId);
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        verify(orderRepository).findById(orderId);
        verify(orderDetailRepository).findByOrderId(orderId);
        verify(productRepository).findByOrderId(orderId);
    }


    @Test
    void getAllOrders_WithValidOrders_ReturnsListOfOrders() throws SQLException {
        Order mockOrder = new Order();
        mockOrder.setOrderId(1);
        mockOrder.setOrderDate(LocalDateTime.now());
        mockOrder.setStatus(OrderStatus.completed);
        when(orderRepository.findById(1)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.findAll()).thenReturn(List.of(mockOrder));
        List<OrderDto> result = orderService.getAllOrders();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void createOrder_WithValidData_CreatesOrder() throws SQLException {
        OrderCreateDto orderCreateDto = new OrderCreateDto(OrderStatus.processing, new OrderDetail(), new ArrayList<>());
        doNothing().when(orderDetailRepository).create(any(OrderDetail.class));
        when(orderRepository.create(any(OrderCreateDto.class))).thenReturn(1);
        assertDoesNotThrow(() -> orderService.createOrder(orderCreateDto));
        verify(orderRepository).create(any(OrderCreateDto.class));
        verify(orderDetailRepository).create(any(OrderDetail.class));
        verify(productRepository, times(orderCreateDto.getProducts().size())).create(any(Product.class));
    }

    @Test
    void updateOrder_WithValidData_UpdatesOrder() throws SQLException {
        OrderDto orderDto = new OrderDto(1, LocalDateTime.now(), OrderStatus.processing, new OrderDetail(), new ArrayList<>());
        when(orderRepository.findById(orderDto.getOrderId())).thenReturn(Optional.of(new Order()));
        assertDoesNotThrow(() -> orderService.updateOrder(orderDto));
        verify(orderRepository).update(any(Order.class));
    }

    @Test
    void deleteOrder_WithValidId_DeletesOrder() throws SQLException {
        int orderId = 1;
        doNothing().when(orderRepository).delete(orderId);
        assertDoesNotThrow(() -> orderService.deleteOrder(orderId));
        verify(orderRepository).delete(orderId);
    }

}
