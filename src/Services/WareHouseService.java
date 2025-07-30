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
        final String sql = "Select * from Warehouse Where RegionalManager = "+ Session.getCurrentUser().getId();

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

             ResultSet rs = statement.executeQuery();

             while (rs.next()){
                Warehouse newWarehouse = new Warehouse();
                 newWarehouse.setName(rs.getString("Name"));
                 newWarehouse.setId(rs.getInt("ID"));
                 newWarehouse.setLocation(rs.getString("Location"));
                 //TODO:manager joint query
                 newWarehouse.setCapacity(rs.getInt("Capacity"));
                 warehouses.add(newWarehouse);
             }
        }
        return warehouses;
    }
}
