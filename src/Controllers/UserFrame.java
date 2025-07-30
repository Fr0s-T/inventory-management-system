package Controllers;

import Services.LogOutService;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import Models.*;

/**
 * Author: @Frost
 *
 */
public class UserFrame extends Application {

    @FXML private ImageView UserImage;
    @FXML private Label UsernameLabel;
    @FXML private Label WarehouseLabel;
    @FXML private Button AlertBtn;
    @FXML private Button LogoutBtn;
    @FXML private Button ProductsBtn;
    @FXML private Button ShipmentsBtn;
    @FXML private Button ReportsBtn;
    @FXML private Button UsersBtn;
    private Stage mainStage;


    @FXML
    private void initialize(){

        User user = Session.getCurrentUser();
        //UsernameLabel.setText(user.getUsername());
        LogoutBtn.setOnAction(event -> LogOutService.Logout());

    }


    @Override
    public void start(Stage stage) throws Exception{
        mainStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/UserFrame.fxml"));
        //loader.setController(this);
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
