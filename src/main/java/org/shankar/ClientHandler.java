package org.shankar;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Logger logger = Logger.getLogger("ClientHandlerLogger");
    private Map<Socket, File> clientFiles = new HashMap<>(); // Map to store client's socket and the file sent to it

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up client streams", e);
        }
    }

    @Override
    public void run() {
        try {
            File sentFile = sendRandomDeviceDetails();
            if (sentFile != null) {
                clientFiles.put(clientSocket, sentFile); // Record the file sent to this client
                processClientMessages();
            } else {
                logger.warning("No device details available to send.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing messages", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing client socket", e);
            }
        }
    }

    private File sendRandomDeviceDetails() throws IOException {
        File folder = new File("/Users/shankaryellure/Desktop/HealthMonitor/src/main/res/");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files != null && files.length > 0) {
            File file = files[new Random().nextInt(files.length)];
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = fileReader.readLine()) != null) {
                out.writeUTF(line);  // Send each line of the file to the client, including the key
            }
            out.writeUTF("EOF");  // Signal end of the file transmission
            return file; // Return the file sent to the client
        } else {
            out.writeUTF("No device details available");
            return null;
        }
    }

    private void processClientMessages() {
        try {
            while (true) {
                String encryptedMessage = in.readUTF(); // Read the encrypted message from the client
                if (encryptedMessage.equals("EOF")) {
                    break; // If EOF received, end the session
                }

                File file = clientFiles.get(clientSocket); // Get the file sent to this client
                if (file != null) {
                    String key = fetchEncryptionKey(file); // Fetch the encryption key from the file
                    logger.info("Encryption key fetched from file: " + key);

                    if (key != null) {
                        String decryptedMessage = decrypt(encryptedMessage, key);
                        logger.info("Decrypted message: " + decryptedMessage);

                        // Extract device ID from decrypted message
                        String[] parts = decryptedMessage.split(",");
                        String deviceId = parts[1].trim(); // Assuming device ID is at index 1

                        // Check if the priority level is "Immediate"
                        String priorityLevel = parts[4].trim(); // Assuming priority level is at index 4
                        if (priorityLevel.equalsIgnoreCase("Immediate")) {
                            // Print an alert message
                            logger.info("ALERT: Immediate Priority Detected!");
                        }
                    } else {
                        logger.warning("Encryption key not found in the .txt file.");
                    }
                } else {
                    logger.warning("No file sent to this client.");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO error in client communication", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error decrypting message", e);
        }
    }



    private String decrypt(String encryptedData, String base64Key) throws Exception {
        byte[] key = Base64.getDecoder().decode(base64Key);
        SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted);
    }

    private String fetchEncryptionKey(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("key=")) {
                    return line.trim().substring(4); // Extract the key value after "key="
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle file reading errors
        }
        return null;
    }
}
