package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import Models.Warehouse;

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
    public static void loadCards(ArrayList<Warehouse> cards, FlowPane flowPane){
        if (cards == null || flowPane == null) return;

        try {
            flowPane.getChildren().clear(); // Clear existing cards if needed

            for (Warehouse warehouse : cards) {
                FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource("/FXML/WarehouseCards.fxml"));
                Node cardNode = loader.load();

                // Set data on the card's controller
                WarehouseCards controller = loader.getController();
                controller.setData(warehouse);

                flowPane.getChildren().add(cardNode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load warehouse cards.");
        }

    }

}