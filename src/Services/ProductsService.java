package Services;

import Models.Product;
import Models.Session;
import Utilities.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class ProductsService {

    public static void getProducts(){
        final String sql =
                "SELECT " +
                        "  p.ItemCode, " +
                        "  p.Color, " +
                        "  q.Quantity, " +
                        "  p.Size, " +
                        "  p.Section, " +
                        "  p.Picture " +
                        "FROM Quantity AS q " +
                        "INNER JOIN ProductType AS p ON q.ItemCode = p.ItemCode " +
                        "INNER JOIN Warehouse AS w ON q.WarehouseID = w.ID " +
                        "WHERE w.ID = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setInt(1, Session.getCurrentWarehouse().getId());

            ResultSet rs = statement.executeQuery();
            ArrayList<Product> products = new ArrayList<>();

            while (rs.next()){
                Product product = new Product(
                        rs.getString("ItemCode"),
                        rs.getString("Color"),
                        rs.getInt("Quantity"),
                        rs.getString("Size"),
                        rs.getString("Section"),
                        rs.getString("Picture")
                );
                products.add(product);
            }
            Session.setProducts(products);

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static ArrayList<Product> getProductsByWarehouseId(int warehouseId) {
        final String sql =
                "SELECT " +
                        "  p.ItemCode, " +
                        "  p.Color, " +
                        "  q.Quantity, " +
                        "  p.Size, " +
                        "  p.Section, " +
                        "  p.Picture " +
                        "FROM Quantity AS q " +
                        "INNER JOIN ProductType AS p ON q.ItemCode = p.ItemCode " +
                        "INNER JOIN Warehouse AS w ON q.WarehouseID = w.ID " +
                        "WHERE w.ID = ?";



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
                        rs.getString("Picture")
                );
                products.add(product);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); // For testing, prints error in console
        }

        return products;
    }

}
