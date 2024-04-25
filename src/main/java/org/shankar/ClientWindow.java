package org.shankar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientWindow {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String clientId;
    private JFrame mainFrame;
    private JTextArea logTextArea;
    private JPanel centerPanel;
    private ButtonGroup healthConditionGroup = new ButtonGroup();
    private ButtonGroup priorityLevelGroup = new ButtonGroup();
    private JButton sendButton;

    public ClientWindow(String clientId) {
        this.clientId = clientId;
        initialize();
    }

    private void initialize() {
        mainFrame = new JFrame("Select Device");
        mainFrame.setSize(700, 500);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        String[] devices = {"Pacemaker", "Pulse Monitor", "SpO2 Monitor", "Glucose Meter", "Temperature Reader"};
        for (String device : devices) {
            JButton deviceButton = new JButton(device);
            deviceButton.addActionListener(e -> openDeviceDetailWindow(device));
            centerPanel.add(deviceButton);
        }

        mainFrame.add(centerPanel, BorderLayout.CENTER);

        logTextArea = new JTextArea(5, 20);
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        mainFrame.add(scrollPane, BorderLayout.SOUTH);

        mainFrame.setVisible(true);
    }

    private void openDeviceDetailWindow(String device) {
        mainFrame.setTitle("Patient Monitoring Client - " + device);
        centerPanel.setVisible(false);

        // Setup health conditions radio buttons
        JPanel conditionPanel = new JPanel(new GridLayout(0, 1));
        JScrollPane conditionScrollPane = new JScrollPane(conditionPanel);
        conditionScrollPane.setBorder(BorderFactory.createTitledBorder("Health Conditions"));

        // Setup priority levels radio buttons
        JPanel priorityPanel = new JPanel(new GridLayout(0, 1));
        JScrollPane priorityScrollPane = new JScrollPane(priorityPanel);
        priorityScrollPane.setBorder(BorderFactory.createTitledBorder("Priority Levels"));

        // Setup the send button
        sendButton = new JButton("Send Update");
        sendButton.setEnabled(false); // Initially disabled

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, conditionScrollPane, priorityScrollPane);
        splitPane.setResizeWeight(0.5);

        // Add health conditions to ButtonGroup and panel
        String[] conditions = getConditionsForDevice(device);
        for (String condition : conditions) {
            JRadioButton conditionButton = new JRadioButton(condition);
            healthConditionGroup.add(conditionButton);
            conditionPanel.add(conditionButton);
            conditionButton.addActionListener(e -> updateSendButtonState());
        }

        // Add priority levels to ButtonGroup and panel
        String[] priorities = {"Routine", "Moderate", "Urgent", "Critical", "Immediate"};
        for (String priority : priorities) {
            JRadioButton priorityButton = new JRadioButton(priority);
            priorityLevelGroup.add(priorityButton);
            priorityPanel.add(priorityButton);
            priorityButton.addActionListener(e -> updateSendButtonState());
        }

        sendButton = new JButton("Send Update"); // Use the class member
        sendButton.setEnabled(false); // Initially, the button is disabled
        sendButton.addActionListener(e -> {
            System.out.println("Update sent for device: " + device);
            resetUI();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);

        mainFrame.getContentPane().removeAll();
        mainFrame.add(splitPane, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);

        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setTitle("Patient Monitoring Client - " + device + " - Client ID: " + clientId);

    }

    private void updateSendButtonState() {
        sendButton.setEnabled(isHealthConditionSelected() && isPriorityLevelSelected());
    }
    // Helper method to check if a health condition is selected
    private boolean isHealthConditionSelected() {
        return healthConditionGroup.getSelection() != null;
    }

    // Helper method to check if a priority level is selected
    private boolean isPriorityLevelSelected() {
        return priorityLevelGroup.getSelection() != null;
    }


    private String[] getConditionsForDevice(String device) {
        switch (device) {
            case "Pacemaker":
                return new String[]{
                        "Irregular Heartbeat (Arrhythmia) - Heartbeat is not regular",
                        "Slow Heart Rate (Bradycardia) - Heart beats too slowly",
                        "Fast Heart Rate (Tachycardia) - Heart beats too quickly",
                        "Heartbeat Out of Sync (Arrhythmia) - Heartbeat is irregular or abnormal",
                        "Blocked Heart Signals (Heart Block) - Signals between heart chambers are blocked"
                };
            case "Pulse Monitor":
                return new String[]{
                        "High Blood Pressure (Hypertension) - Blood pressure is too high",
                        "Low Blood Pressure (Hypotension) - Blood pressure is too low",
                        "Irregular Heartbeat (Arrhythmia) - Heartbeat is not regular",
                        "Abnormal Heart Rhythm (Atrial Fibrillation) - Heart's electrical signals are irregular",
                        "Severe High Blood Pressure (Hypertensive Crisis) - Dangerous spike in blood pressure"
                };
            case "SpO2 Monitor":
                return new String[]{
                        "Low Oxygen Levels (Hypoxemia) - Blood has too little oxygen",
                        "Difficulty Breathing (Respiratory Distress) - Breathing is hard or labored",
                        "Carbon Monoxide Poisoning - Inhaling carbon monoxide, a toxic gas",
                        "Blood Clot in Lung (Pulmonary Embolism) - Blockage in lung"
                };
            case "Glucose Meter":
                return new String[]{
                        "High Blood Sugar (Hyperglycemia) - Too much sugar in blood",
                        "Low Blood Sugar (Hypoglycemia) - Too little sugar in blood",
                        "Diabetic Complication (Diabetic Ketoacidosis - DKA) - Serious diabetes issue",
                        "Diabetic Emergency (Hyperosmolar Hyperglycemic State - HHS) - Critical diabetes problem",
                        "Insensitivity to Low Blood Sugar (Hypoglycemic Unawareness) - Can't detect low sugar"
                };
            case "Temperature Reader":
                return new String[]{
                        "Elevated Body Temperature (Fever) - High body temperature",
                        "Dangerously Low Body Temperature (Hypothermia) - Dangerously low body temperature",
                        "Abnormally High Body Temperature (Hyperthermia) - Extremely high body temperature",
                        "Severe Heat-Related Illness (Heat Stroke) - Serious heat-related condition",
                        "Infection-Induced Systemic Response (Sepsis) - Extreme body response to infection"
                };
            default:
                return new String[]{};
        }
    }



    private void resetUI() {
        mainFrame.setTitle("Select Device");
        mainFrame.getContentPane().removeAll();
        mainFrame.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setVisible(true);
        mainFrame.add(new JScrollPane(logTextArea), BorderLayout.SOUTH);
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setTitle("Select Device - Client ID: " + clientId);
    }
    private void updateWindowTitle(String clientId) {
        if (mainFrame != null) {
            mainFrame.setTitle("Patient Monitoring Client - ID: " + clientId);
        }
    }

    public void connectToServer(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            clientId = inputStream.readObject().toString();
            updateWindowTitle(clientId); // Update the window title with the client ID
            mainFrame.setTitle("Client ID: " + clientId); // Optionally set the window title to reflect client ID
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(mainFrame, "Could not connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        ClientWindow clientWindow = new ClientWindow("Client1");
        clientWindow.connectToServer("localhost", 54357);
    }
}
