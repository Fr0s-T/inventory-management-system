package Services;

import Models.Product;
import Models.Session;
import Utilities.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class ProductsService {

    public static void getProducts(){
        final String sql = "SELECT pt.ItemCode, " +
                "pt.Color, " +
                "p.UnitPrice, " +
                "pt.Quantity, " +
                "pt.Size, " +
                "pt.Section, " +
                "pt.Picture, " +
                "pt.ShipmentDetailsID " +
                "FROM Product AS p " +
                "JOIN ProductType AS pt ON p.ItemCode = pt.ItemCode " +
                "WHERE p.WarehouseID = ?";

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
                        rs.getString("Picture"),
                        rs.getInt("ShipmentDetailsID")
                );
                products.add(product);
            }
            Session.setProducts(products);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static ArrayList<Product> getProductsByWarehouseId(int warehouseId) {
        final String sql = "SELECT pt.ItemCode, " +
                "pt.Color, " +
                "p.UnitPrice, " +
                "pt.Quantity, " +
                "pt.Size, " +
                "pt.Section, " +
                "pt.Picture, " +
                "pt.ShipmentDetailsID " +
                "FROM Product AS p " +
                "JOIN ProductType AS pt ON p.ItemCode = pt.ItemCode " +
                "WHERE p.WarehouseID = ?";


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
                        rs.getInt("ShipmentDetailsID")
                );
                products.add(product);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); // For testing, prints error in console
        }

        return products;
    }

}
