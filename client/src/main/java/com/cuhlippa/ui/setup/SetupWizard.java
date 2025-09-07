package com.cuhlippa.ui.setup;

import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.config.SettingsManager;
import com.cuhlippa.ui.discovery.DeviceDiscoveryDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Multi-step setup wizard that guides new users through initial configuration.
 * Shown when users choose "Get Started" from the welcome dialog.
 */
public class SetupWizard extends JDialog {
    
    public enum SetupResult {
        COMPLETED,          // User completed setup successfully
        CANCELLED,          // User cancelled the wizard
        SKIPPED_TO_MAIN     // User chose to skip and go to main app
    }
    
    private SetupResult result = SetupResult.CANCELLED;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    
    // Wizard steps
    private static final String STEP_MODE_SELECTION = "mode_selection";
    private static final String STEP_CONNECTION_SETUP = "connection_setup";
    private static final String STEP_SUCCESS = "success";
    
    // User's choices
    private boolean wantsToShare = false;
    
    public SetupWizard(Frame parent) {
        super(parent, "Setup Cuhlippa", true);
        initializeComponents();
        setupLayout();
        configureDialog();
        showStep(STEP_MODE_SELECTION);
    }
    
    private void initializeComponents() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Add all wizard steps
        cardPanel.add(createModeSelectionStep(), STEP_MODE_SELECTION);
        cardPanel.add(createConnectionSetupStep(), STEP_CONNECTION_SETUP);
        cardPanel.add(createSuccessStep(), STEP_SUCCESS);
        
