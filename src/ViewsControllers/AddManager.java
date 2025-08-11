package ViewsControllers;


import Services.UserService;
import Utilities.AlertUtils;
import Utilities.HashingUtility;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * Author: @Ilia
 *
 */


public class AddManager {

    @FXML private TextField FnameField;
    @FXML private TextField MnameField;
    @FXML private TextField LnameField;
    @FXML private PasswordField PasswordField;
    @FXML private Button SaveButton;
    @FXML private Button CancelButton;
    @FXML private TextField UsernameField;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void initialize() {
        SaveButton.setOnAction(e -> {
            handleAddManager();
            AlertUtils.showSuccess("Manager Added Successfully");
        });
        CancelButton.setOnAction(e -> dialogStage.close());

        // Auto-update UsernameField when Fname or Lname changes
        ChangeListener<String> updateListener = (
                obs, oldVal, newVal) -> updateUsernameField();
        FnameField.textProperty().addListener(updateListener);
        LnameField.textProperty().addListener(updateListener);
    }

    private void handleAddManager() {
        try {
            String fname = FnameField.getText().trim();
            String mname = MnameField.getText().trim();
            String lname = LnameField.getText().trim();
            String username = UsernameField.getText().trim();
            String password = HashingUtility.md5Hash(PasswordField.getText().trim());

            // Add manager with RoleID = 2
            UserService.addManager(fname, mname, lname, username, password);

            dialogStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUsernameField() {
        String fname = FnameField.getText().trim();
        String lname = LnameField.getText().trim();

        if (!fname.isEmpty() && !lname.isEmpty()) {
            String username = fname.charAt(0) + "." + lname;
            UsernameField.setText(username.toLowerCase());
        } else {
            UsernameField.clear();
        }
    }

}

