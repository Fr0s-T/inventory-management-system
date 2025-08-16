package Services;

import Models.User;
import Utilities.AlertUtils;
import Utilities.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 *
 * Author: @Ilia
 *
 */

public class EditUserServices {

    public static void updateEmployee(int id, String firstName, String middleName, String lastName,
                                      int roleId, boolean onDuty, String password)
            throws SQLException, ClassNotFoundException {

        String sql;
        boolean updatePassword = (password != null && !password.trim().isEmpty());

        if (updatePassword) {
            sql = "UPDATE Employee SET FirstName=?, MiddleName=?, LastName=?, RoleID=?, OnDuty=?, Password=? WHERE ID=?";
        } else {
            sql = "UPDATE Employee SET FirstName=?, MiddleName=?, LastName=?, RoleID=?, OnDuty=? WHERE ID=?";
        }

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, middleName);
            stmt.setString(3, lastName);
            stmt.setInt(4, roleId);
            stmt.setBoolean(5, onDuty);

            if (updatePassword) {
                stmt.setString(6, password);
                stmt.setInt(7, id);
            } else {
                stmt.setInt(6, id);
            }

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                AlertUtils.showSuccess("Employee updated successfully.");
            } else {
                AlertUtils.showWarning("Update", "No employee found with ID: " + id);
            }

        } catch (SQLException | ClassNotFoundException e) {
            AlertUtils.showError("Database Error", e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void endCurrentHierarchy(int employeeId) {
        String query = "UPDATE Hierarchy SET EndDate = ? WHERE EmployeeID = ? AND EndDate IS NULL";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setInt(2, employeeId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                AlertUtils.showSuccess("Hierarchy updated for EmployeeID " + employeeId);
            } else {
                AlertUtils.showWarning("No Update", "No active hierarchy found for EmployeeID " + employeeId);
            }

        } catch (Exception e) {
            AlertUtils.showError("Database Error", e.getMessage());
        }
    }

    public static void insertToHierarchy(int employeeId, int roleId, int managerId) {
        String query = "INSERT INTO Hierarchy (EmployeeID, RoleID, ManagerID, StartDate, EndDate) " +
                "VALUES (?, ?, ?, ?, NULL)";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setInt(2, roleId);
            stmt.setInt(3, managerId);
            stmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                AlertUtils.showSuccess("New hierarchy record inserted for EmployeeID " + employeeId);
            } else {
                AlertUtils.showWarning("Insert Failed", "Failed to insert new hierarchy.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            AlertUtils.showError("Database Error", e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static User fetchUser(int id) {
        String query = "SELECT * FROM Employee WHERE ID = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(rs);
            } else {
                AlertUtils.showWarning("Fetch User", "No user found with ID: " + id);
            }

        } catch (Exception e) {
            AlertUtils.showError("Database Error", e.getMessage());
        }

        return null;
    }
}
