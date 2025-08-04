package Services;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Utilities.DataBaseConnection;
import javafx.application.Platform;
import javafx.scene.control.Alert;

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

        // ✅ Validate inputs before proceeding
        if (items == null || items.isEmpty()) {
            showAlert("Error", "No products selected for shipment.", Alert.AlertType.ERROR);
            return;
        }
        if (quantity == null || quantity.isEmpty()) {
            showAlert("Error", "Quantity list is empty.", Alert.AlertType.ERROR);
            return;
        }
        if (totQuantity <= 0) {
            showAlert("Error", "Total quantity cannot be zero.", Alert.AlertType.ERROR);
            return;
        }

        Connection connection = null;
        try {
            connection = DataBaseConnection.getConnection();
            connection.setAutoCommit(false);

            int shipmentId = createShipment(connection, selectedSourceWarehouse, selectedDestinationWarehouse, totQuantity);
            int shipmentDetailsId = createShipmentDetails(connection, shipmentId, totalPrice, totQuantity);

            addShipmentItems(connection, shipmentDetailsId, items, quantity);
            reduceQuantityFromQuantityTable(connection, selectedSourceWarehouse, items, quantity);

            connection.commit();
            ProductsService.getProducts(); // Refresh products after commit

            // ✅ Success alert
            showAlert("Success", "Shipment created successfully (ID: " + shipmentId + ").", Alert.AlertType.INFORMATION);

        } catch (SQLException | ClassNotFoundException e) {
            if (connection != null) {
                try { connection.rollback(); } catch (SQLException ignored) {}
            }
            showAlert("Error", "Failed to create shipment: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
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

    private static void reduceQuantityFromQuantityTable(Connection connection, Warehouse selectedSourceWarehouse,
                                                        ArrayList<Product> items,
                                                        ArrayList<Integer> quantity) throws SQLException {
        final String sql = "UPDATE Quantity " +
                "SET Quantity = Quantity - ? " +
                "WHERE ItemCode = ? AND WarehouseID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < items.size(); i++) {
                ps.setInt(1, quantity.get(i));
                ps.setString(2, items.get(i).getItemCode());
                ps.setInt(3, selectedSourceWarehouse.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ✅ Reusable alert method
    private static void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
