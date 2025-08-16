package Services;


import Models.Session;
import Models.Warehouse;
import Utilities.DataBaseConnection;
import javafx.scene.control.Alert;

import java.sql.*;
import java.util.ArrayList;

/**
 *
 * Author: @Frost
 *
 */

public class WareHouseService {

    public static ArrayList<Warehouse> getWarehousesFromDb() throws SQLException, ClassNotFoundException {
        ArrayList<Warehouse> warehouses = new ArrayList<>();

        final String sql = "SELECT * FROM v_ManagerWarehouses WHERE RegionalManager = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return warehouses;
    }

    public static void addWarehouse(String name, String location, int capacity, String selectedManager)
            throws SQLException, ClassNotFoundException {
        Warehouse warehouse = new Warehouse(name,selectedManager,capacity,location);

        String callSP = "{call AddWarehouseWithManagerValidation(?,?,?,?,?)}";
        String updateManagerSQL = "UPDATE Employee SET WarehouseID = ? WHERE Username = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(callSP);
             PreparedStatement updateStmt = conn.prepareStatement(updateManagerSQL)) {

            System.out.println(Session.getCurrentUser().getId()+"  "+Session.getCurrentUser().getRole());
            // Prepare the stored procedure call
            cs.setString(1, name);
            cs.setString(2, location);
            cs.setInt(3, capacity);
            cs.setInt(4, Session.getCurrentUser().getId()); // Must be a valid Employee ID (with RoleID = 1)
            cs.registerOutParameter(5, java.sql.Types.INTEGER);

            try {
                cs.execute();
                // **VERY IMPORTANT: flush any remaining results to ensure output param is available!**
                while (cs.getMoreResults() || cs.getUpdateCount() != -1) { /* do nothing */ }
            } catch (SQLException ex) {
                // Handle validation error from RAISERROR in the SP (error code 50000 or custom)
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Warehouse Creation Failed");
                alert.setHeaderText("Error");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                return;
            }

            // Get the generated Warehouse ID
            int newWarehouseId = cs.getInt(5);
            System.out.println("✅ Generated Warehouse ID (SP output): " + newWarehouseId);

            if (newWarehouseId == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Warehouse Creation Failed");
                alert.setHeaderText("No Warehouse Created");
                alert.setContentText("Warehouse was not created. Check that the Regional Manager ID is a valid Employee with RoleID = 1.");
                alert.showAndWait();
                return;
            }

            // Update the manager's warehouse assignment
            updateStmt.setInt(1, newWarehouseId);
            updateStmt.setString(2, selectedManager);
            warehouse.setId(newWarehouseId);
            Session.getWarehouses().add(warehouse);
            int affected = updateStmt.executeUpdate();

            Alert alert = getAlert(affected);
            alert.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Alert getAlert(int affected) {
        Alert alert;
        if (affected == 0) {
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Manager Not Updated");
            alert.setHeaderText("Warning");
            alert.setContentText("Warehouse was created, but the manager could not be updated (username not found).");
        } else {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Manager Assigned");
            alert.setHeaderText("Success");
            alert.setContentText("Manager has been successfully assigned to the new warehouse.");
        }
        return alert;
    }

    public static void getCurrentWarehouseInfo() throws SQLException, ClassNotFoundException {
        final String sql = "SELECT w.ID, w.Name, w.Location, w.Capacity, e.Username " +
                "FROM Warehouse w " +
                "LEFT JOIN Employee e ON e.WarehouseID = w.ID AND e.RoleID = 2 " +
                "WHERE w.ID = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Session.getCurrentUser().getWarehouseId());
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Warehouse warehouse = new Warehouse(
                        rs.getString("Name"),
                        rs.getInt("ID"),
                        rs.getString("Username"),  // now comes from Employee
                        rs.getInt("Capacity"),
                        rs.getString("Location")
                );
                Session.setCurrentWarehouse(warehouse);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void getAllWarehouses(){
        final String sql = "Select * From Warehouse";
        ArrayList<Warehouse> warehouses = new ArrayList<>();

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Warehouse newWarehouse = new Warehouse();
                newWarehouse.setId(rs.getInt("ID"));
                newWarehouse.setName(rs.getString("Name"));
                newWarehouse.setLocation(rs.getString("Location"));
                newWarehouse.setCapacity(rs.getInt("Capacity"));
                // Removed: newWarehouse.setManegeUSerName(rs.getString("Username"));
                warehouses.add(newWarehouse);
            }
            Session.setAllWarehouses(warehouses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
