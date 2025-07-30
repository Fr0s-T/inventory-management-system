package Services;

import Controllers.SceneLoader;
import Models.Session;
import Models.User;
import Utilities.DataBaseConnection;
import Utilities.HashingUtility;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.*;

/**
 *
 * Author: @Frost
 *
 */

public class LogInService {
    private Button loginButton;
    private Label errorLabel;
    private Stage mainStage;

    public LogInService(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public boolean login(String username, String password, Label errorLabel, Button loginButton) {
        this.loginButton = loginButton;
        this.errorLabel = errorLabel;

        if (!validateInputs(username, password)) {
            return false;
        }

        errorLabel.setVisible(false);
        performLoginAsync(username.trim(), password.trim());
        return true;
    }

    private boolean validateInputs(String username, String password) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showError("Please enter both username and password.");
            return false;
        }
        return true;
    }

    private void performLoginAsync(String username, String password) {
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                String hashedPassword = HashingUtility.md5Hash(password);
                return authenticateUser(username, hashedPassword);
            }
        };

        loginTask.setOnSucceeded(event -> {
            User user = loginTask.getValue();
            handleLoginSuccess(user);
        });

        loginTask.setOnFailed(event -> {
            handleLoginFailure(loginTask.getException());
        });

        new Thread(loginTask).start();
    }

    private void handleLoginFailure(Throwable exception) {
        Platform.runLater(() -> {
            showError(exception.getMessage());
            loginButton.setDisable(false);
        });
    }

    private void handleLoginSuccess(User user) {
        Platform.runLater(() -> {
            Session.setCurrentUser(user);
            switchSceneBasedOnRole(user);
        });
    }

    private void switchSceneBasedOnRole(User user) {
        try {
            switch (user.getRole()) { // Assuming getRoleID() returns the role integer
                case REGIONAL_MANAGER: // Regional Manager
                    SceneLoader.loadScene("/FXML/SuFrame.fxml", mainStage);
                    break;
                case WAREHOUSE_MANAGER: // Warehouse Manager
                    SceneLoader.loadScene("/FXML/WarehouseManagerDashboard.fxml", mainStage);
                    break;
                default:
                    showError("Unknown user role or scene not available.");
                    loginButton.setDisable(false);
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

    private User authenticateUser(String username, String hashedPassword) throws SQLException {
        final String sql = "SELECT * FROM Employee WHERE Username = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                if (isAccountLocked(rs)) {
                    throw new SQLException("Account locked until: " + rs.getTimestamp("LockoutUntil"));
                }

                String storedPassword = rs.getString("Password");

                if (hashedPassword.equals(storedPassword)) {
                    resetFailedAttempts(connection, username);
                    return buildUserFromResultSet(rs);
                } else {
                    incrementFailedAttempts(connection, rs.getInt("FailedAttempts"), username);
                    throw new SQLException("Invalid username or password");
                }
            }
            throw new SQLException("User not found");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isAccountLocked(ResultSet rs) throws SQLException {
        Timestamp lockoutUntil = rs.getTimestamp("LockoutUntil");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return lockoutUntil != null && lockoutUntil.after(now);
    }

    private void resetFailedAttempts(Connection connection, String username) throws SQLException {
        String update = "UPDATE Employee SET FailedAttempts = 0, LockoutUntil = NULL WHERE Username = ?";
        try (PreparedStatement resetStmt = connection.prepareStatement(update)) {
            resetStmt.setString(1, username);
            resetStmt.executeUpdate();
        }
    }

    private void incrementFailedAttempts(Connection connection, int failedAttempts, String username) throws SQLException {
        failedAttempts++;

        if (failedAttempts >= 3) {
            Timestamp lockUntil = new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000); // 5 minutes
            String update = "UPDATE Employee SET FailedAttempts = ?, LockoutUntil = ? WHERE Username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(update)) {
                stmt.setInt(1, failedAttempts);
                stmt.setTimestamp(2, lockUntil);
                stmt.setString(3, username);
                stmt.executeUpdate();
            }
        } else {
            String update = "UPDATE Employee SET FailedAttempts = ? WHERE Username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(update)) {
                stmt.setInt(1, failedAttempts);
                stmt.setString(2, username);
                stmt.executeUpdate();
            }
        }
    }

    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
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
                rs.getTimestamp("LockoutUntil"),
                rs.getBoolean("IsLoggedIn")
        );
    }
}