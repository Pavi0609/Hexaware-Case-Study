package com.hexaware.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnUtil {
    public static Connection getDBConnection(String connectionString) throws SQLException {
        try {
            //-> Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            //-> Establish the connection
            return DriverManager.getConnection(connectionString);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found", e);
        }
    }
} 