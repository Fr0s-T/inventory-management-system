package Controllers;

import Services.LogOutService;
import Services.ProductsService;
import Services.WareHouseService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Models.*;

import java.io.IOException;
import java.sql.SQLException;


/**
 * Author: @Frost,@Ilia
 *
 */
public class UserFrame extends Application {

    @FXML private ImageView UserImage;
    @FXML private Label UsernameLabel;
    @FXML private Label WarehouseLabel;
    @FXML private Button AlertBtn;
    @FXML private Button AddEmployeeBtn;
    @FXML private Button LogoutBtn;
    @FXML private Button ProductsBtn;
    @FXML private Button ShipmentsBtn;
    @FXML private Button ReportsBtn;
    @FXML private Button UsersBtn;
    @FXML private Button BackToDashboard;
    @FXML private AnchorPane dynamicPanel;
    private Stage mainStage;

    @FXML
    private void initialize() {
        try {
            User user = Session.getCurrentUser();
            if(user.getRole()!= User.Role.REGIONAL_MANAGER){
                BackToDashboard.setVisible(false);
                switch (user.getRole()) {
                    case User.Role.SHIFT_MANAGER:
                        UsersBtn.setVisible(false);
                        AddEmployeeBtn.setVisible(false);
                        break;
                    case User.Role.EMPLOYEE:
                        UsersBtn.setVisible(false);
                        ReportsBtn.setVisible(false);
                        AddEmployeeBtn.setVisible(false);
                        break;
                }



            }
            UsernameLabel.setText(user.getUsername());

            if (Session.getGlobalProductCatalog() == null) Session.setGlobalProductCatalog(
                    ProductsService.getGlobalProductCatalog());

            // ✅ Load warehouse info if not cached
            if (Session.getWarehouses() == null) {
                WareHouseService.getCurrentWarehouseInfo();
            }

            Warehouse currentWarehouse = Session.getCurrentWarehouse();
            if (currentWarehouse != null) {
                WarehouseLabel.setText(currentWarehouse.getName());

                // ✅ Start background sync now that warehouse is available
                ProductsService.startBackgroundSync();
            } else {
                WarehouseLabel.setText("No warehouse assigned");
            }

            setupButtons();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to load warehouse information");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void setupButtons() {
        AddEmployeeBtn.setOnAction(actionEvent -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AddEmployee.fxml"));
                AnchorPane page = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Add Employee");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(AddEmployeeBtn.getScene().getWindow());

                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                ViewsControllers.AddEmployeeController controller = loader.getController();
                controller.setDialogStage(dialogStage);

                dialogStage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        ProductsBtn.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadProducts(dynamicPanel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        ShipmentsBtn.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadShipment(dynamicPanel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        BackToDashboard.setOnAction(actionEvent -> LogOutService.BackToDashboard());

        UsersBtn.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadEditEmployee(dynamicPanel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        ReportsBtn.setOnAction(actionEvent -> {
            try {
                SceneLoader.loadReports(dynamicPanel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        LogoutBtn.setOnAction(event -> LogOutService.Logout());
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/UserFrame.fxml"));
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.setOnCloseRequest(event->{
            LogOutService.handleExit();
            Platform.exit();
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
