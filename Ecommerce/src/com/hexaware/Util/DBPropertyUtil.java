package com.hexaware.Util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBPropertyUtil {
    public static String getConnectionString(String propertyFileName) {
        Properties properties = new Properties();
        String connectionString = null;

        try (FileInputStream input = new FileInputStream(propertyFileName)) {
            properties.load(input);
            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
            connectionString = url + "?user=" + username + "&password=" + password;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connectionString;
    }
} 