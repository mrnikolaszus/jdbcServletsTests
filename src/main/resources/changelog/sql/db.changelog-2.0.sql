--liquibase formatted sql

--changeset nickz:1

--orders
INSERT INTO orders (order_date, status)
VALUES
    (NOW(), 'processing'),
    (NOW(), 'completed'),
    (NOW(), 'cancelled'),
    (NOW(), 'processing'),
    (NOW(), 'completed'),
    (NOW(), 'cancelled'),
    (NOW(), 'processing'),
    (NOW(), 'completed'),
    (NOW(), 'cancelled'),
    (NOW(), 'processing'),
    (NOW(), 'completed'),
    (NOW(), 'cancelled'),
    (NOW(), 'processing'),
    (NOW(), 'completed'),
    (NOW(), 'cancelled'),
    (NOW(), 'processing'),
    (NOW(), 'completed'),
    (NOW(), 'cancelled'),
    (NOW(), 'processing'),
    (NOW(), 'completed');


--order_details
INSERT INTO order_details (order_id, customer_name, order_description, customer_contact)
SELECT order_id,
       'customer' || order_id,
       'description for order ' || order_id,
       'contact' || order_id
FROM orders;


--products
INSERT INTO products (order_id, name, description, price, quantity)
SELECT o.order_id,
       'product' || o.order_id || '_' || p.id,
       'description for product' || o.order_id || '_' || p.id,
       (RANDOM() * (100 - 10 + 1) + 10)::DECIMAL(10,2),
       (RANDOM() * (10 - 1 + 1) + 1)::INT
FROM orders o
         CROSS JOIN generate_series(1, 3) AS p(id);

