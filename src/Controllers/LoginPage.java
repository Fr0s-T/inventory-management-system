package Controllers;

import Classes.DataBaseConnection;
import Classes.HashingUtility;
import Classes.User;
import Classes.Session;

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

/*
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

//TODO: 1-pass failedAttempt to data and set 0 on successful login
//      2- when failed attempt = 3 lock for 5min send to db and unlock after 5 min

    private void performLoginAsync(String username, String password) {
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
               // HashingUtility hashingUtility = new HashingUtility();
                String hashedPassword = HashingUtility.md5Hash(password);
                return authenticateUser(username, hashedPassword);
            }
        };

        loginTask.setOnSucceeded(event -> handleLoginSuccess(loginTask.getValue()));
        loginTask.setOnFailed(event -> handleLoginFailure());

        new Thread(loginTask).start();
    }

    private void handleLoginSuccess(User user) {
        Platform.runLater(() -> {
            if (user != null) {
                Session.setCurrentUser(user);
                switchSceneBasedOnRole(user);
            } else {
                loginAttempts++;
                if (loginAttempts >= 3) {
                    showError("Too many failed attempts. Please try again later.");
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

/*private void attemptLogin(String username, String password) {
    Task<Void> loginTask = new Task<>() {
        @Override
        protected Void call() {
            try (Connection conn = DataBaseConnection.getConnectionUrl(300)) {
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM Users WHERE Username = ?");
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Timestamp lockoutUntil = rs.getTimestamp("LockoutUntil");
                    int failedAttempts = rs.getInt("FailedAttempts");
                    String storedHash = rs.getString("PasswordHash");

                    Timestamp now = new Timestamp(System.currentTimeMillis());

                    if (lockoutUntil != null && lockoutUntil.after(now)) {
                        Platform.runLater(() -> {
                            showError("Account is locked until: " + lockoutUntil.toLocalDateTime());
                            loginButton.setDisable(true);
                        });
                        return null;
                    }

                    if (HashingUtility.verifyPassword(password, storedHash)) {
                        // Correct password → reset counters
                        PreparedStatement resetStmt = conn.prepareStatement(
                                "UPDATE Users SET FailedAttempts = 0, LockoutUntil = NULL WHERE Username = ?");
                        resetStmt.setString(1, username);
                        resetStmt.executeUpdate();

                        User user = new User(rs); // Assuming your User constructor accepts ResultSet
                        Platform.runLater(() -> {
                            Session.setCurrentUser(user);
                            switchSceneBasedOnRole(user);
                        });

                    } else {
                        failedAttempts++;
                        PreparedStatement updateStmt;
                        if (failedAttempts >= 3) {
                            updateStmt = conn.prepareStatement(
                                    "UPDATE Users SET FailedAttempts = ?, LockoutUntil = DATEADD(MINUTE, 5, GETDATE()) WHERE Username = ?");
                        } else {
                            updateStmt = conn.prepareStatement(
                                    "UPDATE Users SET FailedAttempts = ? WHERE Username = ?");
                        }
                        updateStmt.setInt(1, failedAttempts);
                        updateStmt.setString(2, username);
                        updateStmt.executeUpdate();

                        int attemptsLeft = 3 - failedAttempts;

                        Platform.runLater(() -> {
                            if (failedAttempts >= 3) {
                                showError("Too many failed attempts. Account locked for 5 minutes.");
                                loginButton.setDisable(true);
                            } else {
                                showError("Invalid credentials. " + attemptsLeft + " attempt(s) left.");
                            }
                        });
                    }
                } else {
                    Platform.runLater(() -> showError("User not found."));
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Database error occurred."));
            }

            return null;
        }
    };

    new Thread(loginTask).start();
}
*/


    private void switchSceneBasedOnRole(User user) {
        try {
            switch (user.getRole()) {
                case REGIONAL_MANAGER -> SceneLoader.loadScene("/FXML/SuFrame.fxml", mainStage);
                case WAREHOUSE_MANAGER -> SceneLoader.loadScene("/FXML/WarehouseManagerDashboard.fxml", mainStage);
                // Add other roles here only if their FXML files exist
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
            //DataBaseConnection dataBaseConnection = new DataBaseConnection();
            final String connectionUrl = DataBaseConnection.getConnectionUrl(300);

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            try (
                    Connection connection = DriverManager.getConnection(connectionUrl);
                    PreparedStatement statement = connection.prepareStatement(sql)
            ) {
                statement.setString(1, username);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("Password");
                    if (!hashedPassword.equals(storedPassword)) return null;

                    return new User(
                            rs.getInt("ID"),
                            rs.getString("FirstName"),
                            rs.getString("MiddleName"),
                            rs.getString("LastName"),
                            rs.getString("Username"),
                            rs.getInt("RoleID"),
                            rs.getInt("WarehouseID"),
                            rs.getString("Picture"),
                            rs.getInt("FailedAttempts"),
                            rs.getTimestamp("LockoutUntil")
                    );
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

        loginButton.setOnAction(event -> login());

        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) passwordField.requestFocus();
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) login();
        });

        if (logoPane != null) {
            logoPane.setVisible(true);
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(e -> logoPane.setVisible(false));
            delay.play();
        }

        Platform.runLater(() -> usernameField.requestFocus());

        exitButton.setOnAction(e -> Platform.exit());
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
