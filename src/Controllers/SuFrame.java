package Controllers;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Author: @Frost
 *
 */
public class SuFrame extends Application {

    @FXML private Button LogoutBtn;
    private Stage mainStage;

    @FXML
    private void initialize(){
        LogoutBtn.setOnAction(actionEvent -> {
            System.out.println("Button is pressed");
        });
    }


    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        // 1. Load the CORRECT FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/SuFrame.fxml")); // Changed from LoginPage.fxml
        // 2. Set controller
        loader.setController(this);
        // 3. Load the root
        Parent root = loader.load();
        // 4. Setup scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Main Application");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
