package Services;

import Models.User;
import Utilities.DataBaseConnection;
import Utilities.HashingUtility;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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


    //public static void UpdateHierarchy{



   // }

    //public static void insertToHierarchy{


    //}

    public static User fetchUser(int id) {
        String query = "SELECT * FROM Employee WHERE ID = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(rs); // uses your constructor that maps all fields
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
