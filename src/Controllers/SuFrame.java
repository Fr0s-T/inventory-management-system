package Controllers;

import Classes.DataBaseConnection;
import Classes.HashingUtility;
import Classes.User;
import Classes.Session;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.sql.*;
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
    private Stage mainStage;

    @FXML
    private void Logout(){

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Cpnfirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be redirected to the login screen.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Go back to login page
            SceneLoader.loadScene("/FXML/LoginPage.fxml", null); // Adjust path if needed
        }

    }
    @FXML
    private void Initialize(){
        User user=Session.getCurrentUser();
        UsernameLabel.setText(user.getUsername());

        LogoutBtn.setOnAction(event ->Logout());

    }





    @Override
    public void start(Stage stage) throws Exception{
        mainStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/SuFrame.fxml"));
        loader.setController(this); // Only if SuFrame is controller
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();    }

    public static void main(String[] args) {
        launch(args);
    }
}
