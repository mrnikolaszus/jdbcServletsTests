package com.nickz.repository;

import com.nickz.entity.Order;
import com.nickz.entity.OrderStatus;
import com.nickz.exception.DatabaseOperationException;
import com.nickz.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository {


    public Optional<Order> findById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapToOrder(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching order with ID: " + orderId, e);
        }
        return Optional.empty();
    }

    public List<Order> findAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapToOrder(rs));
            }
        }
        return orders;
    }

    public int create(Order order) throws SQLException {
        String sql = "INSERT INTO orders (order_date, status) VALUES (?, ?::order_status) RETURNING order_id";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(order.getOrderDate()));
            stmt.setString(2, order.getStatus().name());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
    }

    public void update(Order order) throws DatabaseOperationException {
        String sql = "UPDATE orders SET order_date = ?, status = ?::order_status WHERE order_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(order.getOrderDate()));
            stmt.setString(2, order.getStatus().name());
            stmt.setInt(3, order.getOrderId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("No order found with ID: " + order.getOrderId());
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating order with ID: " + order.getOrderId(), e);
        }
    }

    public void delete(int orderId) throws DatabaseOperationException {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("No order found with ID: " + orderId);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting order with ID: " + orderId, e);
        }
    }

    private Order mapToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        return order;
    }
}