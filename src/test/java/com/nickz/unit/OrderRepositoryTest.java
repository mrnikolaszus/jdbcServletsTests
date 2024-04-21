package com.nickz.unit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.nickz.dto.OrderCreateDto;
import com.nickz.entity.Order;
import com.nickz.entity.OrderStatus;
import com.nickz.repository.OrderRepository;
import com.nickz.util.ConnectionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class OrderRepositoryTest {
    private OrderRepository repository;
    private MockedStatic<ConnectionManager> mockedConnectionManager;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws Exception {
        mockedConnectionManager = Mockito.mockStatic(ConnectionManager.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        mockedConnectionManager.when(ConnectionManager::getConnect).thenReturn(connection);
        repository = new OrderRepository();
    }

    @Test
    void create_Order_ShouldReturnGeneratedId() throws Exception {
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(100);
        OrderCreateDto orderCreateDto = new OrderCreateDto();
        orderCreateDto.setStatus(OrderStatus.PROCESSING);
        int generatedId = repository.create(orderCreateDto);
        assertEquals(100, generatedId);
        verify(preparedStatement).setString(2, orderCreateDto.getStatus().name());
    }

    @Test
    void update_ExistingOrder_ShouldUpdateSuccessfully() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        Order order = new Order();
        order.setOrderId(1);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.completed);
        assertDoesNotThrow(() -> repository.update(order));
        verify(preparedStatement).setInt(3, order.getOrderId());
        verify(preparedStatement).setTimestamp(1, Timestamp.valueOf(order.getOrderDate()));
        verify(preparedStatement).setString(2, order.getStatus().name());
    }

    @Test
    void delete_ExistingOrder_ShouldDeleteSuccessfully() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        assertDoesNotThrow(() -> repository.delete(1));
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void findById_ExistingOrder_ReturnsOrder() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("order_id")).thenReturn(1);
        when(resultSet.getTimestamp("order_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("status")).thenReturn(OrderStatus.PROCESSING.name());
        Optional<Order> result = repository.findById(1);
        assertTrue(result.isPresent());
        assertEquals(OrderStatus.PROCESSING, result.get().getStatus());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void findById_NonExistingOrder_ReturnsEmpty() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        Optional<Order> result = repository.findById(999);
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ReturnsListOfOrders() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("order_id")).thenReturn(1, 2);
        when(resultSet.getTimestamp("order_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("status")).thenReturn(OrderStatus.PROCESSING.name(), OrderStatus.completed.name());
        List<Order> result = repository.findAll();
        assertEquals(2, result.size());
    }


    @AfterEach
    void tearDown() {
        mockedConnectionManager.close();
    }

}