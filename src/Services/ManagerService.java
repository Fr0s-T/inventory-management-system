package Services;

import Models.Employee;
import Models.Session;
import Utilities.DataBaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ManagerService {


    public static ArrayList<Employee> getWarehouseManagersFromDb() throws SQLException, ClassNotFoundException {
        ArrayList<Employee> managers = new ArrayList<>();

        final String sql = "SELECT e.ID, e.FirstName, e.MiddleName, e.LastName, e.Username " +
                "FROM Employee e " +
                "INNER JOIN Hierarchy h ON e.ID = h.EmployeeID " +
                "WHERE e.RoleID = 2 " +
                "  AND e.WarehouseID IS NULL " +
                "  AND h.ManagerID = ? ";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Session.getCurrentUser().getId());

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Employee manager = new Employee();
                manager.setId(rs.getInt("ID"));
                manager.setFirstName(rs.getString("FirstName"));
                manager.setMiddleName(rs.getString("MiddleName"));
                manager.setLastName(rs.getString("LastName"));
                manager.setUsername(rs.getString("Username"));
                manager.setPassword(rs.getString("Password"));
                manager.setOnDuty(rs.getBoolean("OnDuty"));
                manager.setRoleID(rs.getInt("RoleID"));
                manager.setWarehouseID(rs.getInt("WarehouseID"));

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
