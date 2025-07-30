package Controllers;

import Services.LogOutService;
import Services.WareHouseService;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import Models.*;
import jdk.jshell.Snippet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Author: @Frost
 *
 */
public class SuFrame extends Application {

    @FXML private ImageView UserImage;
    @FXML private Label UsernameLabel;
    @FXML private Label StatusLabel;
    @FXML private Button AlertBtn;
    @FXML private Button LogoutBtn;
    @FXML private FlowPane flowPane;

    private Stage mainStage;


    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {

        User user = Session.getCurrentUser();
        UsernameLabel.setText(user.getUsername());
        StatusLabel.setTextFill(Paint.valueOf("green"));
        StatusLabel.setText("ONLINE");

        ArrayList<Warehouse> warehouses = WareHouseService.getWarehousesFromDb();
        SceneLoader.loadWarehouseCards(warehouses,flowPane);

        LogoutBtn.setOnAction(event -> LogOutService.Logout());

    }


    @Override
    public void start(Stage stage) throws Exception{
        mainStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/SuFrame.fxml"));
        loader.setController(this); // Only if SuFrame is controller
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
