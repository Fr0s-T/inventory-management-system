package Controllers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
//TODO:1- Add a password logo
//     2- Password Management and security checks
//     3- Add alert feature if !password 3 times
/*
 *
 *
 * Author: @Frost
 *
 */

public class LoginPage extends Application {

    @FXML private TextField u1;
    @FXML private PasswordField pass1;
    @FXML private Label e1;
    @FXML private Button b1;
    @FXML private Button b2;


    @FXML
    private void login() {
        String username = u1.getText();
        String password = pass1.getText();
        e1.setVisible(!"admin".equals(username) || !"admin".equals(password));
    }
    @FXML
    private void initialize() {
        // Initialize UI components
        e1.setVisible(false);

        // Either use this programmatic approach (remove onAction from FXML)
        b1.setOnAction(event -> login());
        b2.setOnAction(event -> Platform.exit());

        // OR use the FXML onAction="#login" (remove this setOnAction)
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginPage.fxml"));
        AnchorPane root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}