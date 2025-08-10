package Services;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Utilities.AlertUtils;
import Utilities.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;

/**
 * Author: @Frost
 * Shipment service for reception and expedition flows
 */
public class ShipmentServices {

    public static void reception(Warehouse src, Warehouse dest, ArrayList<Product> items,
                                 ArrayList<Integer> quantities, int totalQty, float totalPrice) {
        if (invalidInput(items, quantities, totalQty)) return;

        try (Connection conn = DataBaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int shipmentId = insertShipment(conn, src, dest, totalQty, "IN");
            int detailsId = insertShipmentDetails(conn, shipmentId, totalPrice, totalQty);

            ProductsService.getProducts();
            ArrayList<Product> cached = Session.getProducts();

            ArrayList<Product> newProducts = new ArrayList<>();
            ArrayList<Product> existingProducts = new ArrayList<>();
            ArrayList<Integer> newQuantities = new ArrayList<>();
            ArrayList<Integer> existingQuantities = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                Product p = items.get(i);
                int qty = quantities.get(i);

                boolean exists = cached.stream()
                        .anyMatch(prod -> prod.getItemCode().equalsIgnoreCase(p.getItemCode()));

                if (exists) {
                    existingProducts.add(p);
                    existingQuantities.add(qty);
                } else {
                    newProducts.add(p);
                    newQuantities.add(qty);
                }
            }

            if (!newProducts.isEmpty()) {
                ArrayList<Product> trulyNew = filterTrulyNewProductTypes(conn, newProducts);

                if (!trulyNew.isEmpty()) {
                    insertNewProductTypes(conn, trulyNew);
                }

                insertIntoQuantityTable(conn, newProducts, newQuantities, dest); // Keep original list
            }


            if (!existingProducts.isEmpty()) {
                updateQuantities(conn, dest, existingProducts, existingQuantities);
            }

            insertShippedItems(conn, detailsId, items, quantities);
            conn.commit();
            ProductsService.getProducts();

            AlertUtils.showSuccess("Reception created successfully (ID: " + shipmentId + ").");

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Reception failed: " + e.getMessage());
        }
    }

    public static void expedition(Warehouse src, Warehouse dest, ArrayList<Product> items,
                                  ArrayList<Integer> quantities, int totalQty, float totalPrice) {
        if (invalidInput(items, quantities, totalQty)) return;

        try (Connection conn = DataBaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int shipmentId = insertShipment(conn, src, dest, totalQty, "OUT");
            int detailsId = insertShipmentDetails(conn, shipmentId, totalPrice, totalQty);

            insertShippedItems(conn, detailsId, items, quantities);
            decrementQuantities(conn, src, items, quantities);

            conn.commit();
            ProductsService.getProducts();

            AlertUtils.showSuccess("Expedition created successfully (ID: " + shipmentId + ").");

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Expedition failed: " + e.getMessage());
        }
    }

    private static boolean invalidInput(ArrayList<Product> items, ArrayList<Integer> qty, int total) {
        if (items == null || items.isEmpty()) {
            AlertUtils.showError("Error", "No products selected.");
            return true;
        }
        if (qty == null || qty.isEmpty()) {
            AlertUtils.showError("Error", "Quantities missing.");
            return true;
        }
        if (total <= 0) {
            AlertUtils.showError("Error", "Total quantity must be positive.");
            return true;
        }
        return false;
    }

    private static int insertShipment(Connection conn, Warehouse src, Warehouse dest, int qty, String type) throws SQLException {
        String sql = "INSERT INTO Shipment (Date, Type, SourceID, DestinationID, TotalQuantity, EmployeeID) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, new Date(System.currentTimeMillis()));
            ps.setString(2, type);
            ps.setInt(3, src.getId());
            ps.setInt(4, dest.getId());
            ps.setInt(5, qty);
            ps.setInt(6, Session.getCurrentUser().getId());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static int insertShipmentDetails(Connection conn, int shipmentId, float price, int qty) throws SQLException {
        String sql = "INSERT INTO ShipmentDetails (ShipmentID, TotalPrice, TotalQuantity) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, shipmentId);
            ps.setFloat(2, price);
            ps.setInt(3, qty);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void insertShippedItems(Connection conn, int detailsId,
                                           ArrayList<Product> items, ArrayList<Integer> qty) throws SQLException {
        String sql = "INSERT INTO ShippedItems (ShipmentDetailsID, ItemCode, Quantity , UnitPrice) VALUES (?, ?, ?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < items.size(); i++) {
                ps.setInt(1, detailsId);
                ps.setString(2, items.get(i).getItemCode());
                ps.setInt(3, qty.get(i));
                ps.setFloat(4,items.get(i).getUnitPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertNewProductTypes(Connection conn, ArrayList<Product> products) throws SQLException {
        String sql = "INSERT INTO ProductType (ItemCode, Name, UnitPrice, Color, Size, Section) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Product p : products) {
                ps.setString(1, p.getItemCode());
                ps.setString(2, p.getName());
                ps.setFloat(3, p.getUnitPrice());
                ps.setString(4, p.getColor());
                ps.setString(5, p.getSize());
                ps.setString(6, p.getSection());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertIntoQuantityTable(Connection conn, ArrayList<Product> products,
                                                ArrayList<Integer> qty, Warehouse warehouse) throws SQLException {
        String sql = "INSERT INTO Quantity (ItemCode, Quantity, WarehouseID) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < products.size(); i++) {
                ps.setString(1, products.get(i).getItemCode());
                ps.setInt(2, qty.get(i));
                ps.setInt(3, warehouse.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void updateQuantities(Connection conn, Warehouse warehouse,
                                         ArrayList<Product> products, ArrayList<Integer> qty) throws SQLException {
        String sql = "UPDATE Quantity SET Quantity = Quantity + ? WHERE ItemCode = ? AND WarehouseID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < products.size(); i++) {
                ps.setInt(1, qty.get(i));
                ps.setString(2, products.get(i).getItemCode());
                ps.setInt(3, warehouse.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void decrementQuantities(Connection conn, Warehouse warehouse,
                                            ArrayList<Product> products, ArrayList<Integer> qty) throws SQLException {
        String sql = "UPDATE Quantity SET Quantity = Quantity - ? WHERE ItemCode = ? AND WarehouseID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < products.size(); i++) {
                ps.setInt(1, qty.get(i));
                ps.setString(2, products.get(i).getItemCode());
                ps.setInt(3, warehouse.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static ArrayList<Product> filterTrulyNewProductTypes(Connection conn, ArrayList<Product> products) throws SQLException {
        ArrayList<Product> newProducts = new ArrayList<>();
        String sql = "SELECT ItemCode FROM ProductType WHERE ItemCode = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Product p : products) {
                ps.setString(1, p.getItemCode());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        newProducts.add(p);
                    }
                }
            }
        }
        return newProducts;
    }

}
