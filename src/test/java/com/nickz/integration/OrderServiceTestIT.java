package com.nickz.integration;

import com.nickz.dto.OrderCreateDto;
import com.nickz.dto.OrderDto;
import com.nickz.entity.OrderDetail;
import com.nickz.entity.OrderStatus;
import com.nickz.entity.Product;
import com.nickz.exception.OrderDeletionException;
import com.nickz.exception.OrderNotFoundException;
import com.nickz.exception.OrderUpdateException;
import com.nickz.repository.OrderDetailRepository;
import com.nickz.repository.OrderRepository;
import com.nickz.repository.ProductRepository;
import com.nickz.service.OrderService;
import com.nickz.util.ConnectionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OrderServiceTestIT extends IntegrationTestBase{


    private static MockedStatic<ConnectionManager> mockedConnectionManager;
    private static OrderDetailRepository orderDetailRepository;

    private static ProductRepository productRepository;
    private static OrderRepository orderRepository;

    private static OrderService orderService;

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

        productRepository = new ProductRepository();

        orderRepository = new OrderRepository();

        orderService = new OrderService(orderRepository, orderDetailRepository, productRepository);
    }

    @AfterAll
    static void tearDown() {
        mockedConnectionManager.close();
    }


    @Test
    void createOrder_ShouldPersistOrderWithDetailsAndProducts() {
        OrderCreateDto orderDto = setupCreateOrderDto();
        orderService.createOrder(orderDto);
    }

    @Test
    void getOrderById_and_updateOrderById_then_ShouldRetrieveOrderWithDetailsAndProducts() {

        int orderId = 5;
        OrderDto currentOrder = orderService.getOrderById(orderId);
        OrderDto newOrderData = setupOrderDto();
        newOrderData.setOrderId(orderId);
        currentOrder.setOrderDate(newOrderData.getOrderDate());
        currentOrder.setStatus(newOrderData.getStatus());
        currentOrder.setOrderDetail(newOrderData.getOrderDetail());
        currentOrder.setProducts(newOrderData.getProducts());
        orderService.updateOrder(currentOrder);
        OrderDto orderDto = orderService.getOrderById(orderId);
        assertNotNull(orderDto, "OrderDto should not be null");
        assertEquals(orderId, orderDto.getOrderId(), "Order ID should match the requested ID");
        assertNotNull(orderDto.getOrderDetail(), "Order details should not be null");
        Assertions.assertNotEquals("", orderDto.getOrderDetail().getCustomerName(), "Customer name in order details should not be empty");
        Assertions.assertNotEquals("", orderDto.getOrderDetail().getOrderDescription(), "Order description in order details should not be empty");
        assertNotNull(orderDto.getProducts(), "Products list should not be null");
        Assertions.assertFalse(orderDto.getProducts().isEmpty(), "Products list should not be empty");
        Product sampleProduct = orderDto.getProducts().get(0);
        assertNotNull(sampleProduct.getName(), "Product name should not be null");
        assertNotNull(sampleProduct.getDescription(), "Product description should not be null");
        assertNotNull(sampleProduct.getPrice(), "Product price should not be null");
        Assertions.assertTrue(sampleProduct.getQuantity() > 0, "Product quantity should be greater than 0");
    }

    @Test
    void updateOrder_ShouldCorrectlyUpdateOrderDetailsAndProducts() {
        int orderId = 5;
        OrderDto existingOrder = orderService.getOrderById(orderId);
        existingOrder.setStatus(OrderStatus.completed);
        existingOrder.getProducts().get(0).setName("Updated Product Name");
        orderService.updateOrder(existingOrder);
        OrderDto updatedOrder = orderService.getOrderById(orderId);
        assertEquals(OrderStatus.completed, updatedOrder.getStatus());
        assertEquals("Updated Product Name", updatedOrder.getProducts().get(0).getName());
    }

    @Test
    void deleteOrder_ShouldRemoveOrderAndItsDetailsAndProducts() {
        int orderId = 5;
        orderService.deleteOrder(orderId);
        Assertions.assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));
    }

    @Test
    void createOrderWithMultipleProducts_ShouldPersistAllProducts() {
        OrderCreateDto orderDto = setupCreateOrderDto();
        orderService.createOrder(orderDto);
        List<OrderDto> orders = orderService.getAllOrders();
        Assertions.assertTrue(orders.stream().anyMatch(o -> o.getProducts().size() == 2), "Order with two products should be persisted");
        OrderDto persistedOrder = orders.stream()
                .filter(o -> o.getProducts().size() == 2)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Order with two products not found"));
        assertEquals(2, persistedOrder.getProducts().size(), "Persisted order should have exactly two products");
        assertNotNull(persistedOrder.getOrderDetail(), "Order detail should not be null");
    }

    @Test
    void getAllOrders_ShouldReturnAllPersistedOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        Assertions.assertFalse(orders.isEmpty(), "Should return all persisted orders");
    }


    @Test
    void updateOrder_NonExistentOrder_ShouldThrowOrderUpdateException() {
        OrderDto nonExistentOrder = setupOrderDto();
        nonExistentOrder.setOrderId(999);
        Assertions.assertThrows(OrderUpdateException.class,
                () -> orderService.updateOrder(nonExistentOrder),
                "Should throw OrderUpdateException for a non-existent order"
        );
    }

    @Test
    void updateOrder_NonExistentOrder_ShouldThrowOrderNotFoundException() {
        OrderDto nonExistentOrder = setupOrderDto();
        Assertions.assertThrows(OrderUpdateException.class, () -> orderService.updateOrder(nonExistentOrder), "Should throw OrderNotFoundException for a non-existent order");
    }

    @Test
    void deleteOrder_NonExistentOrder_ShouldNotThrowException() {
        int nonExistentOrderId = 999;
        OrderDeletionException exception = Assertions.assertThrows(OrderDeletionException.class, () -> orderService.deleteOrder(nonExistentOrderId), "Expected OrderDeletionException to be thrown");
        Assertions.assertTrue(exception.getMessage().contains("Failed to delete order with ID: " + nonExistentOrderId), "Exception message should indicate no order found with the specified ID");
    }

    private OrderDto setupOrderDto() {
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderDate(LocalDateTime.now());
        orderDto.setStatus(OrderStatus.processing);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setCustomerName("Test name");
        orderDetail.setOrderDescription("Test order with multiple products");
        orderDetail.setCustomerContact("testmail@example.com");
        orderDto.setOrderDetail(orderDetail);

        Product product1 = new Product();
        product1.setName("Product A");
        product1.setDescription("Description for Product A");
        product1.setPrice(new BigDecimal("29.99"));
        product1.setQuantity(3);

        Product product2 = new Product();
        product2.setName("Product B");
        product2.setDescription("Description for Product B");
        product2.setPrice(new BigDecimal("39.99"));
        product2.setQuantity(4);

        orderDto.setProducts(List.of(product1, product2));

        return orderDto;
    }

    private OrderCreateDto setupCreateOrderDto() {
        OrderCreateDto orderCreateDto = new OrderCreateDto();
        orderCreateDto.setStatus(OrderStatus.processing);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setCustomerName("Test name");
        orderDetail.setOrderDescription("Test order with multiple products");
        orderDetail.setCustomerContact("testmail@example.com");
        orderCreateDto.setOrderDetail(orderDetail);

        Product product1 = new Product();
        product1.setName("Product A");
        product1.setDescription("Description for Product A");
        product1.setPrice(new BigDecimal("29.99"));
        product1.setQuantity(3);

        Product product2 = new Product();
        product2.setName("Product B");
        product2.setDescription("Description for Product B");
        product2.setPrice(new BigDecimal("39.99"));
        product2.setQuantity(4);

        orderCreateDto.setProducts(List.of(product1, product2));

        return orderCreateDto;
    }


}
