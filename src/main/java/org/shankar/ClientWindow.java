package org.shankar;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Random;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


public class ClientWindow extends JFrame {
    private JLabel deviceNameLabel;
    private JLabel deviceIdLabel;
    private ButtonGroup conditionGroup;
    private ButtonGroup priorityGroup;
    private JButton sendButton;
    private Properties deviceConfig;
    private String configDirectory;
    private JPanel mainPanel;
    private String clientId;
    private Socket clientSocket;
    private DataOutputStream out;


    public ClientWindow(String configDirectory) {
        this.configDirectory = configDirectory;
        this.deviceConfig = new Properties();
        this.clientId = generateClientId();
        loadRandomConfiguration();
        initializeNetworkConnection();
        initializeUI();
    }

    private String generateClientId() {
        Random random = new Random();
        int num = random.nextInt(999999);
        return String.format("%06d", num); // Generates a 6 digit ID with leading zeros if necessary
    }

    private String encrypt(String data, String base64Key) throws Exception {
        byte[] key = Base64.getDecoder().decode(base64Key);  // Decode the base64 encoded key
        SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private void initializeNetworkConnection() {
        try {
            // Replace with your server's IP and port
            clientSocket = new Socket("10.0.0.20", 4321);
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Or handle reconnection logic
        }
    }

    private void initializeUI() {
        setTitle(String.format("Health Monitor Client with ID: %s", clientId));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Styling the main content panel
        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        getContentPane().add(mainPanel);

        // Styling the device labels
        deviceNameLabel = new JLabel("Device Type: " + deviceConfig.getProperty("deviceType"));
        deviceIdLabel = new JLabel("Device ID: " + deviceConfig.getProperty("deviceId"));
        deviceNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        deviceIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        deviceNameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        deviceIdLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(deviceNameLabel);
        mainPanel.add(deviceIdLabel);

        // Creating and styling radio button panels for health conditions and priorities
        JPanel conditionPanel = createRadioPanel("Health Conditions", deviceConfig.getProperty("healthConditions").split(";"), true);
        JPanel priorityPanel = createRadioPanel("Priority Levels", deviceConfig.getProperty("priorityLevels").split(","), false);

        // Styling and adding send button
        sendButton = new JButton("Send Update");
        sendButton.addActionListener(this::sendUpdate);
        sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Adding all elements to the main panel
        mainPanel.add(conditionPanel);
        mainPanel.add(priorityPanel);
        mainPanel.add(sendButton);

        pack(); // Adjust the window size based on its content
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    private JPanel createRadioPanel(String title, String[] options, boolean isCondition) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(title));

        ButtonGroup group = new ButtonGroup();
        for (String option : options) {
            JRadioButton button = new JRadioButton(option);
            button.setActionCommand(option);
            group.add(button);
            panel.add(button);
        }

        if (isCondition) {
            conditionGroup = group;
        } else {
            priorityGroup = group;
        }
        return panel;
    }

    private void loadRandomConfiguration() {
        File dir = new File(configDirectory);
        String[] configurations = dir.list((dir1, name) -> name.endsWith(".txt"));
        if (configurations == null || configurations.length == 0) {
            throw new IllegalStateException("No configuration files found.");
        }
        Random rand = new Random();
        String selectedConfig = configurations[rand.nextInt(configurations.length)];
        try (FileInputStream in = new FileInputStream(new File(configDirectory, selectedConfig))) {
            deviceConfig.load(in);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load device configuration: " + e.getMessage(), "Configuration Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void sendUpdate(ActionEvent event) {
        String condition = conditionGroup.getSelection().getActionCommand();
        String priority = priorityGroup.getSelection().getActionCommand();
        String dataToEncrypt = "Condition: " + condition + "; Priority: " + priority;

        try {
            String key = deviceConfig.getProperty("key"); // Ensure this is correctly fetched
            String encryptedData = encrypt(dataToEncrypt, key);
            String message = clientId + "," + deviceConfig.getProperty("deviceId") + "," + encryptedData;
            System.out.println("Sending message: " + message); // Log the message being sent
            out.writeUTF(message);
            out.flush();
            JOptionPane.showMessageDialog(this, "Update sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to encrypt or send update: " + e.getMessage(), "Encryption Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new ClientWindow("/Users/shankaryellure/Desktop/HealthMonitor/src/main/res/");
    }
}
