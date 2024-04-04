package com.nickz.unit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.nickz.entity.OrderDetail;
import com.nickz.repository.OrderDetailRepository;
import com.nickz.util.ConnectionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ExtendWith(MockitoExtension.class)
public class OrderDetailRepositoryTest {
    private OrderDetailRepository repository;
    private MockedStatic<ConnectionManager> mockedConnectionManager;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        mockedConnectionManager = Mockito.mockStatic(ConnectionManager.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        mockedConnectionManager.when(ConnectionManager::getConnect).thenReturn(connection);

        repository = new OrderDetailRepository();
    }

    @Test
    void findById_ExistingOrderDetail_ReturnsOrderDetail() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("detail_id")).thenReturn(1);
        when(resultSet.getString("customer_name")).thenReturn("test name");
        when(resultSet.getString("order_description")).thenReturn("test order");
        when(resultSet.getString("customer_contact")).thenReturn("test@example.com");

        OrderDetail result = repository.findById(1);

        assertNotNull(result);
        assertEquals("test name", result.getCustomerName());
        assertEquals("test order", result.getOrderDescription());
        assertEquals("test@example.com", result.getCustomerContact());
    }

    @Test
    void findById_NonExistingOrderDetail_ReturnsNull() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        OrderDetail result = repository.findById(999);

        assertNull(result);
    }

    @Test
    void create_OrderDetail_SuccessfullyCreates() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(1);
        detail.setCustomerName("test name");
        detail.setOrderDescription("New order");
        detail.setCustomerContact("test@example.com");

        assertDoesNotThrow(() -> repository.create(detail));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update_ExistingOrderDetail_SuccessfullyUpdates() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        OrderDetail detail = new OrderDetail();
        detail.setDetailId(1);
        detail.setOrderId(1);
        detail.setCustomerName("test name Updated");
        detail.setOrderDescription("Updated order");
        detail.setCustomerContact("test@example.com");

        assertDoesNotThrow(() -> repository.update(detail));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void delete_ExistingOrderDetail_SuccessfullyDeletes() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        assertDoesNotThrow(() -> repository.delete(1));
        verify(preparedStatement).executeUpdate();
    }

    @AfterEach
    void tearDown() {
        mockedConnectionManager.close();
    }
}
