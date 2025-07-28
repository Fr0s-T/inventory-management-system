package Controllers;

import Classes.DataBaseConnection;
import Classes.HashingUtility;
import Classes.User;
import Classes.Session;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
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

import java.sql.*;

/**
 * Author: @Frost
 * JavaFX Login Page Controller and App Entry
 */

public class LoginPage extends Application {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    @FXML private Pane logoPane;

    private int loginAttempts = 0;
    private Stage mainStage;

    @FXML
    private void login() {
        if (loginAttempts >= 3) {
            loginButton.setDisable(true);
            showError("Too many failed attempts. Please try again later.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        errorLabel.setVisible(false);
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        performLoginAsync(username, password);
    }

    private boolean validateInputs() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            errorLabel.setVisible(true);
            return false;
        }
        return true;
    }

    private void performLoginAsync(String username, String password) {
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                String hashedPassword = HashingUtility.md5Hash(password);
                return authenticateUser(username, hashedPassword);
            }
        };

        loginTask.setOnSucceeded(_ -> handleLoginSuccess(loginTask.getValue()));
        loginTask.setOnFailed(_ -> handleLoginFailure());

        new Thread(loginTask).start();
    }

    private void handleLoginSuccess(User user) {
        Platform.runLater(() -> {
            if (user != null) {
                loginAttempts = 0;
                Session.setCurrentUser(user);
                switchSceneBasedOnRole(user);
            } else {
                loginAttempts++;
                if (loginAttempts >= 3) {
                    showError("Too many failed attempts. Your account has been temporarily locked.");
                    loginButton.setDisable(true);
                } else {
                    showError("Username or Password is incorrect!");
                }
            }
        });
    }

    private void handleLoginFailure() {
        Platform.runLater(() -> {
            loginAttempts++;
            if (loginAttempts >= 3) {
                showError("Too many failed attempts. Please try again later.");
                loginButton.setDisable(true);
            } else {
                showError("Invalid credentials. Attempt " + loginAttempts + " of 3.");
            }
        });
    }

    private void switchSceneBasedOnRole(User user) {
        try {
            switch (user.getRole()) {
                case REGIONAL_MANAGER -> SceneLoader.loadScene("/FXML/SuFrame.fxml", mainStage);
                case WAREHOUSE_MANAGER -> SceneLoader.loadScene("/FXML/WarehouseManagerDashboard.fxml", mainStage);
                default -> {
                    showError("Unknown user role or scene not available.");
                    loginButton.setDisable(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load scene for user role.");
            loginButton.setDisable(false);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private User authenticateUser(String username, String hashedPassword) {
        final String sql = "SELECT * FROM Employee WHERE Username = ?";

        try {
            try (
                    Connection connection = DataBaseConnection.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)
            ) {
                statement.setString(1, username);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    int failedAttempts = rs.getInt("FailedAttempts");
                    Timestamp lockoutUntil = rs.getTimestamp("LockoutUntil");
                    String storedPassword = rs.getString("Password");

                    Timestamp now = new Timestamp(System.currentTimeMillis());

                    if (lockoutUntil != null && lockoutUntil.after(now)) {
                        System.out.println("Account locked until: " + lockoutUntil);
                        return null;
                    }

                    if (hashedPassword.equals(storedPassword)) {
                        try (PreparedStatement resetStmt = connection.prepareStatement(
                                "UPDATE Employee SET FailedAttempts = 0, LockoutUntil = NULL WHERE Username = ?")) {
                            resetStmt.setString(1, username);
                            resetStmt.executeUpdate();
                        }

                        return new User(
                                rs.getInt("ID"),
                                rs.getString("FirstName"),
                                rs.getString("MiddleName"),
                                rs.getString("LastName"),
                                rs.getString("Username"),
                                rs.getInt("RoleID"),
                                rs.getInt("WarehouseID"),
                                rs.getString("Picture"),
                                0,
                                null
                        );
                    } else {
                        failedAttempts++;
                        if (failedAttempts >= 3) {
                            Timestamp lockUntil = new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000);
                            try (PreparedStatement updateStmt = connection.prepareStatement(
                                    "UPDATE Employee SET FailedAttempts = ?, LockoutUntil = ? WHERE Username = ?")) {
                                updateStmt.setInt(1, failedAttempts);
                                updateStmt.setTimestamp(2, lockUntil);
                                updateStmt.setString(3, username);
                                updateStmt.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement updateStmt = connection.prepareStatement(
                                    "UPDATE Employee SET FailedAttempts = ? WHERE Username = ?")) {
                                updateStmt.setInt(1, failedAttempts);
                                updateStmt.setString(2, username);
                                updateStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);

        loginButton.setOnAction(_ -> login());

        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) passwordField.requestFocus();
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // 1. Visual feedback
                String originalStyle = loginButton.getStyle();
                loginButton.setStyle(
                        "-fx-background-color: #004aab; " +
                                "-fx-text-fill: white; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 1);"
                );

                // 2. Add ripple effect
                ScaleTransition scaleIn = getScaleTransition(originalStyle);
                scaleIn.play();
            }
        });

        if (logoPane != null) {
            logoPane.setVisible(true);
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(_ -> logoPane.setVisible(false));
            delay.play();
        }

        Platform.runLater(() -> usernameField.requestFocus());

        exitButton.setOnAction(_ -> Platform.exit());
    }

    private ScaleTransition getScaleTransition(String originalStyle) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), loginButton);
        scaleIn.setToX(0.98);
        scaleIn.setToY(0.98);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), loginButton);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        // 3. Chain animations
        scaleIn.setOnFinished(_ -> {
            scaleOut.play();
            scaleOut.setOnFinished(_ -> {
                loginButton.setStyle(originalStyle);
                login();
            });
        });
        return scaleIn;
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginPage.fxml"));
        loader.setController(this);
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
