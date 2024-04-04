package com.nickz.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nickz.dto.OrderCreateDto;
import com.nickz.dto.OrderDto;
import com.nickz.controllers.error.ErrorResponse;
import com.nickz.exception.OrderDeletionException;
import com.nickz.exception.OrderNotFoundException;
import com.nickz.repository.OrderDetailRepository;
import com.nickz.repository.OrderRepository;
import com.nickz.repository.ProductRepository;
import com.nickz.service.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/orders/*")
public class OrderController extends HttpServlet {

    private OrderService orderService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        this.orderService = new OrderService(new OrderRepository(), new OrderDetailRepository(), new ProductRepository());
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(status, message);
        resp.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            List<OrderDto> orders = orderService.getAllOrders();
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(orders));
        } else {
            String[] splits = pathInfo.split("/");
            if (splits.length != 2) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }
            try {
                int orderId = Integer.parseInt(splits[1]);
                OrderDto orderDto = orderService.getOrderById(orderId);
                resp.setContentType("application/json");
                resp.getWriter().write(objectMapper.writeValueAsString(orderDto));
            } catch (OrderNotFoundException e) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID format");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            OrderCreateDto orderCreateDto = objectMapper.readValue(req.getReader(), OrderCreateDto.class);
            orderService.createOrder(orderCreateDto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "URL must include order ID");
            return;
        }
        String[] splits = pathInfo.split("/");
        if (splits.length != 2) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            return;
        }
        try {
            int orderId = Integer.parseInt(splits[1]);
            OrderDto orderDtoToUpdate = objectMapper.readValue(req.getReader(), OrderDto.class);
            orderDtoToUpdate.setOrderId(orderId);
            orderService.updateOrder(orderDtoToUpdate);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID format");
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "URL must include order ID");
            return;
        }
        String[] splits = pathInfo.split("/");
        if (splits.length != 2) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            return;
        }
        try {
            int orderId = Integer.parseInt(splits[1]);
            orderService.deleteOrder(orderId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID format");
        } catch (OrderDeletionException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
