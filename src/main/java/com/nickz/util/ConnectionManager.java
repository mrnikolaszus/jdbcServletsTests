package com.nickz.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "nickz";
    static final String PASSWORD = "password";

    private static final Integer POOL_SIZE = 10;

    private static  BlockingQueue<Connection> pool;
    private static List<Connection> closePool;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Не удалось загрузить драйвер PostgreSQL", e);
        }
        
        initConnectionPool();
    }

    private ConnectionManager() {

    }

    private static void initConnectionPool() {

        if (System.getProperty("app.mode").equals("test")) {
         return;
        }

        pool = new ArrayBlockingQueue<>(POOL_SIZE);
        closePool = new ArrayList<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            var connection = open();
            var proxyConnection = (Connection) Proxy.newProxyInstance(ConnectionManager.class.getClassLoader(), new Class[]{Connection.class},
                    (proxy, method, args) -> method.getName().equals("close")
                            ? pool.add((Connection) proxy) : method.invoke(connection, args));
            pool.add(proxyConnection);
            closePool.add(connection);
        }
    }

    public static Connection getConnect(){

        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closePool(){
        for (Connection connection : closePool) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static Connection open()  {

        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
