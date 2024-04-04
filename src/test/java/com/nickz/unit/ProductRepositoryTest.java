package com.nickz.unit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.nickz.entity.Product;
import com.nickz.repository.ProductRepository;
import com.nickz.util.ConnectionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ProductRepositoryTest {

    private ProductRepository repository;
    private MockedStatic<ConnectionManager> mockedConnectionManager;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        mockedConnectionManager = mockStatic(ConnectionManager.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        mockedConnectionManager.when(ConnectionManager::getConnect).thenReturn(connection);
        repository = new ProductRepository();
    }

    @Test
    void findById_ExistingProduct_ReturnsProduct() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("product_id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Test Product");
        when(resultSet.getString("description")).thenReturn("Test Description");
        when(resultSet.getBigDecimal("price")).thenReturn(new BigDecimal("19.99"));
        when(resultSet.getInt("order_id")).thenReturn(1);
        when(resultSet.getInt("quantity")).thenReturn(10);

        Product result = repository.findById(1);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(new BigDecimal("19.99"), result.getPrice());
        assertEquals(10, result.getQuantity());
    }

    @Test
    void findById_NonExistingProduct_ReturnsNull() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Product result = repository.findById(999);

        assertNull(result);
    }

    @Test
    void create_Product_ShouldReturnGeneratedId() throws SQLException {
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(100);

        Product product = new Product();
        product.setName("New Product");
        product.setDescription("New Description");
        product.setPrice(new BigDecimal("29.99"));
        product.setQuantity(5);

        int generatedId = repository.create(product);

        assertEquals(100, generatedId);
        verify(preparedStatement).setString(2, product.getName());
        verify(preparedStatement).setString(3, product.getDescription());
    }

    @Test
    void update_ExistingProduct_ShouldUpdateSuccessfully() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Product product = new Product();
        product.setProductId(1);
        product.setName("Updated Product");
        product.setDescription("Updated Description");
        product.setPrice(new BigDecimal("39.99"));
        product.setQuantity(15);

        assertDoesNotThrow(() -> repository.update(product));
        verify(preparedStatement).setInt(6, product.getProductId());
    }

    @Test
    void delete_ExistingProduct_ShouldDeleteSuccessfully() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> repository.delete(1));
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void findByOrderId_ExistingOrder_ReturnsListOfProducts() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("product_id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("Product 1", "Product 2");
        when(resultSet.getString("description")).thenReturn("Description 1", "Description 2");
        when(resultSet.getBigDecimal("price")).thenReturn(new BigDecimal("19.99"), new BigDecimal("29.99"));
        when(resultSet.getInt("order_id")).thenReturn(1);
        when(resultSet.getInt("quantity")).thenReturn(10, 20);

        List<Product> result = repository.findByOrderId(1);

        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());
    }

    @AfterEach
    void tearDown() {
        mockedConnectionManager.close();
    }
}
