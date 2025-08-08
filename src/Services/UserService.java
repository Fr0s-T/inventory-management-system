package Services;

import Models.Session;
import Models.User;
import Utilities.DataBaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 * Author: @Frost
 *
 */

public class UserService {


    public static ArrayList<User> getWarehouseManagersFromDb() throws SQLException, ClassNotFoundException {
        ArrayList<User> managers = new ArrayList<>();

        final String sql = "SELECT * FROM v_ManagersWithoutWarehouse WHERE ManagerID = ?";

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
                        rs.getBoolean("OnDuty"),
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

    public static void addEmployee(String firstName, String middleName, String lastName,
                                   String username, String password, Boolean isShiftManager)
            throws SQLException, ClassNotFoundException {

        int RoleID = 4;
        if (isShiftManager) RoleID = 3;

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
            insertStmt.setInt(7, RoleID);
            insertStmt.setInt(8, Session.getCurrentWarehouse().getId());

            insertStmt.executeUpdate();

            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);

                    String getWarehouseManagerSql =
                            "SELECT ID FROM Employee WHERE WarehouseID = ? AND RoleID = 2";

                    int warehouseManagerId = -1;
                    try (PreparedStatement wmStmt = connection.prepareStatement(getWarehouseManagerSql)) {
                        wmStmt.setInt(1, Session.getCurrentWarehouse().getId());
                        try (ResultSet rs = wmStmt.executeQuery()) {
                            if (rs.next()) {
                                warehouseManagerId = rs.getInt("ID");
                            } else {
                                throw new SQLException("No Warehouse Manager found for this warehouse.");
                            }
                        }
                    }


                    final String insertHierarchySql = "INSERT INTO Hierarchy (EmployeeID, RoleID, ManagerID, StartDate) VALUES (?, ?, ?, ?)";

                    try (PreparedStatement hierarchyStmt = connection.prepareStatement(insertHierarchySql)) {
                        hierarchyStmt.setInt(1, newId);
                        hierarchyStmt.setInt(2, RoleID);
                        hierarchyStmt.setInt(3, warehouseManagerId);
                        hierarchyStmt.setDate(4, Date.valueOf(LocalDate.now()));

                        hierarchyStmt.executeUpdate();
                    }
                } else {
                    throw new SQLException("Creating employee failed, no ID obtained.");
                }
            }
        }
    }



}
