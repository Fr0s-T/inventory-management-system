package Services;

import Models.Session;
import Models.Warehouse;
import Utilities.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

            // ✅ SET THE PARAMETER VALUE HERE
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

}
