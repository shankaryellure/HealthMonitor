package org.shankar;

import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataInputStream in;
    private Logger logger = Logger.getLogger("ClientHandlerLogger");

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            this.in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up client streams", e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (this.in.available() > 0) {
                    String message = in.readUTF();
                    String[] parts = message.split(",", 4);
                    if (parts.length != 4) {
                        logger.warning("Invalid message format received: " + message);
                        continue;
                    }
                    String clientId = parts[0].trim();
                    String deviceId = parts[1].trim();
                    String deviceType = parts[2].trim();
                    String encryptedData = parts[3].trim();

                    String key = getKey(deviceId);
                    if (key == null) {
                        logger.warning("Key not found for Device ID: " + deviceId);
                        continue;
                    }

                    String decryptedData = decrypt(encryptedData, key);
                    logger.info("Decrypted message from Client ID: " + clientId +  ", Device type: " + deviceType + ", Device ID: "+ deviceId + "," +decryptedData);

                    // Split the decrypted data to extract the priority and condition
                    String[] dataParts = decryptedData.split(";");
                    String healthCondition = dataParts[0].trim();
                    String priorityLevel = dataParts[1].trim().split(":")[1].trim(); // Get the priority description after colon

                    // Log to the database
                    Database.logUpdate(clientId, deviceId, healthCondition, priorityLevel);

                    // Check for immediate priority and display an alert if necessary
                    if ("Immediate".equalsIgnoreCase(priorityLevel)) {
                        // If running on a system with a GUI, you might use JOptionPane here; otherwise, log it differently
                        logger.severe("ALERT: Immediate attention needed for client " + clientId);
                        System.out.println("***** ALERT ***** Immediate attention needed for client: " + clientId + ", " + healthCondition);
                    }
                } else {
                    try {
                        Thread.sleep(100);  // Reduce CPU usage if no data is available
                    } catch (InterruptedException ie) {
                        logger.log(Level.WARNING, "Thread interrupted", ie);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing messages", e);
        }
    }

    private String getKey(String deviceId) throws IOException {
        File configFile = new File("/Users/shankaryellure/Desktop/HealthMonitor/src/main/res/" + deviceId + ".txt");
        if (!configFile.exists()) {
            logger.log(Level.SEVERE, "No configuration file found for device ID: " + deviceId);
            return null;
        }
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            prop.load(fis);
        }
        return prop.getProperty("key");
    }

    private String decrypt(String encryptedData, String base64Key) throws Exception {
        byte[] key = Base64.getDecoder().decode(base64Key);
        SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted);
    }
}
