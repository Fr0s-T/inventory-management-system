package ViewsControllers;

import Services.UserService;
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
    @FXML private TextField UsernameField;
    @FXML private CheckBox IsShiftManager;

    private Stage dialogStage;
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void initialize() {

        SaveButton.setOnAction(e -> {
            try {
                UserService.addEmployee(
                        FnameField.getText().trim(),
                        MnameField.getText().trim(),
                        LnameField.getText().trim(),
                        UsernameField.getText().trim(),
                        HashingUtility.md5Hash(PasswordField.getText().trim()),
                        IsShiftManager.isSelected()
                );
                dialogStage.close(); // Optionally close after save
            } catch (SQLException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        CancelButton.setOnAction(e -> dialogStage.close());

        ChangeListener<String> updateListener = (
                obs, oldVal, newVal) -> updateUsernameField();
        FnameField.textProperty().addListener(updateListener);
        LnameField.textProperty().addListener(updateListener);
    }

    private void updateUsernameField() {
        String fname = FnameField.getText().trim();
        String lname = LnameField.getText().trim();

        // Only create username if both fields are NOT empty
        if (!fname.isEmpty() && !lname.isEmpty()) {
            String username = fname.charAt(0) + "." + lname;
            UsernameField.setText(username.toLowerCase());
        } else {
            UsernameField.clear();
        }
    }
}
