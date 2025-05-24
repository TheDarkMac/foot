package com.fifa.analytics.DAO;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DataConnection {
    private final Dotenv dotenv = Dotenv.configure().directory("fifa-analytics").load();
    private final static int defaultPort = 5432;
    private final String user = dotenv.get("DATABASE_USER");
    private final String password = dotenv.get("DATABASE_PASSWORD");
    private final String jdbcUrl;

    public DataConnection() {
        String host = dotenv.get("DATABASE_HOST");
        String database = dotenv.get("DATABASE_NAME");
        jdbcUrl = "jdbc:postgresql://" + host + ":" + defaultPort + "/" + database;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
