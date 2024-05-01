package org.shankar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class ClientWindow extends JFrame {
    private JLabel deviceNameLabel;
    private JLabel deviceIdLabel;
    private JPanel conditionPanel;
    private JPanel priorityPanel;
    private JButton sendButton;
    private JPanel mainPanel;
    private JScrollPane scrollPaneConditions;
    private JScrollPane scrollPanePriorities;
    private String clientId;
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    public ClientWindow() {
        this.clientId = generateClientId();
        initializeUI();
        initializeNetworkConnection();
        fetchConfigurationFromServer();
    }

    private String generateClientId() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    private void initializeNetworkConnection() {
        try {
            clientSocket = new Socket("10.111.118.73", 4321);
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        setTitle("Health Monitor Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        deviceNameLabel = new JLabel("Device Type: Not Set");
        deviceIdLabel = new JLabel("Device ID: Not Set");
        mainPanel.add(deviceNameLabel);
        mainPanel.add(deviceIdLabel);

        conditionPanel = new JPanel();
        conditionPanel.setLayout(new BoxLayout(conditionPanel, BoxLayout.Y_AXIS));
        conditionPanel.setBorder(BorderFactory.createTitledBorder("Health Conditions"));
        scrollPaneConditions = new JScrollPane(conditionPanel);
        scrollPaneConditions.setPreferredSize(new Dimension(480, 200));
        mainPanel.add(scrollPaneConditions);

        priorityPanel = new JPanel();
        priorityPanel.setLayout(new BoxLayout(priorityPanel, BoxLayout.Y_AXIS));
        priorityPanel.setBorder(BorderFactory.createTitledBorder("Priority Levels"));
        scrollPanePriorities = new JScrollPane(priorityPanel);
        scrollPanePriorities.setPreferredSize(new Dimension(480, 120));
        mainPanel.add(scrollPanePriorities);

        sendButton = new JButton("Send Update");
        sendButton.addActionListener(this::sendUpdate);
        mainPanel.add(sendButton);

        setSize(600, 600); // Increased window size
        setVisible(true);
    }

    private void fetchConfigurationFromServer() {
        try {
            Properties deviceDetails = new Properties();
            String line;
            while (!(line = in.readUTF()).equals("EOF")) {
                int delimiterIndex = line.indexOf('=');
                if (delimiterIndex != -1) {
                    String key = line.substring(0, delimiterIndex).trim();
                    String value = line.substring(delimiterIndex + 1).trim();
                    deviceDetails.setProperty(key, value);
                }
            }
            updateDeviceDetails(deviceDetails);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to receive device details: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateDeviceDetails(Properties details) {
        deviceNameLabel.setText("Device Type: " + details.getProperty("deviceType", "Not Set"));
        deviceIdLabel.setText("Device ID: " + details.getProperty("deviceId", "Not Set"));
        updateRadioButtons(conditionPanel, details.getProperty("healthConditions"), ";");
        updateRadioButtons(priorityPanel, details.getProperty("priorityLevels"), ",");
    }

    private void updateRadioButtons(JPanel panel, String data, String delimiter) {
        panel.removeAll();
        if (data != null) {
            ButtonGroup group = new ButtonGroup();
            String[] items = data.split(delimiter);
            for (String item : items) {
                JRadioButton button = new JRadioButton(item.trim());  // Creates button with trimmed item text
                group.add(button);
                panel.add(button);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void sendUpdate(ActionEvent event) {
        try {
            String message = "Response from client";
            out.writeUTF(clientId + "," + message);
            out.flush();
            JOptionPane.showMessageDialog(this, "Message sent successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to send message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new ClientWindow();
    }
}
