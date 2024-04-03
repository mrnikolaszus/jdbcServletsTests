package com.nickz.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import java.sql.Connection;
import java.sql.DriverManager;

@WebListener
public class LiquibaseInitializer implements ServletContextListener {
    private static final String CHANGELOG_FILE = "changelog/db.changelog-master.yaml";
    private static final String CHANGELOG_FILE_TEST = "changelog/db.changelog-master-test.yaml";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        runLiquibase();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionManager.closePool();
        System.out.println("Connection pool has been closed.");
    }

    public static void runLiquibase() {
        try (Connection connection = ConnectionManager.getConnect()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new liquibase.Liquibase(CHANGELOG_FILE, new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new liquibase.Contexts(), new liquibase.LabelExpression());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runLiquibaseTest(String JDBC_URL, String USER, String PASSWORD) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new liquibase.Liquibase(CHANGELOG_FILE_TEST, new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new liquibase.Contexts(), new liquibase.LabelExpression());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка запуска Liquibase", e);
        }
    }
}
