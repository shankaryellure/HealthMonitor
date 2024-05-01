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
    private Map<Socket, File> clientFiles = new HashMap<>();

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
            sendRandomDeviceDetails();
            processClientMessages();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing messages", e);
        } finally {
            try {
                clientFiles.remove(clientSocket);
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing client socket", e);
            }
        }
    }


    private void sendRandomDeviceDetails() throws IOException {
        File folder = new File("/Users/shankaryellure/Desktop/HealthMonitor/src/main/res/");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files != null && files.length > 0) {
            File file = files[new Random().nextInt(files.length)];
            clientFiles.put(clientSocket, file);
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = fileReader.readLine()) != null) {
                out.writeUTF(line);
            }
            out.writeUTF("EOF");
        } else {
            out.writeUTF("No device details available");
        }
    }

    private void processClientMessages() {
        try {
            while (true) {
                String encryptedMessage = in.readUTF();
                if ("EOF".equals(encryptedMessage)) {
                    break;
                }

                File file = clientFiles.get(clientSocket);
                if (file != null) {
                    String key = fetchEncryptionKey(file);
                    if (key != null) {
                        String decryptedMessage = decrypt(encryptedMessage, key);
                        logger.info("Decrypted message: " + decryptedMessage);

                        Map<String, String> messageMap = parseMessage(decryptedMessage);

                        String clientId = messageMap.get("Client ID");
                        String deviceId = messageMap.get("Device ID");
                        String deviceType = messageMap.get("Device Type");
                        String healthCondition = messageMap.get("Health Condition");
                        String priorityLevel = messageMap.get("Priority Level");

                        // Log to database
                        Database.logUpdate(clientId, deviceId, deviceType, healthCondition, priorityLevel);

                        // Check priority level
                        if ("Immediate".equalsIgnoreCase(priorityLevel)) {
                            System.out.println("ALERT: Immediate attention needed! - " + decryptedMessage);
                        }
                    } else {
                        logger.warning("No encryption key found for this client.");
                    }
                } else {
                    logger.warning("No record of the device details file sent to this client.");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO error in client communication", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error decrypting message", e);
        }
    }

    private Map<String, String> parseMessage(String decryptedMessage) {
        Map<String, String> data = new HashMap<>();
        String[] parts = decryptedMessage.split(", ");
        for (String part : parts) {
            String[] keyValue = part.split(": ", 2);
            if (keyValue.length == 2) {
                data.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return data;
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
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("key=")) {
                        return line.trim().substring(4);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
