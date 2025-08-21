package Services;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Utilities.AlertUtils;
import Utilities.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;

/**
 * ----------------------------------------------------------------------------
 *  ShipmentServices
 *  Author: @Frost  (with MERGE upserts by request)
 *  Description:
 *      - Handles Reception (IN) and Expedition (OUT) shipments.
 *      - Uses SQL Server MERGE for atomic upsert into ProductType and Quantity.
 *      - Batches all writes and wraps each flow in a single DB transaction.
 *      - Refreshes product cache after successful commit.
 * ----------------------------------------------------------------------------
 */
public class ShipmentServices {

    /**
     * Reception flow (IN): Items are received into the destination warehouse.
     * Steps:
     *  1) Create Shipment + ShipmentDetails
     *  2) Ensure all ProductType rows exist (MERGE insert-if-missing)
     *  3) Apply +Quantity deltas to Quantity using MERGE upsert
     *  4) Write ShippedItems
     *  5) Commit + refresh cache
     */
    public static void reception(Warehouse src,
                                 Warehouse dest,
                                 ArrayList<Product> items,
                                 ArrayList<Integer> quantities,
                                 int totalQty,
                                 float totalPrice) {

        if (invalidInput(items, quantities, totalQty)) return;

        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1) Header rows
            int shipmentId = insertShipment(conn, src, dest, totalQty, "IN");
            int detailsId  = insertShipmentDetails(conn, shipmentId, totalPrice, totalQty);

            // 2) Ensure each ItemCode exists globally in ProductType (insert-only if missing)
            upsertProductTypesMerge(conn, items);

            // 3) Apply positive deltas to Quantity for DEST warehouse
            applyQuantityDeltaMerge(conn, dest, items, quantities, +1);

            // 4) Log item lines
            insertShippedItems(conn, detailsId, items, quantities);

            // 5) Commit & refresh cache
            conn.commit();
            ProductsService.getProducts(); // refresh Session products for current warehouse

