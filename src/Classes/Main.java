package Classes;

import javafx.application.Application;
import javafx.stage.Stage;
import Controllers.SceneLoader;

/*
 * Main entry point of the JavaFX application.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneLoader.setPrimaryStage(primaryStage);
        SceneLoader.loadScene("/FXML/SuFrame.fxml", null);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
