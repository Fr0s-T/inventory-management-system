package Services;

import Models.Product;
import Utilities.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class EditProductService {

    public static boolean updateProduct(Product product) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE ProductType SET " +
                "Color = ?, " +
                "Size = ?, " +
                "Section = ?, " +
                "UnitPrice = ?, " +
                "Name = ? " +
                "WHERE ItemCode = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getColor());
            ps.setString(2, product.getSize());
            ps.setString(3, product.getSection());
            ps.setFloat(4, product.getUnitPrice());
            ps.setString(5, product.getName());
            ps.setString(6, product.getItemCode());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

