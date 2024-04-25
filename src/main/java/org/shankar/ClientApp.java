package org.shankar;
import java.io.*;
import java.net.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Base64;

public class ClientApp {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String clientId;
    private String category;
    private String sharedKey = "your_key_here"; // Replace with your encryption key

    public ClientApp(String clientId, String category) {
        this.clientId = clientId;
        this.category = category;
    }

    public void connectToServer(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            // Start a separate thread to listen for updates from the server
            Thread serverListenerThread = new Thread(new ServerListener());
            serverListenerThread.start();
        } catch (IOException e) {
            handleException(e);
        }
    }

    public void sendUpdate(String healthCondition, int priorityLevel) {
        try {
            // Encrypt the message before sending
            String encryptedMessage = encryptMessage(healthCondition, sharedKey);

            PatientUpdate message = new PatientUpdate(clientId, category, encryptedMessage, priorityLevel);
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            handleException(e);
        }
    }

    private String encryptMessage(String message, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    private String decryptMessage(String encryptedMessage, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    // Listen for updates from the server
                    Object receivedObject = inputStream.readObject();
                    if (receivedObject instanceof String) {
                        String decryptedMessage = decryptMessage((String) receivedObject, sharedKey);
                        System.out.println("Received update from server: " + decryptedMessage);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                handleException(e);
            }
        }
    }

    private void handleException(Exception e) {
        // Handle exceptions (e.g., log, display error message, etc.)
        e.printStackTrace();
    }

    public static void main(String[] args) {
        String clientId = "Client1"; // Replace with actual client ID
        String category = "Pacemaker"; // Replace with actual category

        ClientApp client = new ClientApp(clientId, category);
        client.connectToServer("localhost", 54356); // Replace with server address and port
        // Example usage: client.sendUpdate("High Blood Pressure", 5);
    }
}
