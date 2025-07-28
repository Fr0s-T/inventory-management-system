package Controllers;

import Services.LogInService;
import javafx.animation.ScaleTransition;
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

public class LoginPage extends Application {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    @FXML private Pane logoPane;

    private Stage mainStage;
    private LogInService loginService;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);

        // Initialize login service with the main stage
        loginService = new LogInService(mainStage);

        // Set up login button action
        loginButton.setOnAction(event -> login());

        // Set up keyboard navigation
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) passwordField.requestFocus();
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                triggerLoginAnimation();
            }
        });

        // Logo animation
        if (logoPane != null) {
            logoPane.setVisible(true);
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(_ -> logoPane.setVisible(false));
            delay.play();
        }

        Platform.runLater(() -> usernameField.requestFocus());
        exitButton.setOnAction(_ -> Platform.exit());
    }

    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Disable login button during authentication
        loginButton.setDisable(true);

        // Perform login through the service
        boolean loginInitiated = loginService.login(username, password, errorLabel, loginButton);

        if (!loginInitiated) {
            loginButton.setDisable(false);
        }
    }

    private void triggerLoginAnimation() {
        String originalStyle = loginButton.getStyle();
        loginButton.setStyle(
                "-fx-background-color: #004aab; " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 1);"
        );

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), loginButton);
        scaleIn.setToX(0.98);
        scaleIn.setToY(0.98);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), loginButton);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        scaleIn.setOnFinished(_ -> {
            scaleOut.play();
            scaleOut.setOnFinished(_ -> {
                loginButton.setStyle(originalStyle);
                login();
            });
        });
        scaleIn.play();
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginPage.fxml"));
        loader.setController(this);
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}