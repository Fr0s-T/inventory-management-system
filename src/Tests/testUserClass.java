package Tests;

import Utilities.DataBaseConnection;
import Models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 *
 * Author: @Frost
 *
 */

public class testUserClass {
    public static void main(String[] args) {
        List<User> users = new ArrayList<>();

        // Initialize database connection
        DataBaseConnection dbConnection = new DataBaseConnection();
        String connectionUrl = dbConnection.getConnectionUrl(300);

        String sql = "SELECT * FROM Employee";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            try (Connection connection = DriverManager.getConnection(connectionUrl);
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {

                System.out.println("Database connection successful!\n");
                System.out.println("Fetching Employee Records...\n");

                while (rs.next()) {
                    // Create User objects without password
                    User user = new User(
                            rs.getInt("ID"),
                            rs.getString("FirstName"),
                            rs.getString("MiddleName"),
                            rs.getString("LastName"),
                            rs.getString("Username"),
                            rs.getBoolean("OnDuty"),
                            rs.getInt("RoleID"),
                            rs.getInt("WarehouseID"),
                            rs.getString("Picture"),
                            rs.getInt("FailedAttempts"),
                            rs.getTimestamp("LockoutUntil"),
                            rs.getBoolean("IsLoggedIn")
                    );

                    users.add(user);
                }
            }

            // Print users info after fetching
            users.forEach(testUserClass::printUserInfo);

        } catch (ClassNotFoundException e) {
            System.err.println("\nError: SQL Server JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("\nDatabase error occurred:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());

            if (e.getMessage().contains("Invalid column name")) {
                System.err.println("\nTIP: Verify your Employee table has these columns:");
                System.err.println("ID, FirstName, MiddleName, LastName, Username, RoleID, WarehouseID, Picture");
            }
            e.printStackTrace();
        }
    }

    private static void printUserInfo(User user) {
        System.out.println("ID: " + user.getId() +
                " | Name: " + user.getFirstName() + " " + user.getMiddleName() + " " + user.getLastName() +
                " | Username: " + user.getUsername() +
                " | Role: " + user.getRole() +
                " | Warehouse: " + user.getWarehouseId() +
                " | Picture: " + (user.getPicture() != null ? user.getPicture() : "No picture")
        );
    }
}
