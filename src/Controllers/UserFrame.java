package Controllers;

import Services.LogOutService;
import Services.WareHouseService;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import Models.*;

import java.sql.SQLException;

/**
 * Author: @Frost
 *
 */
public class UserFrame extends Application {

    @FXML private ImageView UserImage;
    @FXML private Label UsernameLabel;
    @FXML private Label WarehouseLabel;
    @FXML private Button AlertBtn;
    @FXML private Button LogoutBtn;
    @FXML private Button ProductsBtn;
    @FXML private Button ShipmentsBtn;
    @FXML private Button ReportsBtn;
    @FXML private Button UsersBtn;
    private Stage mainStage;


    @FXML
    private void initialize() {
        try {
            User user = Session.getCurrentUser();
            // Set username label
            UsernameLabel.setText(user.getUsername());
            // Fetch and display warehouse info if user is linked to one
            if (Session.getWarehouses() == null) {
                WareHouseService.getCurrentWarehouseInfo();
                Warehouse currentWarehouse = Session.getCurrentWarehouse();
                if (currentWarehouse != null) {
                    WarehouseLabel.setText(currentWarehouse.getName());
                } else {
                    WarehouseLabel.setText("No warehouse assigned");
                }
            } else {
                WarehouseLabel.setText(Session.getCurrentWarehouse().getName());
            }

            // Handle logout
            LogoutBtn.setOnAction(event -> LogOutService.Logout());

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to load warehouse information");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        //ProductsBtn.setOnAction(actionEvent -> );
    }



    @Override
    public void start(Stage stage) throws Exception{
        mainStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/UserFrame.fxml"));
        //loader.setController(this);
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
