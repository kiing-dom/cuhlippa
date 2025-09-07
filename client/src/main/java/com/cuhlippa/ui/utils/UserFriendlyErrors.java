package com.cuhlippa.ui.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for showing user-friendly error messages while logging technical details.
 * Eliminates technical jargon from user-facing dialogs.
 */
public class UserFriendlyErrors {
    
    /**
     * Show a user-friendly error dialog while logging technical details to console
     */
    public static void showError(Component parent, String userMessage, String technicalDetails) {
        // Show user-friendly message in UI
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parent, userMessage, "Cuhlippa", JOptionPane.WARNING_MESSAGE);
        });
        
        // Log technical details for debugging
        System.err.println("[DEBUG] " + technicalDetails);
    }
    
    /**
     * Show a user-friendly error dialog without parent component
     */
    public static void showError(String userMessage, String technicalDetails) {
        showError(null, userMessage, technicalDetails);
    }
    
    /**
     * Show a user-friendly info message while logging details to console
     */
    public static void showInfo(Component parent, String userMessage, String technicalDetails) {
        // Show user-friendly message in UI
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parent, userMessage, "Cuhlippa", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Log technical details for debugging
        System.out.println("[INFO] " + technicalDetails);
    }
    
    /**
     * Show a user-friendly info message without parent component
     */
    public static void showInfo(String userMessage, String technicalDetails) {
        showInfo(null, userMessage, technicalDetails);
    }
    
    /**
     * Log only (no UI dialog) - for background operations
     */
    public static void logError(String userFriendlyDescription, String technicalDetails) {
        System.err.println("[ERROR] " + userFriendlyDescription + " - " + technicalDetails);
    }
    
    /**
     * Log only (no UI dialog) - for background operations
     */
    public static void logInfo(String userFriendlyDescription, String technicalDetails) {
        System.out.println("[INFO] " + userFriendlyDescription + " - " + technicalDetails);
    }
}
