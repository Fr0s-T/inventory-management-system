package Controllers;

import ViewsControllers.*;
import ViewsControllers.ShipmentForm.ShipmentController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import Models.Warehouse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Utility class for managing scene transitions across the application.
 * Author: @Frost
 */
public class SceneLoader {
    private static Stage primaryStage;
    private static String currentSceneFxml = ""; // ✅ Track the current FXML

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadScene(String fxmlPath, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
            if (controller != null) {
                loader.setController(controller);
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // ✅ Update current scene
            currentSceneFxml = fxmlPath;

            if (fxmlPath.contains("LoginPage.fxml")) {
                primaryStage.setMaximized(false);
                primaryStage.centerOnScreen();
            } else {
                primaryStage.centerOnScreen();
            }

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load scene: " + fxmlPath);
        }
    }

    public static void loadWarehouseCards(ArrayList<Warehouse> warehouses, FlowPane flowPane) {
        flowPane.getChildren().clear();
        flowPane.setOrientation(Orientation.HORIZONTAL);
        flowPane.setPrefWrapLength(600);
        flowPane.setHgap(20);
        flowPane.setVgap(20);

        for (Warehouse warehouse : warehouses) {
            try {
                FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource("/Views/WarehouseCards.fxml"));
                Node card = loader.load();

                WarehouseCards controller = loader.getController();
                controller.setData(warehouse);

                flowPane.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadProducts(AnchorPane dynamicPanel) throws IOException {
        dynamicPanel.getChildren().clear();

        FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource("/Views/Products.fxml"));
        Node card = loader.load();
        ProductsController controller = loader.getController();
        dynamicPanel.getChildren().add(card);

        AnchorPane.setTopAnchor(card, 0.0);
        AnchorPane.setBottomAnchor(card, 0.0);
        AnchorPane.setLeftAnchor(card, 0.0);
        AnchorPane.setRightAnchor(card, 0.0);

        // ✅ Update current scene for dynamic panels
        currentSceneFxml = "Products.fxml";
    }

    public static void loadShipment(AnchorPane dynamicPanel) throws IOException {
        dynamicPanel.getChildren().clear();

        FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource("/Views/Shipment.fxml"));
        Node card = loader.load();
        ShipmentController controller = loader.getController();
        dynamicPanel.getChildren().add(card);

        AnchorPane.setTopAnchor(card, 0.0);
        AnchorPane.setBottomAnchor(card, 0.0);
        AnchorPane.setLeftAnchor(card, 0.0);
        AnchorPane.setRightAnchor(card, 0.0);

        currentSceneFxml = "Shipment.fxml";
    }

    public static void loadEditEmployee(AnchorPane dynamicPanel) throws IOException {
        dynamicPanel.getChildren().clear();

        FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource("/Views/Users.fxml"));
        Node card = loader.load();
        UsersController controller = loader.getController();
        dynamicPanel.getChildren().add(card);

        AnchorPane.setTopAnchor(card, 0.0);
        AnchorPane.setBottomAnchor(card, 0.0);
        AnchorPane.setLeftAnchor(card, 0.0);
        AnchorPane.setRightAnchor(card, 0.0);

        currentSceneFxml = "Users.fxml";
    }

    public static void loadReports(AnchorPane dynamicPanel) throws IOException {
        dynamicPanel.getChildren().clear();

        FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource("/Views/Reports.fxml"));
        Node card = loader.load();
        ReportsController controller = loader.getController();
        dynamicPanel.getChildren().add(card);

        AnchorPane.setTopAnchor(card, 0.0);
        AnchorPane.setBottomAnchor(card, 0.0);
        AnchorPane.setLeftAnchor(card, 0.0);
        AnchorPane.setRightAnchor(card, 0.0);

        currentSceneFxml = "Reports.fxml";
    }

    // ✅ Method to get the current scene name
    public static String getCurrentScene() {
        return currentSceneFxml;
    }
}
