package Controllers;

import Services.DeletedLogsService;
import Services.LogOutService;
import Services.WareHouseService;
import ViewsControllers.AddManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Models.*;
import jdk.jshell.Snippet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Author: @Frost
 *
 */
public class SuFrame extends Application {

    @FXML private ImageView UserImage;
    @FXML private Label UsernameLabel;
    @FXML private Label StatusLabel;
    @FXML private Button AlertBtn;
    @FXML private Button LogoutBtn;
    @FXML private Button DeletedLogs;
    @FXML private FlowPane flowPane;
    @FXML private Button AddWarehouseBtn;
    @FXML private Button AddManagerBtn;

    private Stage mainStage;


    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {

        User user = Session.getCurrentUser();
        UsernameLabel.setText(user.getUsername());
        StatusLabel.setTextFill(Paint.valueOf("green"));
        StatusLabel.setText("ONLINE");

        ArrayList<Warehouse> warehouses = Session.getWarehouses();
        if (warehouses == null) {
            warehouses = WareHouseService.getWarehousesFromDb();  // Fetch from DB
            Session.setWarehouses(warehouses);                    // Cache it
        }

// (Optional) Only print IDs if you just loaded from DB
        for (Warehouse warehouse : warehouses) {
            System.out.println(warehouse.getId());
        }

        SceneLoader.loadWarehouseCards(warehouses, flowPane);

        LogoutBtn.setOnAction(event -> LogOutService.Logout());
        DeletedLogs.setOnAction(actionEvent -> {
            try {
                // Load FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/DeletedLogs.fxml"));
                AnchorPane page = loader.load();

                // Create popup Stage
                Stage dialogStage = new Stage();
                dialogStage.setTitle("Save deleted logs");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(DeletedLogs.getScene().getWindow());

                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                // Pass stage to controller
                ViewsControllers.DeletedLogs controller = loader.getController();
                controller.setDialogStage(dialogStage);

                // Show popup
                dialogStage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        AddWarehouseBtn.setOnAction(actionEvent -> {
            try {
                // Load FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AddWarehouse.fxml"));
                AnchorPane page = loader.load();

                // Create popup Stage
                Stage dialogStage = new Stage();
                dialogStage.setTitle("Add Warehouse");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(AddWarehouseBtn.getScene().getWindow());

                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                // Pass stage to controller
                ViewsControllers.AddWarehouse controller = loader.getController();
                controller.setDialogStage(dialogStage);

                // Show popup
                dialogStage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        AddManagerBtn.setOnAction(actionEvent -> {
            try {
                // Load FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AddManager.fxml"));
                AnchorPane page = loader.load();

                // Create popup Stage
                Stage dialogStage = new Stage();
                dialogStage.setTitle("Add Manager");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(AddManagerBtn.getScene().getWindow());

                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                // Pass stage to controller
                ViewsControllers.AddManager controller = loader.getController();
                controller.setDialogStage(dialogStage);

                // Show popup
                dialogStage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }


    @Override
    public void start(Stage stage) throws Exception{
        mainStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/SuFrame.fxml"));
        loader.setController(this); // Only if SuFrame is controller
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
