package Services;

import Controllers.SceneLoader;
import Models.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 *
 * Author: @Frost
 *
 */


public class LogOutService {


    public static void Logout() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be redirected to the login screen.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Session.logOut();
            SceneLoader.loadScene("/FXML/LoginPage.fxml", null); // Adjust path if needed
        }
    }
    public static void BackToDashboard(){

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Back To Regional Manager Dashboard");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be redirected to the Regional Manager Dashboard");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Session.BackToDashboard();
            SceneLoader.loadScene("/FXML/SuFrame.fxml", null); // Adjust path if needed
        }
    }
}
