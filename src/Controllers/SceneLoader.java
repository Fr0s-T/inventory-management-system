package Controllers;

import ViewsControllers.WarehouseCards;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import Models.Warehouse;

import java.io.IOException;
import java.util.ArrayList;
//to do: add primaryStage.setMaximized(true);

/**
 * Utility class for managing scene transitions across the application.
 * Author: @Frost
 */
public class SceneLoader {
    private static Stage primaryStage;

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

            // ✅ Automatically maximize SuFrame
            if (fxmlPath.contains("LoginPage.fxml")) {
                primaryStage.setMaximized(false);
                primaryStage.centerOnScreen();
            } else {
                //primaryStage.setMaximized(true);
                primaryStage.centerOnScreen(); // optional for login page
            }

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load scene: " + fxmlPath);
        }
    }
    public static void loadWarehouseCards(ArrayList<Warehouse> warehouses, FlowPane flowPane) {
        flowPane.getChildren().clear(); // clear old cards if any

        // Layout setup
        flowPane.setOrientation(Orientation.HORIZONTAL);
        flowPane.setPrefWrapLength(600); // 2 cards of 280 + 20 gap
        flowPane.setHgap(20); // horizontal space between cards
        flowPane.setVgap(20); // vertical space between rows

        for (Warehouse warehouse : warehouses) {
            try {
                FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource("/Views/WarehouseCards.fxml"));
                Node card = loader.load();

                WarehouseCards controller = loader.getController();
                controller.setData(warehouse); // inject warehouse data

                flowPane.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

