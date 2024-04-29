package org.shankar;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import javax.crypto.Cipher;

public class MedicalDeviceClient {
    private Socket socket;
    private DataOutputStream dos;
    private String deviceType;
    private int deviceId;
    private Key encryptionKey;

    public MedicalDeviceClient(String host, int port, String deviceType, int deviceId, Key key) {
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.encryptionKey = key;
        try {
            this.socket = new Socket(host, port);
            this.dos = new DataOutputStream(socket.getOutputStream());
            sendInitialInfo();
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private void sendInitialInfo() {
        try {
            String message = "Device Type: " + deviceType + ", ID: " + deviceId;
            String encryptedMessage = encrypt(message, encryptionKey);
            dos.writeUTF(encryptedMessage);
            dos.flush();
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Failed to send encrypted data: " + e.getMessage());
        }
    }

    private String encrypt(String message, Key key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return new String(encryptedBytes); // Convert encrypted bytes to String for sending
    }
}
