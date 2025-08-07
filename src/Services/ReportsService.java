package Services;

import Models.Session;
import Models.Shipment;
import Models.User;
import Utilities.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * Author: @Ilia
 *
 */


public class ReportsService {



    public static ArrayList<User> getEmployeesFromDb() throws SQLException, ClassNotFoundException {
        ArrayList<User> users = new ArrayList<>();

        String query = "SELECT * FROM Employee WHERE WarehouseID = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Session.getCurrentWarehouse().getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(rs));
                }
            }
        }

        return users;
    }

    public static ArrayList<Shipment> getShipmentsFromDb() throws Exception {
        ArrayList<Shipment> shipments = new ArrayList<>();

        String query = "SELECT * FROM v_ShipmentsWithDetails WHERE SourceID = ? OR DestinationID = ?";


        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int currentWarehouseId = Session.getCurrentWarehouse().getId();

            stmt.setInt(1, currentWarehouseId);
            stmt.setInt(2, currentWarehouseId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Shipment shipment = new Shipment(
                        rs.getInt("ID"),
                        rs.getString("Date"),
                        rs.getString("SourceName"),
                        rs.getString("DestinationName"),
                        rs.getInt("TotalQuantity"),
                        rs.getString("HandledBy")
                );
                shipments.add(shipment);
            }
        }

        return shipments;
    }






}
