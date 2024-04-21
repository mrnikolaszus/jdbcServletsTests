package com.nickz.repository;

import com.nickz.entity.OrderDetail;
import com.nickz.exception.DatabaseOperationException;
import com.nickz.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailRepository {

    public OrderDetail findById(int detailId) throws SQLException {
        String sql = "SELECT * FROM order_details WHERE detail_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detailId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapToOrderDetail(rs);
            }
        }
        return null;
    }

    public List<OrderDetail> findAll() throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM order_details";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                details.add(mapToOrderDetail(rs));
            }
        }
        return details;
    }

    public void create(OrderDetail detail) throws SQLException {
        String sql = "INSERT INTO order_details (order_id, customer_name, order_description, customer_contact) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detail.getOrderId());
            stmt.setString(2, detail.getCustomerName());
            stmt.setString(3, detail.getOrderDescription());
            stmt.setString(4, detail.getCustomerContact());
            stmt.executeUpdate();
        }
    }

    public void update(OrderDetail detail) throws SQLException {
        String sql = "UPDATE order_details SET order_id = ?, customer_name = ?, order_description = ?, customer_contact = ? WHERE detail_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detail.getOrderId());
            stmt.setString(2, detail.getCustomerName());
            stmt.setString(3, detail.getOrderDescription());
            stmt.setString(4, detail.getCustomerContact());
            stmt.setInt(5, detail.getDetailId());
            stmt.executeUpdate();
        }
    }

    public void delete(int detailId) throws SQLException {
        String sql = "DELETE FROM order_details WHERE detail_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detailId);
            stmt.executeUpdate();
        }
    }

    public OrderDetail findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM order_details WHERE order_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapToOrderDetail(rs);
            }
        }
        return null;
    }

    public void deleteByOrderId(int orderId) throws DatabaseOperationException {
        String sql = "DELETE FROM order_details WHERE order_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("No order details found with order_id: " + orderId);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting order details with order_id: " + orderId, e);
        }
    }

    private OrderDetail mapToOrderDetail(ResultSet rs) throws SQLException {
        OrderDetail detail = new OrderDetail();
        detail.setDetailId(rs.getInt("detail_id")); // можно было бы прямо в маппер вынести, но это не обязательно
        detail.setOrderId(rs.getInt("order_id"));
        detail.setCustomerName(rs.getString("customer_name"));
        detail.setOrderDescription(rs.getString("order_description"));
        detail.setCustomerContact(rs.getString("customer_contact"));
        return detail;
    }
}

