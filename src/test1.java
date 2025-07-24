import javax.swing.*;
import java.sql.*;

public class test1 {
    public static void main(String[] args) {
        Main m1 = new Main();
        String connectionUrl = m1.getConnectionUrl(300);


        // SQL to insert a test row
        String insertSQL = "INSERT INTO test1 (id, name) VALUES (4, 'fouad')";

        try (Connection connection = DriverManager.getConnection(connectionUrl);
             Statement statement = connection.createStatement()) {

            // Load Microsoft SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Execute the insert
            int rowsAffected = statement.executeUpdate(insertSQL);
            System.out.println(rowsAffected + " row(s) inserted successfully!");

            // Optional: Verify the insert
            ResultSet rs = statement.executeQuery("SELECT * FROM test1");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name"));
            }

        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL Error:");
            e.printStackTrace();
        }
    }
}
