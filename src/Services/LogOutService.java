package Services;

import Controllers.SceneLoader;
import Models.Session;
import Utilities.AlertUtils;

import java.util.Optional;

/**
 *
 * Author: @Frost
 *
 */

public class LogOutService {

    public static void Logout() {
        boolean confirmed = AlertUtils.showConfirmation(
                "Logout Confirmation",
                "Are you sure you want to logout?\nYou will be redirected to the login screen."
        );

        if (confirmed) {
            Session.logOut();
            SceneLoader.loadScene("/FXML/LoginPage.fxml", null); // Adjust path if needed
        }
    }

    public static void BackToDashboard() {
        boolean confirmed = AlertUtils.showConfirmation(
                "Back To Regional Manager Dashboard",
                "Are you sure you want to logout?\nYou will be redirected to the Regional Manager Dashboard."
        );

        if (confirmed) {
            Session.BackToDashboard();
            SceneLoader.loadScene("/FXML/SuFrame.fxml", null); // Adjust path if needed
        }
    }
}
