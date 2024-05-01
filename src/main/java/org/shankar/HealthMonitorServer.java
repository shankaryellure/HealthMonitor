package org.shankar;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HealthMonitorServer {
    private static final Logger logger = Logger.getLogger(HealthMonitorServer.class.getName());
    private static final int DEFAULT_PORT = 4321;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server listening on port " + port);

            Database.getConnection();
            logger.info("Database connected!");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected");
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server could not start", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection failed: " + e.getMessage(), e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Failed to close server socket", e);
                }
            }
        }
    }
}
