package Services;

import Models.User;
import Utilities.DataBaseConnection;
import Utilities.HashingUtility;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

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

            stmt.executeUpdate();
        }
    }


    public static void endCurrentHierarchy(int employeeId){
        String query = "UPDATE Hierarchy SET EndDate = ? WHERE EmployeeID = ? AND EndDate IS NULL";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setInt(2, employeeId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Hierarchy updated for EmployeeID " + employeeId);
            } else {
                System.out.println("No active hierarchy found for EmployeeID " + employeeId);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
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
                System.out.println("New hierarchy record inserted for EmployeeID " + employeeId);
            } else {
                System.out.println("Failed to insert new hierarchy.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
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
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
