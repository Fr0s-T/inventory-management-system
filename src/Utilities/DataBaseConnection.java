package Utilities;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String server = dotenv.get("DB_SERVER"); // "DESKTOP-8BKFRC5\\SQLEXPRESS"
    private static final String database = dotenv.get("DB_NAME");   // "IMS(1)"
    private static final String username = dotenv.get("DB_USER");   // "sa"
    private static final String password = dotenv.get("DB_PASSWORD"); // "123456"

    public static String getConnectionUrl() {
        // This now uses your environment variables!
        String url = "jdbc:sqlserver://" + server + ":1433;"
                + "databaseName=" + database + ";"
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=30;";
        return url;
    }

    public static Connection getConnection() throws Exception {
        // Print the attempt details (masking the password for security)
        String url = getConnectionUrl();
        System.out.println("[DEBUG] Connection Attempt:");
        System.out.println("  URL: " + url);
        System.out.println("  User: " + username);
        System.out.println("  Password: " + (password != null ? "******" : "null"));

        try {
            // Load the JDBC Driver
            System.out.println("[DEBUG] Loading JDBC driver...");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("[DEBUG] JDBC driver loaded successfully.");

            // Attempt to establish the connection
            System.out.println("[DEBUG] Attempting to connect to the database...");
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("[DEBUG] Connection established successfully!");
            return conn;

        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] CRITICAL: JDBC Driver class not found.");
            System.err.println("  Message: " + e.getMessage());
            System.err.println("  Please ensure the 'mssql-jdbc' JAR file is added to your project's build path.");
            e.printStackTrace();
            throw new Exception("Failed to load SQL Server JDBC driver. Check the JAR file.", e);

        } catch (SQLException e) {
            System.err.println("[ERROR] SQL Exception occurred during connection.");
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Message: " + e.getMessage());

            // Print a more user-friendly message based on common error codes
            switch (e.getErrorCode()) {
                case 18456: // Login failed
                    System.err.println("  Diagnosis: Login failed. Incorrect username or password.");
                    break;
                case 4060: // Cannot open database
                    System.err.println("  Diagnosis: The database '" + database + "' does not exist or access is denied.");
                    break;
                case 0: // Often associated with network issues
                    if (e.getMessage().contains("connection refused") || e.getMessage().contains("The TCP/IP connection to the host has failed")) {
                        System.err.println("  Diagnosis: Network error. Cannot reach the server. Check if SQL Server is running and the server name/port is correct.");
                    }
                    break;
                default:
                    System.err.println("  Diagnosis: An unknown database error occurred.");
            }
            e.printStackTrace();
            throw new Exception("Failed to establish a database connection. See details above.", e);
        }
    }

    // Test method
    public static void testConnection() {
        try (Connection conn = DataBaseConnection.getConnection()) {
            System.out.println("SUCCESS: Connected to database '" + database + "' on server '" + server + "'!");

        } catch (Exception e) {
            System.err.println("testConnection() failed: " + e.getMessage());
        }
    }

    // Main method to run a quick test
    public static void main(String[] args) {
        System.out.println("Testing DataBaseConnection with .env variables...");
        testConnection();
    }
}