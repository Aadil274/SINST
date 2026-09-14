package com.sinst.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final String CONFIG_FILE = "db.properties";

    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "sinst_db";
    private static String user = "root";
    private static String password = "password123";

    static {
        loadConfig();
    }

    public static void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(fis);
                host = props.getProperty("db.host", host);
                port = Integer.parseInt(props.getProperty("db.port", String.valueOf(port)));
                database = props.getProperty("db.name", database);
                user = props.getProperty("db.user", user);
                password = props.getProperty("db.password", password);
            } catch (Exception e) {
                System.err.println("Warning: Could not read db.properties, using defaults: " + e.getMessage());
            }
        } else {
            saveConfig();
        }
    }

    public static void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("db.host", host);
            props.setProperty("db.port", String.valueOf(port));
            props.setProperty("db.name", database);
            props.setProperty("db.user", user);
            props.setProperty("db.password", password);
            props.store(fos, "SINST Database Configuration");
        } catch (Exception e) {
            System.err.println("Warning: Could not save db.properties: " + e.getMessage());
        }
    }

    public static void updateCredentials(String newHost, int newPort, String newDb, String newUser, String newPass) {
        host = newHost;
        port = newPort;
        database = newDb;
        user = newUser;
        password = newPass;
        saveConfig();
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath. Ensure mysql-connector-j.jar is in lib/", e);
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + 
                     "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDatabase() { return database; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }
}
