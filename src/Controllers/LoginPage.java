package Controllers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
/*
*
* Author: @Frost
*
* */
public class LoginPage extends Application {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    @FXML private Pane logoPane;

    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setVisible(!"admin".equals(username) || !"admin".equals(password));
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        loginButton.setOnAction(event -> login());

        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                login();
            }
        });

        exitButton.setOnAction(event -> Platform.exit());
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginPage.fxml"));
        AnchorPane root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        LoginPage controller = loader.getController();

        if (controller.logoPane != null) {
            controller.logoPane.setVisible(true);
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> controller.logoPane.setVisible(false));
            delay.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
