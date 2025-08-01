package Services;

import Models.Employee;
import Models.Session;
import Models.User;
import Utilities.DataBaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ManagerService {


    public static ArrayList<User> getWarehouseManagersFromDb() throws SQLException, ClassNotFoundException {
        ArrayList<User> managers = new ArrayList<>();

        final String sql = "SELECT e.ID, e.FirstName, e.MiddleName, e.LastName, e.Username, " +
                "e.RoleID, e.WarehouseID, e.Picture, e.FailedAttempts, " +
                "e.LockoutUntil, e.IsLoggedIn " +
                "FROM Employee e " +
                "INNER JOIN Hierarchy h ON e.ID = h.EmployeeID " +
                "WHERE e.RoleID = 2 " +
                "  AND e.WarehouseID IS NULL " +
                "  AND h.ManagerID = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Session.getCurrentUser().getId());
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                User manager = new User(
                        rs.getInt("ID"),
                        rs.getString("FirstName"),
                        rs.getString("MiddleName"),
                        rs.getString("LastName"),
                        rs.getString("Username"),
                        rs.getInt("RoleID"),
                        rs.getInt("WarehouseID"),
                        rs.getString("Picture"),
                        rs.getInt("FailedAttempts"),
                        rs.getTimestamp("LockoutUntil"),
                        rs.getBoolean("IsLoggedIn")
                );
                managers.add(manager);
            }
        }
        return managers;
    }


    public static void addManager(String firstName, String middleName, String lastName,
                                  String username, String password)
            throws SQLException, ClassNotFoundException {

        // RoleID for warehouse manager is 2
        final int roleID = 2;

        String insertEmployeeSql = "INSERT INTO Employee " +
                "(FirstName, MiddleName, LastName, Username, Password, OnDuty, RoleID, WarehouseID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement insertStmt = connection.prepareStatement(insertEmployeeSql, Statement.RETURN_GENERATED_KEYS)) {

            insertStmt.setString(1, firstName);
            insertStmt.setString(2, middleName);
            insertStmt.setString(3, lastName);
            insertStmt.setString(4, username);
            insertStmt.setString(5, password); // If hashing needed, hash before passing
            insertStmt.setBoolean(6, true);
            insertStmt.setInt(7, 2);
            insertStmt.setNull(8, Types.INTEGER);

            insertStmt.executeUpdate();

            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newId = generatedKeys.getInt(1);

                final String insertHierarchySql = "INSERT INTO Hierarchy (EmployeeID, RoleID, ManagerID, StartDate) VALUES (?, ?, ?, ?)";

                try (PreparedStatement hierarchyStmt = connection.prepareStatement(insertHierarchySql)) {
                    hierarchyStmt.setInt(1, newId);
                    hierarchyStmt.setInt(2, 2); // RoleID for Warehouse Manager
                    hierarchyStmt.setInt(3, Session.getCurrentUser().getId());
                    hierarchyStmt.setDate(4, Date.valueOf(LocalDate.now())); // Current date

                    hierarchyStmt.executeUpdate();
                }

            } else {
                throw new SQLException("Creating manager failed, no ID obtained.");
            }
        }
    }


}
