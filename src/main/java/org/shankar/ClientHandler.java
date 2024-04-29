package org.shankar;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.Key;
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
                    String encryptedMessage = in.readUTF();
                    String key = "your_decryption_key_here"; // This should be securely fetched or passed
                    String decryptedMessage = decrypt(encryptedMessage, key);
                    // Extract client ID from the decrypted message
                    String[] messageParts = decryptedMessage.split(", ");
                    String clientId = messageParts[0].split(": ")[1];
                    logger.info("Decrypted message from Client ID " + clientId + ": " + decryptedMessage);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to decrypt message: " + e.getMessage());
        }
    }

    private String decrypt(String encryptedData, String key) throws Exception {
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted);
    }
}
