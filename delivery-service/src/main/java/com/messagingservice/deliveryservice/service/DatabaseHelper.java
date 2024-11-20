package com.messagingservice.deliveryservice.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHelper {

    public static boolean searchInDatabase(String columnName, String value, String DB_URL, String DB_USERNAME, String DB_PASSWORD) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            // Establish the database connection
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // Prepare the SQL statement
            String sql = "SELECT COUNT(*) FROM demoeventdata WHERE " + columnName + " = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, value);

            // Execute the query
            resultSet = statement.executeQuery();
            resultSet.next();

            // Get the count of matching rows
            int rowCount = resultSet.getInt(1);

            return rowCount > 0; // Return true if at least one matching row is found

        } catch (SQLException e) {
            // Handle any potential database errors here
            e.printStackTrace();
        } finally {
            // Close the database resources
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return false; // Return false by default if any error occurs
    }
}