        add(cardPanel, BorderLayout.CENTER);
    }
    
    private JPanel createModeSelectionStep() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("🚀 How do you want to use Cuhlippa?");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("Choose the option that best fits your needs");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Options panel
        JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        optionsPanel.setBackground(Color.WHITE);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        
        // Option 1: Share between computers
        JPanel shareOption = createModeOption(
            "🌐",
            "Share between my computers",
            "Copy on one computer, paste on another. Perfect for people with multiple devices.",
            new Color(0, 123, 255),
            true
        );
        
        // Option 2: Local only
        JPanel localOption = createModeOption(
            "💻",
            "Use on this computer only",
            "Keep a history of copied items on this computer. Simple and private.",
            new Color(108, 117, 125),
            false
        );
        
        optionsPanel.add(shareOption);
        optionsPanel.add(localOption);
        
        // Button panel
        JPanel buttonPanel = createWizardButtonPanel(null, "Next →", this::handleModeNext);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(optionsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createModeOption(String emoji, String title, String description, Color accentColor, boolean isShareMode) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Left side - emoji
        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        panel.add(emojiLabel, BorderLayout.WEST);
        
        // Center - text content
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(accentColor);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        descLabel.setForeground(new Color(80, 80, 80));
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        textPanel.add(descLabel, BorderLayout.CENTER);
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        // Click handler
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                wantsToShare = isShareMode;
                highlightSelectedOption(panel);
                enableNextButton();
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentColor, 2),
                    BorderFactory.createEmptyBorder(25, 30, 25, 30)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!isOptionSelected(panel)) {
                    panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
                        BorderFactory.createEmptyBorder(25, 30, 25, 30)
                    ));
                }
            }
        });
        
        return panel;
    }
      private void highlightSelectedOption(JPanel selectedPanel) {
        // Remove selection from all options
        Container parent = selectedPanel.getParent();
        if (parent != null) {
            for (Component comp : parent.getComponents()) {
                if (comp instanceof JPanel && comp != selectedPanel) {
                    ((JPanel) comp).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
                        BorderFactory.createEmptyBorder(25, 30, 25, 30)
                    ));
                }
            }
        }
        
        // Highlight selected option
        selectedPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 3),
            BorderFactory.createEmptyBorder(24, 29, 24, 29)
        ));
        selectedPanel.setBackground(new Color(248, 251, 255));
    }
    
    private boolean isOptionSelected(JPanel panel) {
        return panel.getBackground().equals(new Color(248, 251, 255));
    }
    
    private void enableNextButton() {
        JButton nextButton = findButtonInDialog("Next →");
        if (nextButton != null) {
            nextButton.setEnabled(true);
        }
    }
    
    private JPanel createConnectionSetupStep() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("🔗 Let's connect your computers");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("We'll help you find and connect to your other computers");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        
        // Instructions
        JPanel instructionsPanel = new JPanel(new GridLayout(3, 1, 0, 15));
        instructionsPanel.setBackground(Color.WHITE);
        
        instructionsPanel.add(createInstructionItem("1️⃣", "Make sure Cuhlippa is running on your other computers"));
        instructionsPanel.add(createInstructionItem("2️⃣", "Ensure all computers are connected to the same Wi-Fi network"));
        instructionsPanel.add(createInstructionItem("3️⃣", "Click 'Find My Computers' to automatically discover them"));
        
        contentPanel.add(instructionsPanel, BorderLayout.CENTER);
        
        // Action button
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        
        JButton findButton = new JButton("🔍 Find My Computers");
        findButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        findButton.setPreferredSize(new Dimension(200, 40));
        findButton.setBackground(new Color(40, 167, 69));
        findButton.setForeground(Color.WHITE);
        findButton.setFocusPainted(false);
        findButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        findButton.addActionListener(this::handleFindComputers);
        
        actionPanel.add(findButton);
        contentPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = createWizardButtonPanel("← Back", "Skip for Now", this::handleConnectionBack, this::handleConnectionSkip);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createInstructionItem(String number, String text) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        
        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        panel.add(numberLabel, BorderLayout.WEST);
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        panel.add(textLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSuccessStep() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("🎉 You're all set!");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("Cuhlippa is ready to help you share your clipboard");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Next steps panel
        JPanel nextStepsPanel = new JPanel(new GridLayout(3, 1, 0, 20));
        nextStepsPanel.setBackground(Color.WHITE);
        nextStepsPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        
        nextStepsPanel.add(createNextStepItem("📋", "Try it out", "Copy some text and see it appear in your clipboard history"));
        nextStepsPanel.add(createNextStepItem("⚙️", "Customize settings", "Access settings anytime through the system tray or main window"));
        nextStepsPanel.add(createNextStepItem("🔗", "Connect more computers", "Add more devices anytime from the settings menu"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        
        JButton finishButton = new JButton("🚀 Start Using Cuhlippa");
        finishButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        finishButton.setPreferredSize(new Dimension(220, 45));
        finishButton.setBackground(new Color(0, 123, 255));
        finishButton.setForeground(Color.WHITE);
        finishButton.setFocusPainted(false);
        finishButton.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        finishButton.addActionListener(this::handleFinish);
        
        buttonPanel.add(finishButton);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(nextStepsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createNextStepItem(String emoji, String title, String description) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        
        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        panel.add(emojiLabel, BorderLayout.WEST);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        textPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        descLabel.setForeground(new Color(80, 80, 80));
        textPanel.add(descLabel, BorderLayout.CENTER);
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createWizardButtonPanel(String backText, String nextText, ActionListener backAction, ActionListener nextAction) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        
        // Back button (left side)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.WHITE);
        
        if (backText != null && backAction != null) {
            JButton backButton = new JButton(backText);
            backButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            backButton.setPreferredSize(new Dimension(100, 35));
            backButton.setBackground(new Color(248, 249, 250));
            backButton.setForeground(new Color(108, 117, 125));
            backButton.setFocusPainted(false);
            backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
            ));
            backButton.addActionListener(backAction);
            leftPanel.add(backButton);
        }
        
        // Next button (right side)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        
        if (nextText != null && nextAction != null) {
            JButton nextButton = new JButton(nextText);
            nextButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            nextButton.setPreferredSize(new Dimension(120, 35));
            nextButton.setBackground(new Color(0, 123, 255));
            nextButton.setForeground(Color.WHITE);
            nextButton.setFocusPainted(false);
            nextButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            nextButton.setEnabled(false); // Will be enabled when user makes selection
            nextButton.addActionListener(nextAction);
            rightPanel.add(nextButton);
        }
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createWizardButtonPanel(String backText, String nextText, ActionListener nextAction) {
        return createWizardButtonPanel(backText, nextText, null, nextAction);
    }
    
    private void showStep(String stepName) {
        cardLayout.show(cardPanel, stepName);
    }
    
    private void handleModeNext(ActionEvent e) {
        if (wantsToShare) {
            showStep(STEP_CONNECTION_SETUP);
        } else {
            // Local mode - skip connection setup
            configureLocalMode();
            showStep(STEP_SUCCESS);
        }
    }
    
    private void handleConnectionBack(ActionEvent e) {
        showStep(STEP_MODE_SELECTION);
    }
    
    private void handleConnectionSkip(ActionEvent e) {
        // User wants to skip connection setup - configure basic sharing mode
        configureBasicSharingMode();
        showStep(STEP_SUCCESS);
    }
      private void handleFindComputers(ActionEvent e) {
        // Launch the device discovery dialog
        SwingUtilities.invokeLater(() -> {
            DeviceDiscoveryDialog discoveryDialog = new DeviceDiscoveryDialog((Frame) getParent());
            discoveryDialog.setVisible(true);
            
            // After discovery, proceed to success step
            showStep(STEP_SUCCESS);
        });
    }
    
    private void handleFinish(ActionEvent e) {
        result = SetupResult.COMPLETED;
        dispose();
    }
      private void configureLocalMode() {
        try {
            Settings settings = SettingsManager.getSettings();
            settings.getSync().setEnabled(false);
            settings.getSync().setServerAddress("");
            SettingsManager.saveSettings();
        } catch (Exception e) {
            System.err.println("Warning: Could not save settings - " + e.getMessage());
            // Continue anyway - settings will use defaults
        }
    }
    
    private void configureBasicSharingMode() {
        try {
            Settings settings = SettingsManager.getSettings();
            settings.getSync().setEnabled(true);
            // Leave server address empty - user can configure later
            SettingsManager.saveSettings();
        } catch (Exception e) {
            System.err.println("Warning: Could not save settings - " + e.getMessage());
            // Continue anyway - settings will use defaults
        }
    }
    
    private JButton findButtonInDialog(String text) {
        return findButtonInContainer(this, text);
    }
    
    private JButton findButtonInContainer(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (text.equals(button.getText())) {
                    return button;
                }
            } else if (component instanceof Container) {
                JButton found = findButtonInContainer((Container) component, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    private void configureDialog() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(getParent());
        
        // Set reasonable size
        Dimension preferredSize = getPreferredSize();
        setSize(Math.max(600, preferredSize.width), Math.max(500, preferredSize.height));
        
        // Handle window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                result = SetupResult.CANCELLED;
                dispose();
            }
        });
    }
    
    /**
     * Show the setup wizard and return the result
     */
    public SetupResult showSetupWizard() {
        setVisible(true);
        return result;
    }
    
    /**
     * Get the setup result after the dialog has been closed
     */
    public SetupResult getResult() {
        return result;
    }
}
