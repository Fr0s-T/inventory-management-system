import java.sql.*;

public class test1 {
    public static void main(String[] args) {
        // Database connection details
        String server = "inventorymanegmentsystem-srv.database.windows.net";
        String database = "inventory_manegment_system_db";  // Replace with your actual DB name
        String username = "sqladmin";
        String password = "";      // Replace with your actual password

        String connectionUrl = "jdbc:sqlserver://" + server + ":1433;"
                + "database=" + database + ";"
                + "user=" + username + ";"
                + "password=" + password + ";"
                + "encrypt=true;"
                + "trustServerCertificate=false;"
                + "loginTimeout=30;";

        // SQL to insert a test row
        String insertSQL = "INSERT INTO test1 (id, name) VALUES (1, 'Test Name')";

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