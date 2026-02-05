package com.forward.direct.debit.bpm.camunda.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class to load and manage database and Camunda properties
 * from application.properties file.
 *
 * This class provides a centralized way to access all configuration values
 * needed for the Camunda application.
 */
public class DatabaseConfig {

    private static final String PROPERTIES_FILE = "application.properties";
    private Properties properties;

    /**
     * Constructor that automatically loads properties from application.properties
     */
    public DatabaseConfig() {
        loadProperties();
    }

    /**
     * Loads properties from the application.properties file in classpath
     *
     * @throws RuntimeException if properties file cannot be found or loaded
     */
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                System.err.println("Unable to find " + PROPERTIES_FILE);
                throw new RuntimeException("Configuration file not found: " + PROPERTIES_FILE);
            }
            properties.load(input);
            System.out.println("Configuration loaded successfully from " + PROPERTIES_FILE);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading properties file: " + ex.getMessage(), ex);
        }
    }

    // ========== Database Configuration Methods ==========

    /**
     * Get JDBC connection URL for PostgreSQL database
     *
     * @return JDBC URL (e.g., jdbc:postgresql://localhost:5432/camunda)
     */
    public String getJdbcUrl() {
        return properties.getProperty("database.jdbc.url");
    }

    /**
     * Get database username
     *
     * @return Database username
     */
    public String getJdbcUsername() {
        return properties.getProperty("database.jdbc.username");
    }

    /**
     * Get database password
     *
     * @return Database password
     */
    public String getJdbcPassword() {
        return properties.getProperty("database.jdbc.password");
    }

    /**
     * Get JDBC driver class name
     *
     * @return JDBC driver (e.g., org.postgresql.Driver)
     */
    public String getJdbcDriver() {
        return properties.getProperty("database.jdbc.driver");
    }

    // ========== Camunda Configuration Methods ==========

    /**
     * Check if database schema auto-update is enabled
     *
     * @return true if schema should be auto-created/updated, false otherwise
     */
    public boolean isSchemaUpdateEnabled() {
        return Boolean.parseBoolean(properties.getProperty("camunda.schema.update", "true"));
    }

    /**
     * Check if Camunda job executor should be activated
     *
     * @return true if job executor should be active, false otherwise
     */
    public boolean isJobExecutorActivateEnabled() {
        return Boolean.parseBoolean(properties.getProperty("camunda.job.executor.activate", "true"));
    }

    /**
     * Get the process definition key to start
     *
     * @return Process definition key (e.g., simple-process)
     */
    public String getProcessDefinitionKey() {
        return properties.getProperty("camunda.process.definition.key");
    }

    /**
     * Get the BPMN process resource file path
     *
     * @return Process resource path (e.g., simple-process.bpmn)
     */
    public String getProcessResource() {
        return properties.getProperty("camunda.process.resource");
    }

    // ========== Connection Pool Configuration Methods ==========

    /**
     * Get maximum number of active database connections in the pool
     *
     * @return Maximum pool size (default: 10)
     */
    public int getMaxPoolSize() {
        return Integer.parseInt(properties.getProperty("database.connection.pool.max", "10"));
    }

    /**
     * Get minimum number of idle database connections in the pool
     *
     * @return Minimum pool size (default: 2)
     */
    public int getMinPoolSize() {
        return Integer.parseInt(properties.getProperty("database.connection.pool.min", "2"));
    }

    // ========== Generic Property Access Methods ==========

    /**
     * Get any property value by key
     *
     * @param key Property key
     * @return Property value or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get property value with a default fallback
     *
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get integer property value
     *
     * @param key Property key
     * @param defaultValue Default value if property not found or invalid
     * @return Integer property value
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            System.err.println("Invalid integer value for property '" + key + "', using default: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Get boolean property value
     *
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Boolean property value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Check if a property exists
     *
     * @param key Property key
     * @return true if property exists, false otherwise
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * Print all loaded properties (useful for debugging)
     * Masks sensitive values like passwords
     */
    public void printConfiguration() {
        System.out.println("=".repeat(80));
        System.out.println("Loaded Configuration:");
        System.out.println("=".repeat(80));
        properties.forEach((key, value) -> {
            String displayValue = key.toString().toLowerCase().contains("password")
                    ? "********"
                    : value.toString();
            System.out.println("  " + key + " = " + displayValue);
        });
        System.out.println("=".repeat(80));
    }

    /**
     * Get all properties
     *
     * @return Properties object containing all configuration
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }
}