package com.financeapp.model;

public class SystemConfig {
    private static SystemConfig instance;
    private String dbConnection;
    private String systemSettings;

    private SystemConfig() {
        this.dbConnection = "jdbc:mysql://localhost:3306/finance_db";
        this.systemSettings = "default";
    }

    public static SystemConfig getInstance() {
        if (instance == null) {
            instance = new SystemConfig();
        }
        return instance;
    }

    public String getDbConnection() { return dbConnection; }
    public String getSystemSettings() { return systemSettings; }
    public void setSystemSettings(String s) { this.systemSettings = s; }
}