package com.cuhlippa.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.config.SettingsManager;
import com.cuhlippa.client.discovery.DiscoveredServer;
import com.cuhlippa.ui.discovery.DeviceDiscoveryDialog;

public class SettingsDialog extends JDialog {
    private final transient Settings settings;
    private JTextField maxHistoryField;
    private JTextField thumbnailSizeField;
    private JComboBox<String> themeCombo;
    private JTextArea ignorePatternsArea;
      // Sync settings components
    private JCheckBox enableSyncCheckBox;
    private JTextField syncServerField;
    private JTextField encryptionKeyField;
    private JButton autoDetectButton;
    private JButton discoverDevicesButton;

    public SettingsDialog(JFrame parent, Settings settings) {
        super(parent, "Settings", true);
        this.settings = settings;
        initializeComponents();
        loadCurrentSettings();
        setupLayout();
        pack();
        setLocationRelativeTo(parent);
    }    private void initializeComponents() {
        maxHistoryField = new JTextField(10);
        thumbnailSizeField = new JTextField(10);
        themeCombo = new JComboBox<>(new String[]{"light", "dark"});
        ignorePatternsArea = new JTextArea(5, 30);
        ignorePatternsArea.setLineWrap(true);
        ignorePatternsArea.setWrapStyleWord(true);
          // Sync components
        enableSyncCheckBox = new JCheckBox("Sync Clipboards");
        syncServerField = new JTextField(25);
        encryptionKeyField = new JTextField(25);
        autoDetectButton = new JButton("Find Automatically");
        discoverDevicesButton = new JButton("🔍 Find Devices");
          // Discover devices button action
        discoverDevicesButton.addActionListener(e -> {
            DeviceDiscoveryDialog dialog = new DeviceDiscoveryDialog((Frame) getOwner());
            if (dialog.showDialog()) {
                DiscoveredServer selectedServer = dialog.getSelectedServer();
                if (selectedServer != null) {
                    String serverUrl = selectedServer.getWebSocketUrl();
                    syncServerField.setText(serverUrl);
                    JOptionPane.showMessageDialog(this, 
                        "Connected to: " + selectedServer.getServerName() + 
                        "\nServer: " + serverUrl,
                        "Server Selected", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        // Auto-detect button action (keep as fallback)
        autoDetectButton.addActionListener(e -> {
            try {
                // Use reflection to call NetworkUtils.buildDefaultSyncUrl()
                Class<?> networkUtilsClass = Class.forName("com.cuhlippa.shared.config.NetworkUtils");
                String defaultUrl = (String) networkUtilsClass.getMethod("buildDefaultSyncUrl").invoke(null);
                syncServerField.setText(defaultUrl);
                JOptionPane.showMessageDialog(this, "Auto-detected server: " + defaultUrl, 
                                            "Auto-Detection", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                syncServerField.setText("ws://localhost:8080/sync");
                JOptionPane.showMessageDialog(this, "Auto-detection failed. Using default: ws://localhost:8080/sync", 
                                            "Auto-Detection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // Enable/disable sync fields based on checkbox
        enableSyncCheckBox.addActionListener(e -> {
            boolean enabled = enableSyncCheckBox.isSelected();
            syncServerField.setEnabled(enabled);
            encryptionKeyField.setEnabled(enabled);
            autoDetectButton.setEnabled(enabled);
            discoverDevicesButton.setEnabled(enabled);
        });
    }    private void loadCurrentSettings() {
        maxHistoryField.setText(String.valueOf(settings.getMaxHistoryItems()));
        thumbnailSizeField.setText(String.valueOf(settings.getThumbnailSize()));
        themeCombo.setSelectedItem(settings.getTheme());
        ignorePatternsArea.setText(String.join("\n", settings.getIgnorePatterns()));
        
        // Load sync settings
        enableSyncCheckBox.setSelected(settings.getSync().isEnabled());
        syncServerField.setText(settings.getSync().getServerAddress());
        encryptionKeyField.setText(settings.getSync().getEncryptionKey());
        
        // Update field states based on sync enabled
        boolean syncEnabled = settings.getSync().isEnabled();
        syncServerField.setEnabled(syncEnabled);
        encryptionKeyField.setEnabled(syncEnabled);
        autoDetectButton.setEnabled(syncEnabled);
    }    private void setupLayout() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        
        // General Settings Tab
        JPanel generalPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        generalPanel.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1;
        generalPanel.add(themeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        generalPanel.add(new JLabel("Max History Items:"), gbc);
        gbc.gridx = 1;
        generalPanel.add(maxHistoryField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        generalPanel.add(new JLabel("Thumbnail Size:"), gbc);
        gbc.gridx = 1;
        generalPanel.add(thumbnailSizeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        generalPanel.add(new JLabel("Ignore Patterns:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        generalPanel.add(new JScrollPane(ignorePatternsArea), gbc);
        
        // Sync Settings Tab
        JPanel syncPanel = new JPanel(new GridBagLayout());
        GridBagConstraints syncGbc = new GridBagConstraints();
        syncGbc.insets = new Insets(10, 10, 5, 10);
        syncGbc.anchor = GridBagConstraints.WEST;

        syncGbc.gridx = 0; syncGbc.gridy = 0;
        syncGbc.gridwidth = 2;
        syncPanel.add(enableSyncCheckBox, syncGbc);

        syncGbc.gridx = 0; syncGbc.gridy = 1;
        syncGbc.gridwidth = 1;
        syncGbc.insets = new Insets(15, 10, 5, 10);
        syncPanel.add(new JLabel("Computer to Connect to:"), syncGbc);
        syncGbc.gridx = 1;
        syncGbc.fill = GridBagConstraints.HORIZONTAL;
        syncGbc.weightx = 1.0;
        syncPanel.add(syncServerField, syncGbc);        syncGbc.gridx = 0; syncGbc.gridy = 2;
        syncGbc.fill = GridBagConstraints.NONE;
        syncGbc.weightx = 0;
        syncPanel.add(new JLabel(""), syncGbc); // Spacer
        
        // Discovery button panel for discovery and auto-detect
        JPanel discoveryButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        discoveryButtonPanel.add(discoverDevicesButton);
        discoveryButtonPanel.add(autoDetectButton);
        
        syncGbc.gridx = 1;
        syncPanel.add(discoveryButtonPanel, syncGbc);

        syncGbc.gridx = 0; syncGbc.gridy = 3;
        syncGbc.insets = new Insets(15, 10, 5, 10);
        syncPanel.add(new JLabel("Security Password (optional):"), syncGbc);
        syncGbc.gridx = 1;
        syncGbc.fill = GridBagConstraints.HORIZONTAL;
        syncGbc.weightx = 1.0;
        syncPanel.add(encryptionKeyField, syncGbc);

        syncGbc.gridx = 0; syncGbc.gridy = 4;
        syncGbc.gridwidth = 2;
        syncGbc.insets = new Insets(10, 10, 5, 10);
        syncGbc.fill = GridBagConstraints.NONE;
        syncGbc.weightx = 0;
        JLabel infoLabel = new JLabel("<html><i>Note: Leave the security password empty if the other computer doesnt require one.<br>" +
                                     "Computer address format: [computer-name-or-ip]:8080</i></html>");
        infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getSize() - 1.0f));
        syncPanel.add(infoLabel, syncGbc);

        // Add tabs
        tabbedPane.addTab("General", generalPanel);
        tabbedPane.addTab("Sync", syncPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveSettings());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }    private void saveSettings() {
        try {
            settings.setTheme((String) themeCombo.getSelectedItem());
            settings.setMaxHistoryItems(Integer.parseInt(maxHistoryField.getText()));
            settings.setThumbnailSize(Integer.parseInt(thumbnailSizeField.getText()));
            
            String patternsText = ignorePatternsArea.getText().trim();
            if (patternsText.isEmpty()) {
                settings.getIgnorePatterns().clear();
            } else {
                settings.setIgnorePatterns(Arrays.asList(patternsText.split("\n")));
            }
            
            // Save sync settings
            settings.getSync().setEnabled(enableSyncCheckBox.isSelected());
            settings.getSync().setServerAddress(syncServerField.getText().trim());
            settings.getSync().setEncryptionKey(encryptionKeyField.getText().trim());

            SettingsManager.saveSettings();
            
            // Show confirmation message
            String message = "Settings saved successfully!";
            if (enableSyncCheckBox.isSelected()) {
                message += "\n\nSync is now enabled. Please restart the application for sync changes to take effect.";
            }
            JOptionPane.showMessageDialog(this, message, "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for history items and thumbnail size.", 
                                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
}
