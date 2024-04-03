package com.nickz;

import com.nickz.entity.Order;
import com.nickz.entity.OrderStatus;
import com.nickz.entity.Product;
import com.nickz.repository.OrderRepository;
import com.nickz.repository.ProductRepository;
import com.nickz.util.ConnectionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Testcontainers
public class ProductRepositoryTest extends IntegrationTestBase {

    private static MockedStatic<ConnectionManager> mockedConnectionManager;
    private static ProductRepository productRepository;
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

        productRepository = new ProductRepository();
        orderRepository = new OrderRepository();
    }

    @AfterAll
    static void tearDown() {
        mockedConnectionManager.close();
    }

    @Test
    void testCreateProduct() throws SQLException {
        Order newOrder = new Order();
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus(OrderStatus.processing);
        int newOrderId = orderRepository.create(newOrder);
        Product newProduct = new Product();
        newProduct.setOrderId(newOrderId);
        newProduct.setName("Test Product");
        newProduct.setDescription("Test Description");
        newProduct.setPrice(new BigDecimal("19.99"));
        newProduct.setQuantity(5);
        productRepository.create(newProduct);
        List<Product> products = productRepository.findAll();
        Assertions.assertFalse(products.isEmpty());

    }

    @Test
    void testFindProductById() throws SQLException {
        Product product = productRepository.findById(1);
        Assertions.assertNotNull(product);
        Assertions.assertEquals(1, product.getProductId());
    }

    @Test
    void testUpdateProduct() throws SQLException {
        Product product = productRepository.findById(5);
        Assertions.assertNotNull(product);
        product.setName("Updated Product");
        productRepository.update(product);
        Product updatedProduct = productRepository.findById(5);
        Assertions.assertEquals("Updated Product", updatedProduct.getName());
    }

    @Test
    void testDeleteProduct() throws SQLException {
        Order newOrder = new Order();
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus(OrderStatus.processing);
        int newOrderId = orderRepository.create(newOrder);
        Product newProduct = new Product();
        newProduct.setOrderId(newOrderId);
        newProduct.setName("Test Product");
        newProduct.setDescription("Test Description");
        newProduct.setPrice(new BigDecimal("19.99"));
        newProduct.setQuantity(5);
        int productId = productRepository.create(newProduct);
        productRepository.delete(productId);
        Product product = productRepository.findById(productId);
        Assertions.assertNull(product);
    }
}
