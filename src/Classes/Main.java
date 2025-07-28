package Classes;

import javafx.application.Application;
import javafx.stage.Stage;
import Controllers.SceneLoader;

public class Main extends Application {
//SuFrame.fxml
    @Override
    public void start(Stage primaryStage) {
        try {
            SceneLoader.setPrimaryStage(primaryStage);
            SceneLoader.loadScene("/FXML/LoginPage.fxml", null);
            primaryStage.setTitle("Inventory Management System");
            //primaryStage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace(); // Print full error for debugging
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
