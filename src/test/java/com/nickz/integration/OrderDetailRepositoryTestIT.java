package com.nickz.integration;

import com.nickz.dto.OrderCreateDto;
import com.nickz.entity.OrderDetail;
import com.nickz.entity.OrderStatus;
import com.nickz.repository.OrderDetailRepository;
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
import java.util.List;

@Testcontainers
public class OrderDetailRepositoryTestIT extends IntegrationTestBase {

    private static MockedStatic<ConnectionManager> mockedConnectionManager;
    private static OrderDetailRepository orderDetailRepository;
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

        orderDetailRepository = new OrderDetailRepository();
        orderRepository = new OrderRepository();
    }

    @AfterAll
    static void tearDown() {
        mockedConnectionManager.close();
    }

    @Test
    void testCreateOrderDetail() throws SQLException {

        OrderCreateDto newOrder = new OrderCreateDto();
        newOrder.setStatus(OrderStatus.PROCESSING);
        int newOrderId = orderRepository.create(newOrder);
        OrderDetail newDetail = new OrderDetail();
        newDetail.setOrderId(newOrderId);
        newDetail.setCustomerName("Test Customer");
        newDetail.setOrderDescription("Test Description");
        newDetail.setCustomerContact("Test Contact");
        orderDetailRepository.create(newDetail);
        List<OrderDetail> details = orderDetailRepository.findAll();
        Assertions.assertFalse(details.isEmpty());
    }

    @Test
    void testFindOrderDetailById() throws SQLException {
        OrderDetail detail = orderDetailRepository.findById(5);
        Assertions.assertNotNull(detail);
        Assertions.assertEquals(5, detail.getDetailId());
    }

    @Test
    void testUpdateOrderDetail() throws SQLException {
        OrderDetail detail = orderDetailRepository.findById(6);
        Assertions.assertNotNull(detail);
        detail.setCustomerName("Updated Customer");
        orderDetailRepository.update(detail);
        OrderDetail updatedDetail = orderDetailRepository.findById(6);
        Assertions.assertEquals("Updated Customer", updatedDetail.getCustomerName());
    }

    @Test
    void testDeleteOrderDetail() throws SQLException {
        OrderCreateDto newOrder = new OrderCreateDto();
        newOrder.setStatus(OrderStatus.PROCESSING);
        int newOrderId = orderRepository.create(newOrder);

        OrderDetail newDetail = new OrderDetail();
        newDetail.setOrderId(newOrderId);
        newDetail.setCustomerName("Test Customer");
        newDetail.setOrderDescription("Test Description");
        newDetail.setCustomerContact("Test Contact");
        orderDetailRepository.create(newDetail);
        orderDetailRepository.delete(newOrderId);
        OrderDetail detail = orderDetailRepository.findById(newOrderId);
        Assertions.assertNull(detail);
    }
}
