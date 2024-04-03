package com.nickz.entity;

import lombok.Data;

@Data
public class OrderDetail {
    private int detailId;
    private int orderId;
    private String customerName;
    private String orderDescription;
    private String customerContact;
}