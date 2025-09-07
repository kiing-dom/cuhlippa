package com.cuhlippa.ui.utils;

import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.config.SettingsManager;

/**
 * Manages first-run detection and welcome experience coordination.
 * Integrates with the existing settings system to track whether
 * the user has completed the initial setup.
 */
public class FirstRunManager {
    
    private static final String FIRST_RUN_COMPLETED_KEY = "firstRunCompleted";    /**
     * Check if this is the user's first time running the application
     */
    public static boolean isFirstRun() {
        Settings settings = SettingsManager.getSettings();
        
        // Check if the firstRunCompleted flag exists and is true
        // We'll add this as a custom property to the settings
        return !isFirstRunCompleted(settings);
    }
    
    /**
     * Mark the first-run experience as completed
     */
    public static void markFirstRunCompleted() {
        Settings settings = SettingsManager.getSettings();
        setFirstRunCompleted(settings, true);
        SettingsManager.saveSettings();
    }
    
    /**
     * Reset first-run status (useful for testing or re-onboarding)
     */
    public static void resetFirstRunStatus() {
        Settings settings = SettingsManager.getSettings();
        setFirstRunCompleted(settings, false);
        SettingsManager.saveSettings();
    }
      /**
     * Check if first run has been completed based on settings
     */
    private static boolean isFirstRunCompleted(Settings settings) {
        // For now, we'll use a simple heuristic:
        // If sync is configured (has a server address), assume they've been through setup
        // This isn't perfect but works with the current settings structure
        
        // Check if sync has been explicitly configured
        boolean syncConfigured = settings.getSync().getServerAddress() != null && 
                                !settings.getSync().getServerAddress().trim().isEmpty();
        
        // Also check if they have any ignore patterns set up (indicates customization)
        boolean customizedSettings = !settings.getIgnorePatterns().isEmpty();
        
        // For now, let's rely primarily on sync configuration rather than history items
        // since history items might have been changed during testing
        
        return syncConfigured || customizedSettings;
    }
    
    /**
     * Set the first-run completed status
     * For now, we'll use the sync configuration as our indicator
     */
    private static void setFirstRunCompleted(Settings settings, boolean completed) {
        if (completed) {
            // If no sync server is set, set a placeholder to indicate setup completion
            // This will be overridden if user actually configures sync
            if (settings.getSync().getServerAddress() == null || 
                settings.getSync().getServerAddress().trim().isEmpty()) {
                settings.getSync().setServerAddress("setup_completed_placeholder");
            }
        } else {
            // Reset to indicate first run needed
            settings.getSync().setServerAddress("");
            settings.getSync().setEnabled(false);
            settings.getSync().setEncryptionKey("");
        }
    }
    
    /**
     * Check if the user has completed any meaningful setup
     * (Used to determine if we should show advanced features)
     */
    public static boolean hasUserCustomizedSettings() {
        Settings settings = SettingsManager.getSettings();
        
        // Check various indicators that user has used the app
        boolean hasIgnorePatterns = !settings.getIgnorePatterns().isEmpty();
        boolean hasCustomTheme = !"light".equals(settings.getTheme());
        boolean hasCustomHistory = settings.getMaxHistoryItems() != 100;
        boolean hasSyncEnabled = settings.getSync().isEnabled();
        
        return hasIgnorePatterns || hasCustomTheme || hasCustomHistory || hasSyncEnabled;
    }
    
    /**
     * Determine what kind of welcome experience to show based on current state
     */
    public static WelcomeExperienceType getRecommendedExperience() {
        if (isFirstRun()) {
            return WelcomeExperienceType.FULL_WELCOME;
        } else if (!hasUserCustomizedSettings()) {
            return WelcomeExperienceType.QUICK_TIPS;
        } else {
            return WelcomeExperienceType.NONE;
        }
    }
    
    /**
     * Types of welcome experiences we can show
     */
    public enum WelcomeExperienceType {
        FULL_WELCOME,    // Complete first-run experience
        QUICK_TIPS,      // Brief tips for returning users
        NONE            // No welcome needed
    }
}
