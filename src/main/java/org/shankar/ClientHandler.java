package org.shankar;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private int clientId;  // Use int for clientId
    private Logger logger = Logger.getLogger("ClientHandlerLogger");

    public ClientHandler(Socket socket, int clientId) {
        this.clientSocket = socket;
        this.clientId = clientId;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream.writeObject(clientId); // Send the client ID back to the client
            outputStream.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up client streams or sending client ID", e);
        }
    }

    @Override
    public void run() {
        try {
            // Process messages from client
            processClient();
        } finally {
            closeConnection();
        }
    }

    private void processClient() {
        try {
            while (true) {
                try {
                    Object receivedObject = inputStream.readObject();
                    if (receivedObject instanceof PatientUpdate) {
                        PatientUpdate patientUpdate = (PatientUpdate) receivedObject;
                        // Process the received update here
                    }
                } catch (EOFException eof) {
                    logger.info("EOFException: Client " + clientId + " has closed the connection.");
                    break; // Exit the loop as client has closed the connection
                } catch (IOException | ClassNotFoundException ex) {
                    logger.log(Level.SEVERE, "Error processing messages for client " + clientId, ex);
                    break; // Exit on other exceptions
                }
            }
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            inputStream.close();
            outputStream.close();
            clientSocket.close();
            logger.info("Connection with client " + clientId + " closed");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing connection with client " + clientId, e);
        }
    }
}
