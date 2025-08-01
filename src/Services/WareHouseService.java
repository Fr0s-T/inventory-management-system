package Services;

import Models.Employee;
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

    public static void addWarehouse(String name, String location, int capacity, Employee selectedManager)
            throws SQLException, ClassNotFoundException {

        String insertWarehouseSQL = "INSERT INTO Warehouse (Name, Location, Capacity, RegionalManager) VALUES (?, ?, ?, ?)";
        String updateManagerSQL = "UPDATE Employee SET WarehouseID = ? WHERE ID = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertWarehouseSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement updateStmt = conn.prepareStatement(updateManagerSQL)) {

            // Insert warehouse with current user as RegionalManager
            insertStmt.setString(1, name);
            insertStmt.setString(2, location);
            insertStmt.setInt(3, capacity);
            insertStmt.setInt(4, Session.getCurrentUser().getId()); // regional manager is current user

            insertStmt.executeUpdate();

            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) {
                int newWarehouseId = keys.getInt(1);

                // Update the selected manager to assign them this warehouse
                updateStmt.setInt(1, newWarehouseId);
                updateStmt.setInt(2, selectedManager.getId());
                updateStmt.executeUpdate();

            } else {
                throw new SQLException("Creating warehouse failed: no ID obtained.");
            }
        }
    }


}
