package ru.eventagency.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseManager {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager() {
        Properties properties = loadProperties();
        url = env("DB_URL", properties.getProperty("db.url", "jdbc:postgresql://localhost:5432/event_agency"));
        user = env("DB_USER", properties.getProperty("db.user", "postgres"));
        password = env("DB_PASSWORD", properties.getProperty("db.password", "postgres"));
    }

    private String env(String name, String fallback) {
        return System.getenv().getOrDefault(name, fallback);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseManager.class.getResourceAsStream("/database.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось загрузить database.properties", exception);
        }
        return properties;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
