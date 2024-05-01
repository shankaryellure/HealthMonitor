package org.shankar;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Logger logger = Logger.getLogger("ClientHandlerLogger");

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
            // Existing message processing logic can continue here if needed.
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

    private void sendRandomDeviceDetails() throws IOException {
        File folder = new File("/Users/shankaryellure/Desktop/HealthMonitor/src/main/res/"); // Adjust path accordingly
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files != null && files.length > 0) {
            File file = files[new Random().nextInt(files.length)];
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = fileReader.readLine()) != null) {
                out.writeUTF(line); // Send each line of the file to the client
            }
            out.writeUTF("EOF"); // Signal end of the file transmission
        } else {
            out.writeUTF("No device details available");
        }
    }
}
