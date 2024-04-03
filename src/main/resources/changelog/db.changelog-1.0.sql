--liquibase formatted sql

--changeset nickz:1

-- Создание таблицы заказов
CREATE TABLE orders
(
    order_id   SERIAL PRIMARY KEY,
    order_date TIMESTAMP   NOT NULL,
    status     VARCHAR(32) NOT NULL
);

-- Создание таблицы детальной информации о заказе
CREATE TABLE order_details
(
    detail_id         SERIAL PRIMARY KEY,
    order_id          INT UNIQUE   NOT NULL,
    customer_name     VARCHAR(64) NOT NULL,
    order_description VARCHAR(255),
    customer_contact  VARCHAR(64),
    FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE
);

-- Создание таблицы продуктов
CREATE TABLE products
(
    product_id  SERIAL PRIMARY KEY,
    order_id    INT            NOT NULL,
    name        VARCHAR(64)   NOT NULL,
    description TEXT,
    price       DECIMAL(10, 2) NOT NULL,
    quantity    INT            NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE
);
-- Создание enum
CREATE TYPE order_status AS ENUM ('processing', 'completed', 'cancelled');
