package Controllers;

import Classes.DataBaseConnection;
import Classes.HashingUtility;
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

import java.sql.*;
import java.util.Objects;

/*
*
* Author: @Frost
*
* */
public class LoginPage extends Application {
// #c7d3df
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    @FXML private Pane logoPane;


    @FXML
    private void login() {
        final String username = usernameField.getText().trim();
        final String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            errorLabel.setVisible(true);
            return;
        }

        HashingUtility hashingUtility = new HashingUtility();
        final String hashedPassword = hashingUtility.md5Hash(password);

        if (verifyPassword(username, hashedPassword)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login Status");
            alert.setHeaderText(null);
            alert.setContentText("Login successful!");
            alert.showAndWait();

            // Exit or transition here if needed
            Platform.exit(); // or close current window
        } else {
            errorLabel.setText("Username or Password is incorrect!");
            errorLabel.setVisible(true);
        }
    }




    private boolean verifyPassword(String username,String password){
            final String sql = "SELECT Password FROM Employee WHERE Username = ?";

            try {
                DataBaseConnection dataBaseConnection = new DataBaseConnection();
                final String connectionUrl = dataBaseConnection.getConnectionUrl(300);

                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

                try (
                        Connection connection = DriverManager.getConnection(connectionUrl);
                        PreparedStatement statement = connection.prepareStatement(sql)
                ) {
                    statement.setString(1, username);
                    ResultSet rs = statement.executeQuery();

                    if (rs.next()) {
                        String storedPassword = rs.getString("Password");
                        return password.equals(storedPassword);
                    }
                }

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace(); // Or log this in a more secure way
            }

            return false;
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
