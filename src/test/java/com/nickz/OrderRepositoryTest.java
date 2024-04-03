package com.nickz;


import com.nickz.entity.Order;
import com.nickz.entity.OrderStatus;
import com.nickz.repository.OrderRepository;

import com.nickz.util.ConnectionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;


@Testcontainers
public class OrderRepositoryTest extends IntegrationTestBase {


    private static MockedStatic<ConnectionManager> mockedConnectionManager;
    private static OrderRepository orderRepository;

    @BeforeAll
    static void setUp() throws SQLException {
        mockedConnectionManager = Mockito.mockStatic(ConnectionManager.class);
        String jdbcUrl = getJdbcUrl();
        String username = getUsername();
        String password = getPassword();

        Connection realConnection = DriverManager.getConnection(jdbcUrl, username, password);

        Connection mockConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    return method.invoke(realConnection, args);
                }
        );


        mockedConnectionManager.when(ConnectionManager::getConnect).thenReturn(mockConnection);

        orderRepository = new OrderRepository();
    }

    @AfterAll
    static void tearDown() {
        mockedConnectionManager.close();
    }

    @Test
    void testCreateOrder() throws SQLException {
        Order newOrder = new Order();
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus(OrderStatus.processing);

        var size = orderRepository.findAll().size();
        orderRepository.create(newOrder);
        List<Order> orders = orderRepository.findAll();
        var sizeAfter = orderRepository.findAll().size();
        Assertions.assertFalse(orders.isEmpty());
        Assertions.assertEquals(size + 1, sizeAfter);
    }

    @Test
    void testFindOrderById()  {
        orderRepository.findById(1).ifPresent(order -> {
            Assertions.assertEquals(1, order.getOrderId());
        });
    }

    @Test
    void testUpdateOrder(){
        orderRepository.findById(1).ifPresent(order -> {
                order.setStatus(OrderStatus.completed);
                orderRepository.update(order);
                Order updatedOrder = orderRepository.findById(1).get();
                Assertions.assertEquals(OrderStatus.completed, updatedOrder.getStatus());

        });
    }

    @Test
    void testDeleteOrder() throws SQLException {
        Order newOrder = new Order();
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus(OrderStatus.processing);
        int newOrderId = orderRepository.create(newOrder);

        orderRepository.delete(newOrderId);
        Assertions.assertTrue(orderRepository.findById(newOrderId).isEmpty());
    }



}