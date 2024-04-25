package org.shankar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    private static Connection connection = null;  // Declare the connection object

    public static Connection getConnection() {
        if (connection == null) {
            try {
                String DB_URL = "jdbc:mysql://localhost:3306/health_monitoring?useSSL=false&serverTimezone=UTC";
                String DB_USER = "root";
                String DB_PASSWORD = "Tillu@122";
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connected!");
            } catch (SQLException e) {
                System.out.println("Error connecting to database: " + e.getMessage());
            }
        }
        return connection;
    }

    public static void logUpdate(String clientId, String healthCondition, int priorityLevel) {
        String sql = "INSERT INTO updates (client_id, health_condition, priority_level) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, clientId);
            stmt.setString(2, healthCondition);
            stmt.setInt(3, priorityLevel);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error logging update to database: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.out.println("Error closing the database connection: " + e.getMessage());
            }
        }
    }
}
