package Controllers;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Author: @Frost
 * JavaFX main class that initializes the app and loads the first scene.
 */
public class SuFrame extends Application {

    @Override
    public void start(Stage stage) {
        SceneLoader.setPrimaryStage(stage);
        SceneLoader.loadScene("/FXML/SuFrame.fxml", null); // initial scene
    }

    public static void main(String[] args) {
        launch(args);
    }
}
