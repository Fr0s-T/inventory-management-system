package Services;

import Models.Product;
import Models.Session;
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
     * Fetches products from the database for the current warehouse and updates session cache.
     */
    public static void getProducts() {
        ArrayList<Product> products = fetchProductsFromDB(Session.getCurrentWarehouse().getId());
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
     */
    public static void startBackgroundSync() {
        if (scheduler != null && !scheduler.isShutdown()) return;

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("🔍 Checking for product updates...");

                Timestamp dbLastUpdate = getCurrentLastUpdate();
                if (dbLastUpdate != null &&
                        (Session.getLastUpdate() == null || dbLastUpdate.after(Session.getLastUpdate()))) {

                    System.out.println("♻️ Detected change, refreshing product cache...");

                    ArrayList<Product> updatedProducts = fetchProductsFromDB(Session.getCurrentWarehouse().getId());
                    Session.setProducts(updatedProducts); // 🚀 This will trigger refreshTable automatically
                    Session.setLastUpdate(dbLastUpdate);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                        " SELECT MAX(LastUpdated) AS latest FROM ProductType " +
                        " UNION " +
                        " SELECT MAX(LastUpdated) FROM Quantity" +
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
        final String sql =
                "SELECT * FROM v_ProductInventory WHERE WarehouseID = ?";

        ArrayList<Product> products = new ArrayList<>();

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, warehouseId);

            ResultSet rs = statement.executeQuery();

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
                        0, // quantity is unknown here
                        rs.getString("Size"),
                        rs.getString("Section"),
                        null, // picture is not in ProductType table
                        rs.getFloat("UnitPrice"),
                        rs.getString("Name")
                );
                catalog.put(pt.getItemCode(), pt);
            }
        }
        return catalog;
    }



}
