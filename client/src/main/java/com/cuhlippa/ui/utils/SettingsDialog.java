package com.cuhlippa.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.config.SettingsManager;

public class SettingsDialog extends JDialog {
    private final transient Settings settings;
    private JTextField maxHistoryField;
    private JTextField thumbnailSizeField;
    private JComboBox<String> themeCombo;
    private JTextArea ignorePatternsArea;

    public SettingsDialog(JFrame parent, Settings settings) {
        super(parent, "Settings", true);
        this.settings = settings;
        initializeComponents();
        loadCurrentSettings();
        setupLayout();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        maxHistoryField = new JTextField(10);
        thumbnailSizeField = new JTextField(10);
        themeCombo = new JComboBox<>(new String[]{"light", "dark"});
        ignorePatternsArea = new JTextArea(5, 30);
        ignorePatternsArea.setLineWrap(true);
        ignorePatternsArea.setWrapStyleWord(true);
    }

    private void loadCurrentSettings() {
        maxHistoryField.setText(String.valueOf(settings.getMaxHistoryItems()));
        thumbnailSizeField.setText(String.valueOf(settings.getThumbnailSize()));
        themeCombo.setSelectedItem(settings.getTheme());
        ignorePatternsArea.setText(String.join("\n", settings.getIgnorePatterns()));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(themeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Max History Items:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(maxHistoryField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Thumbnail Size:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(thumbnailSizeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Ignore Patterns:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(new JScrollPane(ignorePatternsArea), gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveSettings() {
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

            SettingsManager.saveSettings();
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for history items and thumbnail size.", 
                                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
}
