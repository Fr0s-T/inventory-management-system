package Services;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Utilities.DataBaseConnection;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * Author: @Frost
 *
 */

public class ProductsService {

    private static ScheduledExecutorService scheduler;

    /**
     * Fetches products for the current warehouse and updates session cache.
     * Safely no-ops if current warehouse is not yet selected.
     */
    public static void getProducts() {
        Warehouse current = Session.getCurrentWarehouse();
        if (current == null) {
            // Not ready yet; avoid NPE and leave cache as-is.
            System.out.println("getProducts(): current warehouse is null, skipping fetch.");
            return;
        }
        ArrayList<Product> products = fetchProductsFromDB(current.getId());
        Session.setProducts(products);
        Session.setLastUpdate(getCurrentLastUpdate());
    }

    /**
     * Fetches products from the database for a specific warehouse ID.
     */
    public static ArrayList<Product> getProductsByWarehouseId(int warehouseId) {
        return fetchProductsFromDB(warehouseId);
    }

    /**
     * Background sync to auto-refresh cached products if DB changes.
     * Safely skips cycles until current warehouse is set.
     */
    public static void startBackgroundSync() {
        if (scheduler != null && !scheduler.isShutdown()) return;

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Warehouse current = Session.getCurrentWarehouse();
                if (current == null) {
                    // App not fully initialized or user logged out; skip this tick.
                    // (Don’t throw; keep the scheduler alive.)
                    // System.out.println("Products sync: current warehouse not set yet.");
                    return;
                }

                Timestamp dbLastUpdate = getCurrentLastUpdate();
                Timestamp cached = Session.getLastUpdate();

                if (dbLastUpdate != null && (cached == null || dbLastUpdate.after(cached))) {
                    // Refresh cache for the active warehouse
                    ArrayList<Product> updatedProducts = fetchProductsFromDB(current.getId());
                    Session.setProducts(updatedProducts);
                    Session.setLastUpdate(dbLastUpdate);
                    // System.out.println("Products sync: cache refreshed.");
                }
            } catch (Throwable t) {
                // Never let an exception kill the repeating task
                t.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Stops background synchronization.
     */
    public static void stopBackgroundSync() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * Retrieves the latest update timestamp from the database.
     */
    public static Timestamp getCurrentLastUpdate() {
        final String sql =
                "SELECT MAX(latest) AS LastUpdate FROM (" +
                        "  SELECT MAX(LastUpdated) AS latest FROM ProductType " +
                        "  UNION " +
                        "  SELECT MAX(LastUpdated) FROM Quantity" +
                        ") AS combined";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getTimestamp("LastUpdate");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Private helper to fetch products from DB.
     */
    private static ArrayList<Product> fetchProductsFromDB(int warehouseId) {
        final String sql = "SELECT * FROM v_ProductInventory WHERE WarehouseID = ?";

        ArrayList<Product> products = new ArrayList<>();

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, warehouseId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                            rs.getString("ItemCode"),
                            rs.getString("Color"),
                            rs.getInt("Quantity"),
                            rs.getString("Size"),
                            rs.getString("Section"),
                            rs.getString("Picture"),
                            rs.getFloat("UnitPrice"),
                            rs.getString("Name")
                    );
                    products.add(product);
                }
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return products;
    }

    public static Map<String, Product> getGlobalProductCatalog() throws SQLException, ClassNotFoundException {
        Map<String, Product> catalog = new HashMap<>();
        String sql = "SELECT * FROM ProductType";

        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product pt = new Product(
                        rs.getString("ItemCode"),
                        rs.getString("Color"),
                        0,
                        rs.getString("Size"),
                        rs.getString("Section"),
                        null,
                        rs.getFloat("UnitPrice"),
                        rs.getString("Name")
                );
                catalog.put(pt.getItemCode(), pt);
            }
        }
        return catalog;
    }
}
