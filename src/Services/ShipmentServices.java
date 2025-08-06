package Services;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Utilities.AlertUtils;
import Utilities.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * Author: @Frost
 *
 */
public class ShipmentServices {

    public static void reception(Warehouse selectedSourceWarehouse, Warehouse selectedDestinationWarehouse,
                                 ArrayList<Product> items, ArrayList<Integer> quantity, int totQuantity, float totalPrice) {

        if (isInputInvalid(items, quantity, totQuantity)) return;

        Connection connection = null;
        try {
            connection = DataBaseConnection.getConnection();
            connection.setAutoCommit(false);

            // ✅ Create shipment record
            int shipmentId = createShipment(connection, selectedSourceWarehouse, selectedDestinationWarehouse, totQuantity, "IN");
            int shipmentDetailsId = createShipmentDetails(connection, shipmentId, totalPrice, totQuantity);

            // ✅ Fetch latest products to avoid stale data
            ProductsService.getProducts();
            ArrayList<Product> warehouseProducts = Session.getProducts();

            // ✅ Separate new and existing products
            ArrayList<Product> newProducts = new ArrayList<>();
            ArrayList<Product> existingProducts = new ArrayList<>();
            ArrayList<Integer> newProductsQty = new ArrayList<>();
            ArrayList<Integer> existingProductsQty = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                Product incomingProduct = items.get(i);
                int qty = quantity.get(i);

                boolean exists = warehouseProducts.stream()
                        .anyMatch(p -> p.getItemCode().equalsIgnoreCase(incomingProduct.getItemCode()));

                if (exists) {
                    existingProducts.add(incomingProduct);
                    existingProductsQty.add(qty);
                } else {
                    newProducts.add(incomingProduct);
                    newProductsQty.add(qty);
                }
            }

            // ✅ Insert new products into ProductType table (with Name)
            if (!newProducts.isEmpty()) {
                insertNewProducts(connection, newProducts);
                insertNewProductValuesIntoQuantity(connection, newProducts, newProductsQty, selectedDestinationWarehouse);
            }

            // ✅ Update existing quantities
            if (!existingProducts.isEmpty()) {
                increaseQuantityInQuantityTable(connection, selectedDestinationWarehouse, existingProducts, existingProductsQty);
            }

            // ✅ Add shipment items
            addShipmentItems(connection, shipmentDetailsId, items, quantity);

            connection.commit();
            ProductsService.getProducts(); // Refresh products

            AlertUtils.showSuccess("Reception created successfully (ID: " + shipmentId + ").");

        } catch (SQLException | ClassNotFoundException e) {
            if (connection != null) {
                try { connection.rollback(); } catch (SQLException ignored) {}
            }
            AlertUtils.showError("Error", "Failed to create reception: " + e.getMessage());
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public static void expedition(Warehouse selectedSourceWarehouse, Warehouse selectedDestinationWarehouse,
                                  ArrayList<Product> items, ArrayList<Integer> quantity, int totQuantity, float totalPrice) {

        if (isInputInvalid(items, quantity, totQuantity)) return;

        Connection connection = null;
        try {
            connection = DataBaseConnection.getConnection();
            connection.setAutoCommit(false);

            int shipmentId = createShipment(connection, selectedSourceWarehouse, selectedDestinationWarehouse, totQuantity,"OUT");
            int shipmentDetailsId = createShipmentDetails(connection, shipmentId, totalPrice, totQuantity);

            addShipmentItems(connection, shipmentDetailsId, items, quantity);
            reduceQuantityFromQuantityTable(connection, selectedSourceWarehouse, items, quantity);

            connection.commit();
            ProductsService.getProducts(); // Refresh products after commit

            AlertUtils.showSuccess("Shipment created successfully (ID: " + shipmentId + ").");

        } catch (SQLException | ClassNotFoundException e) {
            if (connection != null) {
                try { connection.rollback(); } catch (SQLException ignored) {}
            }
            AlertUtils.showError("Error", "Failed to create shipment: " + e.getMessage());
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private static boolean isInputInvalid(ArrayList<Product> items, ArrayList<Integer> quantity, int totQuantity) {
        if (items == null || items.isEmpty()) {
            AlertUtils.showError("Error", "No products selected for shipment.");
            return true;
        }
        if (quantity == null || quantity.isEmpty()) {
            AlertUtils.showError("Error", "Quantity list is empty.");
            return true;
        }
        if (totQuantity <= 0) {
            AlertUtils.showError("Error", "Total quantity cannot be zero.");
            return true;
        }
        return false;
    }

    // -------------------- Sub-methods --------------------

    private static int createShipment(Connection connection, Warehouse source, Warehouse destination, int totQuantity, String type) throws SQLException {
        final String sql = "INSERT INTO Shipment (Date, Type, SourceID, DestinationID, TotalQuantity, EmployeeID) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            ps.setString(2, type);
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

    // ✅ Updated method to include Name
    private static void insertNewProducts(Connection connection, ArrayList<Product> newProducts) throws SQLException {
        final String sql = "INSERT INTO ProductType (ItemCode, Name, UnitPrice, Color, Size, Section) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Product p : newProducts) {
                ps.setString(1, p.getItemCode());
                ps.setString(2, p.getName() != null ? p.getName() : "");
                ps.setFloat(3, p.getUnitPrice());
                ps.setString(4, p.getColor() != null ? p.getColor() : "");
                ps.setString(5, p.getSize() != null ? p.getSize() : "");
                ps.setString(6, p.getSection() != null ? p.getSection() : "");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertNewProductValuesIntoQuantity(Connection connection,
                                                           ArrayList<Product> newProducts,
                                                           ArrayList<Integer> newProductsQty,
                                                           Warehouse destinationWarehouse) throws SQLException {
        final String sql = "INSERT INTO Quantity (ItemCode, Quantity, WarehouseID) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < newProducts.size(); i++) {
                Product product = newProducts.get(i);
                int qty = newProductsQty.get(i);

                ps.setString(1, product.getItemCode());
                ps.setInt(2, qty);
                ps.setInt(3, destinationWarehouse.getId());

                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void increaseQuantityInQuantityTable(Connection connection, Warehouse destinationWarehouse,
                                                        ArrayList<Product> items, ArrayList<Integer> quantity) throws SQLException {
        final String sql = "UPDATE Quantity SET Quantity = Quantity + ? WHERE ItemCode = ? AND WarehouseID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < items.size(); i++) {
                ps.setInt(1, quantity.get(i));
                ps.setString(2, items.get(i).getItemCode());
                ps.setInt(3, destinationWarehouse.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
