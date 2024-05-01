package org.shankar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private static Connection connection = null;
    private static final Logger logger = Logger.getLogger("DatabaseLogger");

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                String DB_URL = "jdbc:mysql://localhost:3306/health_monitoring?useSSL=false&serverTimezone=UTC";
                String DB_USER = "root";
                String DB_PASSWORD = "Tillu@122";
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                logger.info("Database connected!");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error connecting to database: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    public static void logUpdate(String clientId, String deviceId, String healthCondition, String priorityLevel) {
        String sql = "INSERT INTO updates (client_id, device_id, health_condition, priority_level) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, clientId);
            stmt.setString(2, deviceId);
            stmt.setString(3, healthCondition);
            stmt.setString(4, priorityLevel);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error logging update to database: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                logger.info("Database connection closed.");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing the database connection: " + e.getMessage());
            }
        }
    }
}
