package com.tekcno.translate.config;

import java.io.*;
import java.util.Properties;

public class TranslatorConfig {

    private final Properties properties;
    private final File configFile;

    public TranslatorConfig(String configurationFilePath) throws IOException {
        configFile = new File(configurationFilePath);
        configFile.getParentFile().mkdirs(); // Create parent directories if not exists
        configFile.createNewFile(); // Create if it doesn't exist
        FileInputStream configFileIS = new FileInputStream(configurationFilePath);
        this.properties = new Properties();
        properties.load(configFileIS);
    }

    /**
     * Get a value from the config, If it does not exist, returns {@code null}
     * @param key Configuration Key
     * @return Value at config key
     */
    public String getConfigString(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get a value from config, If it does not exist, returns {@code defaultValue}
     * @param key Configuration Key
     * @param defaultValue Default return value
     * @return Value at config key or {@code defaultValue}
     */
    public String getConfigString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Set a config value at key if it is not already set
     * @param key Config Key
     * @param value Value to set
     */
    public void setDefaults(String key, String value) {
        if (properties.getProperty(key) != null)
            return;
        properties.setProperty(key, value);
    }

    /**
     * Set configuration string
     * @param key Config key
     * @param value Value to set
     */
    public void setConfigString(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Save configuration
     * @throws IOException Something went wrong
     */
    public void saveConfig() throws IOException {
        Writer inputStream = new FileWriter(this.configFile);
        properties.store(inputStream, "Properties");
    }

}
