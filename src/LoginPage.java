import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginPage extends Application {

    @FXML private TextField u1;
    @FXML private TextField p1;
    @FXML private Label e1;
    @FXML private Button b1;


    @FXML
    private void login() {
        String username = u1.getText();
        String password = p1.getText();
        e1.setVisible(!"admin".equals(username) || !"admin".equals(password));
    }
    @FXML
    private void initialize() {
        // Initialize UI components
        e1.setVisible(false);

        // Either use this programmatic approach (remove onAction from FXML)
        b1.setOnAction(event -> login());

        // OR use the FXML onAction="#login" (remove this setOnAction)
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginPage.fxml"));
        AnchorPane root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}