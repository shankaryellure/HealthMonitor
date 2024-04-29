package org.shankar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    private static Connection connection = null;

    // Synchronized method to handle multi-threaded access
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                String DB_URL = "jdbc:mysql://localhost:3306/health_monitoring?useSSL=false&serverTimezone=UTC";
                String DB_USER = "root";
                String DB_PASSWORD = "Tillu@122";
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connected!");
            } catch (SQLException e) {
                System.out.println("Error connecting to database: " + e.getMessage());
                throw e;  // Rethrow the exception to handle it outside
            }
        }
        return connection;
    }

    public static void logUpdate(String clientId, String healthCondition, int priorityLevel) throws SQLException {
        String sql = "INSERT INTO updates (client_id, health_condition, priority_level) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, clientId);
            stmt.setString(2, healthCondition);
            stmt.setInt(3, priorityLevel);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error logging update to database: " + e.getMessage());
            throw e;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.out.println("Error closing the database connection: " + e.getMessage());
            }
        }
    }
}
