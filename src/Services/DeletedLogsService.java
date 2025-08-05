package Services;

import Models.Session;
import Models.User;
import Utilities.AlertUtils;
import Utilities.DataBaseConnection;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;

/**
 *
 * Author: @Frost
 *
 */

public class DeletedLogsService {

    DeletedLogsService() {}

    public static void saveDeletedLogsToAFileAsync(String filepath) {

        if (Session.getCurrentUser().getRole() != User.Role.REGIONAL_MANAGER) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                File file = new File(filepath);
                if (!file.exists()) file.createNewFile();

                try (
                        FileWriter fileWriter = new FileWriter(file, true);
                        Connection connection = DataBaseConnection.getConnection();
                        PreparedStatement statement = connection.prepareStatement("SELECT * FROM DeletedLog");
                        ResultSet rs = statement.executeQuery()
                ) {
                    while (rs.next()) {
                        String logEntry =
                                "LogID: " + rs.getInt("LogID") +
                                        "    Deleted from: " + rs.getString("TableName") +
                                        "    Deleted By: " + rs.getString("DeletedBy") +
                                        "    Deleted At: " + rs.getTimestamp("DeletedAt") +
                                        "\nDeleted Record: " + rs.getString("RecordData") +
                                        "\n\n";
                        fileWriter.write(logEntry);
                    }
                }

                // Show success alert on the JavaFX Application Thread
                Platform.runLater(() -> {
                    Throwable ex = getException();
                    Platform.runLater(() -> AlertUtils.showError(
                            "Error",
                            "Could not save deleted logs.\n" + ex.getMessage()
                    ));
                });

                return null;
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Could not save deleted logs.");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });
            }
        };

        // Start the task on a new thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
