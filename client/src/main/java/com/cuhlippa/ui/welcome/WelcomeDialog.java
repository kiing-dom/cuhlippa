package com.cuhlippa.ui.welcome;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Welcome dialog shown to users on their first launch of Cuhlippa.
 * Explains what the application does and guides them to the next steps.
 */
public class WelcomeDialog extends JDialog {
    
    public enum UserChoice {
        GET_STARTED,    // User wants guided setup
        ADVANCED_MODE,  // User wants full control
        CANCELLED       // User closed dialog
    }
    
    private UserChoice userChoice = UserChoice.CANCELLED;
    
    public WelcomeDialog(Frame parent) {
        super(parent, "Welcome to Cuhlippa", true);
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureDialog();
    }
    
    private void initializeComponents() {
        // Main components will be created in setupLayout()
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);
        
        // Header with emoji and title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("🎉 Welcome to Cuhlippa!");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("📋 Share your clipboard between computers");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(100, 100, 100));
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Feature explanation panel
        JPanel featuresPanel = createFeaturesPanel();
        
        // Security reassurance panel
        JPanel securityPanel = createSecurityPanel();
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        
        // Combine all panels
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(featuresPanel, BorderLayout.CENTER);
        centerPanel.add(securityPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createFeaturesPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        
        // Feature 1
        JPanel feature1 = createFeatureItem(
            "✨", 
            "What Cuhlippa does:",
            "Copy text or images on one computer and instantly access them on your other computers"
        );
        
        // Feature 2  
        JPanel feature2 = createFeatureItem(
            "🌐", 
            "Works everywhere:",
            "Compatible with Windows, Mac, and Linux - mix and match any computers you own"
        );
        
        // Feature 3
        JPanel feature3 = createFeatureItem(
            "⚡", 
            "Instant and easy:",
            "No accounts, no internet required - just copy and paste like you normally do"
        );
        
        panel.add(feature1);
        panel.add(feature2);
        panel.add(feature3);
        
        return panel;
    }
    
    private JPanel createFeatureItem(String emoji, String title, String description) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        
        // Emoji label
        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        emojiLabel.setVerticalAlignment(SwingConstants.TOP);
        panel.add(emojiLabel, BorderLayout.WEST);
        
        // Text panel
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        textPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        descLabel.setForeground(new Color(80, 80, 80));
        textPanel.add(descLabel, BorderLayout.CENTER);
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSecurityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 252, 248)); // Very light green
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 230, 200), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel iconLabel = new JLabel("🔒");
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        panel.add(iconLabel, BorderLayout.WEST);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(new Color(248, 252, 248));
        
        JLabel titleLabel = new JLabel("Your data stays private");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        titleLabel.setForeground(new Color(0, 100, 0));
        textPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel descLabel = new JLabel("<html>Nothing goes to the internet - clipboard data only travels between YOUR computers on your local network.</html>");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        descLabel.setForeground(new Color(0, 80, 0));
        textPanel.add(descLabel, BorderLayout.CENTER);
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        
        // Get Started button (primary)
        JButton getStartedButton = new JButton("🚀 Get Started");
        getStartedButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        getStartedButton.setPreferredSize(new Dimension(150, 40));
        getStartedButton.setBackground(new Color(0, 123, 255));
        getStartedButton.setForeground(Color.WHITE);
        getStartedButton.setFocusPainted(false);
        getStartedButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Advanced button (secondary)
        JButton advancedButton = new JButton("⚙️ Advanced Setup");
        advancedButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        advancedButton.setPreferredSize(new Dimension(175, 40));
        advancedButton.setBackground(new Color(248, 249, 250));
        advancedButton.setForeground(new Color(108, 117, 125));
        advancedButton.setFocusPainted(false);
        advancedButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        panel.add(getStartedButton);
        panel.add(advancedButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Find buttons and add listeners
        findButtonByText("🚀 Get Started").addActionListener(e -> {
            userChoice = UserChoice.GET_STARTED;
            dispose();
        });
        
        findButtonByText("⚙️ Advanced Setup").addActionListener(e -> {
            userChoice = UserChoice.ADVANCED_MODE;
            dispose();
        });
        
        // Handle window closing
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                userChoice = UserChoice.CANCELLED;
                dispose();
            }
        });
    }
    
    private JButton findButtonByText(String text) {
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
        
        // Set minimum size to ensure good appearance
        Dimension preferredSize = getPreferredSize();
        setMinimumSize(new Dimension(Math.max(500, preferredSize.width), preferredSize.height));
    }
    
    /**
     * Show the welcome dialog and return the user's choice
     */
    public UserChoice showWelcomeDialog() {
        setVisible(true);
        return userChoice;
    }
    
    /**
     * Get the user's choice after the dialog has been closed
     */
    public UserChoice getUserChoice() {
        return userChoice;
    }
}