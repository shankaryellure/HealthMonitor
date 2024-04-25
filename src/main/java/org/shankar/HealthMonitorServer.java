package org.shankar;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class HealthMonitorServer {
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private Logger logger = Logger.getLogger("ServerLogger");
    private static int clientCount = 0;
    private final Object lock = new Object();

    public HealthMonitorServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            executorService = Executors.newCachedThreadPool();
            logger.info("Server started on port " + port);

            acceptClients();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server failed to start", e);
        }
    }

    private void acceptClients() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                int clientId = getNextClientId();
                logger.info("Client " + clientId + " connected: " + clientSocket);
                executorService.execute(new ClientHandler(clientSocket, clientId));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error accepting client connection", e);
            }
        }
    }

    private int getNextClientId() {
        synchronized (lock) {
            return ++clientCount; // Increment and return to ensure uniqueness
        }
    }

    public static void main(String[] args) {
        new HealthMonitorServer(54357); // Server port number
    }
}
