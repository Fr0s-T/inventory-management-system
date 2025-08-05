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

        String query = """
    SELECT s.ID, s.Date,
           ws.Name AS SourceName,
           wd.Name AS DestinationName,
           s.TotalQuantity,
           e.Username AS HandledBy
    FROM Shipment s
    JOIN Warehouse ws ON s.SourceID = ws.ID
    JOIN Warehouse wd ON s.DestinationID = wd.ID
    JOIN Employee e ON s.EmployeeID = e.ID
    WHERE s.SourceID = ? OR s.DestinationID = ?
""";


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
