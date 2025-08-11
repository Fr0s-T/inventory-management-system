package ViewsControllers;

import Services.UserService;
import Utilities.AlertUtils;
import Utilities.HashingUtility;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 *
 * Author: @Ilia
 *
 */


public class AddEmployeeController {

    @FXML private TextField FnameField;
    @FXML private TextField MnameField;
    @FXML private TextField LnameField;
    @FXML private PasswordField PasswordField;
    @FXML private Button SaveButton;
    @FXML private Button CancelButton;
    @FXML private CheckBox IsShiftManager;

    private Stage dialogStage;
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void initialize() {

        SaveButton.setOnAction(e -> {
            try {
                int newEmployeeId = UserService.addEmployee(
                        FnameField.getText().trim(),
                        MnameField.getText().trim(),
                        LnameField.getText().trim(),
                        HashingUtility.md5Hash(PasswordField.getText().trim()),
                        IsShiftManager.isSelected()
                );
                String generatedUsername = UserService.getUsernameById(newEmployeeId);

                AlertUtils.showSuccess("Employee Added Successfully.\nGenerated Username: " + generatedUsername);
            } catch (SQLException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        CancelButton.setOnAction(e -> dialogStage.close());

    }

}
