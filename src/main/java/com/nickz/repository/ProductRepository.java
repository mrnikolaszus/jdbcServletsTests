package com.nickz.repository;

import com.nickz.entity.Product;
import com.nickz.exception.DatabaseOperationException;
import com.nickz.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public Product findById(int productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapToProduct(rs);
            }
        }
        return null;
    }

    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapToProduct(rs));
            }
        }
        return products;
    }

    public int create(Product product) throws SQLException {
        String sql = "INSERT INTO products (order_id, name, description, price, quantity) VALUES (?, ?, ?, ?, ?) RETURNING product_id";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, product.getOrderId());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getQuantity());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }
        }
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET order_id = ?, name = ?, description = ?, price = ?, quantity = ? WHERE product_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, product.getOrderId());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getQuantity());
            stmt.setInt(6, product.getProductId());
            stmt.executeUpdate();
        }
    }

    public void delete(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }

    public List<Product> findByOrderId(int orderId) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE order_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapToProduct(rs));
            }
        }
        return products;
    }
    public void deleteByOrderId(int orderId) throws DatabaseOperationException {
        String sql = "DELETE FROM products WHERE order_id = ?";
        try (Connection conn = ConnectionManager.getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("No products found with order ID: " + orderId);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting products with order ID: " + orderId, e);
        }
    }

    private Product mapToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setOrderId(rs.getInt("order_id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setQuantity(rs.getInt("quantity"));
        return product;
    }
}
