package Tests;

import Utilities.DataBaseConnection;
import java.sql.*;

/**
 *
 * Author: @Frost
 *
 */

public class DataBaseConnectionTest {
    public static void main(String[] args) {
        // 1. Initialize database connection
        DataBaseConnection dbConnection = new DataBaseConnection();
        String connectionUrl = dbConnection.getConnectionUrl(300);

        // 2. Changed to SELECT * query
        String sql = "SELECT * FROM Employee";

        try {
            // 3. Load JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // 4. Establish connection and execute query
            try (Connection connection = DriverManager.getConnection(connectionUrl);
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {

                System.out.println("Database connection successful!\n");
                System.out.println("Employee Records:");
                System.out.println("----------------------------------------");

                // 5. Process results with picture path
                while (rs.next()) {
                    System.out.printf(
                            "ID: %d | Name: %s %s %s | Username: %s | Password: %s | Role: %d | Warehouse: %d | Picture: %s%n",
                            rs.getInt("ID"),
                            rs.getString("FirstName"),
                            rs.getString("MiddleName"),
                            rs.getString("LastName"),
                            rs.getString("Username"),
                            rs.getString("Password"),
                            rs.getInt("RoleID"),
                            rs.getInt("WarehouseID"),
                            rs.getString("Picture")
                    );

                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("\nError: SQL Server JDBC Driver not found!");
            System.err.println("Please ensure the driver is in your classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("\nDatabase error occurred:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());

            // Additional debug for column issues
            if (e.getMessage().contains("Invalid column name")) {
                System.err.println("\nTIP: Verify your Employee table has these columns:");
                System.err.println("EmployeeID, FirstName, LastName, Username, RoleID, WarehouseID, PicturePath");
            }
            e.printStackTrace();
        }
    }
}