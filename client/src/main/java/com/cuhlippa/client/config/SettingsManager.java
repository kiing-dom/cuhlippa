package com.cuhlippa.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class SettingsManager {
    private static final String CONFIG_DIR = "config";
    private static final String SETTINGS_FILE = CONFIG_DIR + File.separator + "settings.json";

    private static Settings settings;
    private static final ObjectMapper mapper = new ObjectMapper();

    private SettingsManager() {}

    public static void loadSettings() {
        try {
            File configDir = new File(CONFIG_DIR);
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            File file = new File(SETTINGS_FILE);
            if (file.exists()) {
                settings = mapper.readValue(file, Settings.class);
            } else {
                settings = new Settings();
                saveSettings();
            }
        } catch (IOException e) {
            e.printStackTrace();
            settings = new Settings();
        }
    }

    public static void saveSettings() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(SETTINGS_FILE), settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Settings getSettings() {
        if (settings == null) {
            loadSettings();
        }

        return settings;
    }
}
