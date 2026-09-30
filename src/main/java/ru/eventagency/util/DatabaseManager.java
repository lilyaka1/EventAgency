package ru.eventagency.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager() {
        url = env("DB_URL", "jdbc:postgresql://localhost:5432/event_agency");
        user = env("DB_USER", "postgres");
        password = env("DB_PASSWORD", "postgres");
    }

    private String env(String name, String fallback) {
        return System.getenv().getOrDefault(name, fallback);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
