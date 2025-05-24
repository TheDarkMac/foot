package com.fifa.app.dataSource;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DataSource {

    protected String url;
    protected String user;
    protected String password;
    protected int port;
    protected String database;

    // Constructeur pour initialiser les variables avec les valeurs par défaut
    public DataSource() {
        this.url = "jdbc:postgresql://localhost:5432/jean_foot?sslmode=disable";
        this.user = "jean_foot";
        this.password = "jeanfoot";
        this.port = 5432;
        this.database = "jean_foot";
    }

    // Si vous voulez utiliser des variables d'environnement pour la connexion
    public DataSource(String url, String user, String password, int port, String database) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.port = port;
        this.database = database;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
