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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class LoginPage extends Application {
    @FXML private TextField u1;
    @FXML private PasswordField pass1;
    @FXML private Label e1;
    @FXML private Button b1;
    @FXML private Button b2;
    @FXML private Pane logopane;

    @FXML
    private void login() {
        String username = u1.getText();
        String password = pass1.getText();
        e1.setVisible(!"admin".equals(username) || !"admin".equals(password));
    }

    @FXML
    private void initialize() {
        e1.setVisible(false);
        b1.setOnAction(event -> login());
        b2.setOnAction(event -> Platform.exit());
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginPage.fxml"));
        AnchorPane root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        // Get controller reference after loading
        LoginPage controller = loader.getController();

        // Show logo pane for 1 second then hide it
        if (controller.logopane != null) {
            controller.logopane.setVisible(true);

            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> controller.logopane.setVisible(false));
            delay.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}