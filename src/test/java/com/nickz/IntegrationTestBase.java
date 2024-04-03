package com.nickz;

import com.nickz.util.LiquibaseInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
public abstract class IntegrationTestBase {

    @Container
    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:14.1")
            .withDatabaseName("postgres")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    public static void setup() {
        System.setProperty("app.mode", "test");
    }
    @BeforeAll
    static void runContainer() {
        container.start();

        LiquibaseInitializer.runLiquibaseTest(container.getJdbcUrl(), container.getUsername(), container.getPassword());


    }

    @AfterAll
    static void tearDown() {
        container.stop();
    }


    public static String getJdbcUrl() {
        return container.getJdbcUrl();
    }


    protected static String getUsername() {
        return container.getUsername();
    }

    protected static String getPassword() {
        return container.getPassword();
    }
}