            AlertUtils.showSuccess("Reception created successfully (ID: " + shipmentId + ").");

        } catch (Exception e) {
            safeRollback(conn);
            e.printStackTrace();
            AlertUtils.showError("Error", "Reception failed: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Expedition flow (OUT): Items leave the source warehouse.
     * Steps:
     *  1) Create Shipment + ShipmentDetails
     *  2) Apply -Quantity deltas to Quantity using MERGE upsert
     *  3) Write ShippedItems
     *  4) Commit + refresh cache
     *
     */
    public static void expedition(Warehouse src,
                                  Warehouse dest,
                                  ArrayList<Product> items,
                                  ArrayList<Integer> quantities,
                                  int totalQty,
                                  float totalPrice) {

        if (invalidInput(items, quantities, totalQty)) return;

        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1) Header rows
            int shipmentId = insertShipment(conn, src, dest, totalQty, "OUT");
            int detailsId  = insertShipmentDetails(conn, shipmentId, totalPrice, totalQty);

            // (Optional) Ensure ProductType exists for outbound lines too, if you want strict referential integrity.
            // upsertProductTypesMerge(conn, items);

            // 2) Apply negative deltas to Quantity for SOURCE warehouse
            applyQuantityDeltaMerge(conn, src, items, quantities, -1);

            // 3) Log item lines (persist unit price at the time of shipment)
            insertShippedItems(conn, detailsId, items, quantities);

            // 4) Commit & refresh cache
            conn.commit();
            ProductsService.getProducts();

            AlertUtils.showSuccess("Expedition created successfully (ID: " + shipmentId + ").");

        } catch (Exception e) {
            safeRollback(conn);
            e.printStackTrace();
            AlertUtils.showError("Error", "Expedition failed: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }
    }

    /* ---------------------------- Validation ---------------------------- */

    private static boolean invalidInput(ArrayList<Product> items, ArrayList<Integer> qty, int total) {
        if (items == null || items.isEmpty()) {
            AlertUtils.showError("Error", "No products selected.");
            return true;
        }
        if (qty == null || qty.isEmpty()) {
            AlertUtils.showError("Error", "Quantities missing.");
            return true;
        }
        if (items.size() != qty.size()) {
            AlertUtils.showError("Error", "Items and quantities mismatch.");
            return true;
        }
        if (total <= 0) {
            AlertUtils.showError("Error", "Total quantity must be positive.");
            return true;
        }
        return false;
    }

    /* ---------------------------- Headers ---------------------------- */

    private static int insertShipment(Connection conn, Warehouse src, Warehouse dest, int qty, String type) throws SQLException {
        final String sql =
                "INSERT INTO Shipment (Date, Type, SourceID, DestinationID, TotalQuantity, EmployeeID) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, new Date(System.currentTimeMillis()));
            ps.setString(2, type);
            ps.setInt(3, src.getId());
            ps.setInt(4, dest.getId());
            ps.setInt(5, qty);
            ps.setInt(6, Session.getCurrentUser().getId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static int insertShipmentDetails(Connection conn, int shipmentId, float price, int qty) throws SQLException {
        final String sql =
                "INSERT INTO ShipmentDetails (ShipmentID, TotalPrice, TotalQuantity) " +
                        "VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, shipmentId);
            ps.setFloat(2, price);
            ps.setInt(3, qty);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* ---------------------------- Lines ---------------------------- */

    /**
     * Persists shipped lines. Stores UnitPrice snapshot for auditing.
     */
    private static void insertShippedItems(Connection conn,
                                           int detailsId,
                                           ArrayList<Product> items,
                                           ArrayList<Integer> qty) throws SQLException {
        System.out.println("[DEBUG] Starting insertShippedItems");
        System.out.println("[DEBUG] DetailsID: " + detailsId + ", Items count: " + items.size());

        final String sql = "INSERT INTO ShippedItems (ShipmentDetailsID, ItemCode, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < items.size(); i++) {
                System.out.println("[DEBUG] Inserting: " + items.get(i).getItemCode() + ", Qty: " + qty.get(i));
                ps.setInt(1, detailsId);
                ps.setString(2, items.get(i).getItemCode());
                ps.setInt(3, qty.get(i));
                ps.setFloat(4, items.get(i).getUnitPrice());
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("[DEBUG] Batch executed successfully");
        } catch (SQLException e) {
            System.out.println("[ERROR] SQLException: " + e.getMessage());
            throw e;
        }
    }

    /* ---------------------------- MERGE Upserts ---------------------------- */

    /**
     * Ensures each ItemCode exists in ProductType.
     * - If not found, INSERTs a new row.
     * - If found, does nothing (no-op).
     *
     * Uses batched MERGE with a single VALUES row per batch entry.
     */
    private static void upsertProductTypesMerge(Connection conn,
                                                ArrayList<Product> products) throws SQLException {
        final String sql =
                "MERGE ProductType AS T " +
                        "USING (VALUES (?, ?, ?, ?, ?, ?)) AS S(ItemCode, Name, UnitPrice, Color, Size, Section) " +
                        "   ON T.ItemCode = S.ItemCode " +
                        "WHEN MATCHED THEN " +  // ← ADD THIS CLAUSE
                        "   UPDATE SET " +
                        "      Name = S.Name, " +
                        "      UnitPrice = S.UnitPrice, " +
                        "      Color = S.Color, " +
                        "      Size = S.Size, " +
                        "      Section = S.Section " +
                        "WHEN NOT MATCHED THEN " +
                        "   INSERT (ItemCode, Name, UnitPrice, Color, Size, Section) " +
                        "   VALUES (S.ItemCode, S.Name, S.UnitPrice, S.Color, S.Size, S.Section);";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Product p : products) {
                ps.setString(1, p.getItemCode());
                ps.setString(2, p.getName()   != null ? p.getName()   : "");
                ps.setFloat (3, p.getUnitPrice());
                ps.setString(4, p.getColor()  != null ? p.getColor()  : "");
                ps.setString(5, p.getSize()   != null ? p.getSize()   : "");
                ps.setString(6, p.getSection()!= null ? p.getSection(): "");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Applies a signed quantity delta to Quantity per (ItemCode, WarehouseID).
     * - WHEN MATCHED: UPDATE (add delta)
     * - WHEN NOT MATCHED: INSERT (with delta as starting quantity)
     *
     * @param sign +1 for reception, -1 for expedition
     */
    private static void applyQuantityDeltaMerge(Connection conn,
                                                Warehouse warehouse,
                                                ArrayList<Product> products,
                                                ArrayList<Integer> qty,
                                                int sign) throws SQLException {

        System.out.println("[DEBUG] Starting Quantity MERGE for warehouse: " +
                (warehouse != null ? warehouse.getId() : "NULL_WAREHOUSE"));

        final String sql = "MERGE Quantity AS T " +
                "USING (VALUES (?, ?, ?)) AS S(ItemCode, QtyDelta, WarehouseID) " +
                "   ON T.ItemCode = S.ItemCode AND T.WarehouseID = S.WarehouseID " +
                "WHEN MATCHED THEN " +
                "   UPDATE SET Quantity = T.Quantity + S.QtyDelta " +
                "WHEN NOT MATCHED THEN " +
                "   INSERT (ItemCode, Quantity, WarehouseID) " +
                "   VALUES (S.ItemCode, S.QtyDelta, S.WarehouseID);";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < products.size(); i++) {
                int delta = sign * qty.get(i);
                String itemCode = products.get(i).getItemCode();

                // DEBUG LOGGING
                assert warehouse != null;
                System.out.println("[DEBUG] ItemCode: '" + itemCode +
                        "', Delta: " + delta +
                        ", WarehouseID: " + warehouse.getId());

                // NULL CHECKS
                if (itemCode == null) {
                    throw new SQLException("ItemCode cannot be null for product at index: " + i);
                }

                ps.setString(1, itemCode);
                ps.setInt(2, delta);
                ps.setInt(3, warehouse.getId());
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("[DEBUG] Quantity MERGE completed successfully");
        }
    }

    /* ---------------------------- Helpers ---------------------------- */

    private static void safeRollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) { }
        }
    }

    private static void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) { }
        }
    }
}
