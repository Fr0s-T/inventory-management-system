package Utilities;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 *
 * Author: @Frost
 *
 */
public class AlertUtils {

    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    public static void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Success", message);
    }

    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    public static boolean showConfirmation(String title, String message) {
        final boolean[] confirmed = {false};

        Runnable dialog = () -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.showAndWait();
            confirmed[0] = (alert.getResult() == ButtonType.OK);
        };

        if (Platform.isFxApplicationThread()) {
            dialog.run();
        } else {
            try {
                Platform.runLater(dialog);
                // Optional: wait for response if needed
            } catch (Exception ignored) {}
        }

        return confirmed[0];
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Runnable dialog = () -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        };

        if (Platform.isFxApplicationThread()) {
            dialog.run();
        } else {
            Platform.runLater(dialog);
        }
    }

}
