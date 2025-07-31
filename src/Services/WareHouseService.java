package Services;

import Models.Session;
import Models.Warehouse;
import Utilities.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;

/**
 *
 * Author: @Frost
 *
 */

public class WareHouseService {

    public static ArrayList<Warehouse> getWarehousesFromDb() throws SQLException, ClassNotFoundException {
        ArrayList<Warehouse> warehouses = new ArrayList<>();

        final String sql = "SELECT w.ID, w.Name, w.Location, w.Capacity, e.Username " +
                "FROM Employee AS e " +
                "INNER JOIN Warehouse AS w ON e.WarehouseID = w.ID " +
                "INNER JOIN [dbo].[Hierarchy] AS h ON e.ID = h.EmployeeID " +
                "WHERE e.RoleID = 2 " +
                "AND w.RegionalManager = h.ManagerID " +
                "AND w.RegionalManager = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Session.getCurrentUser().getId());

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Warehouse newWarehouse = new Warehouse();
                newWarehouse.setId(rs.getInt("ID"));
                newWarehouse.setName(rs.getString("Name"));
                newWarehouse.setLocation(rs.getString("Location"));
                newWarehouse.setCapacity(rs.getInt("Capacity"));
                newWarehouse.setManegeUSerName(rs.getString("Username"));
                warehouses.add(newWarehouse);
            }
        }

        return warehouses;
    }

    public static void addWarehouse(String name, String location, int capacity) {
        Warehouse newWarehouse = new Warehouse(name, Session.getCurrentUser().getUsername(), capacity, location);

        final String insertSql = "INSERT INTO Warehouse (Name, Location, Capacity, RegionalManager) VALUES (?, ?, ?, ?)";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, name);
            insertStmt.setString(2, location);
            insertStmt.setInt(3, capacity);
            insertStmt.setInt(4, Session.getCurrentUser().getId());
            insertStmt.executeUpdate();

            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                newWarehouse.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("Creating warehouse failed, no ID obtained.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
