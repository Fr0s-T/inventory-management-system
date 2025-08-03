package Services;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Utilities.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ShipmentServices {

    public static void reception() {
        // Reception logic will be added later
    }

    public static void expedition(Warehouse selectedSourceWarehouse, Warehouse selectedDestinationWarehouse,
                                  ArrayList<Product> items, ArrayList<Integer> quantity, int totQuantity, float totalPrice) {

        try (Connection connection = DataBaseConnection.getConnection()) {

            // 1️⃣ Create Shipment
            int shipmentId = createShipment(connection, selectedSourceWarehouse, selectedDestinationWarehouse, totQuantity);

            // 2️⃣ Create ShipmentDetails
            int shipmentDetailsId = createShipmentDetails(connection, shipmentId, totalPrice, totQuantity);

            System.out.println("Shipment created: " + shipmentId + ", Details ID: " + shipmentDetailsId);

             addShipmentItems(connection, shipmentDetailsId, items, quantity);

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Error creating shipment: " + e.getMessage(), e);
        }
    }

    // -------------------- Sub-methods --------------------

    private static int createShipment(Connection connection, Warehouse source, Warehouse destination, int totQuantity) throws SQLException {
        final String sql = "INSERT INTO Shipment (Date, Type, SourceID, DestinationID, TotalQuantity, EmployeeID) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            ps.setString(2, "OUT");
            ps.setInt(3, source.getId());
            ps.setInt(4, destination.getId());
            ps.setInt(5, totQuantity);
            ps.setInt(6, Session.getCurrentUser().getId());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static int createShipmentDetails(Connection connection, int shipmentId, float totalPrice, int totQuantity) throws SQLException {
        final String sql = "INSERT INTO ShipmentDetails (ShipmentID, TotalPrice, TotalQuantity) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, shipmentId);
            ps.setFloat(2, totalPrice);
            ps.setInt(3, totQuantity);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void addShipmentItems(Connection connection, int shipmentDetailsId,
                                         ArrayList<Product> items, ArrayList<Integer> quantity) throws SQLException {
        final String sql = "INSERT INTO ShippedItems (ShipmentDetailsID, ItemCode, Quantity) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < items.size(); i++) {
                ps.setInt(1, shipmentDetailsId);
                ps.setString(2, items.get(i).getItemCode());
                ps.setInt(3, quantity.get(i));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
