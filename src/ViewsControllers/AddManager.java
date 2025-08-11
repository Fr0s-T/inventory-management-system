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

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void initialize() {
        SaveButton.setOnAction(e ->handleAddManager());
        CancelButton.setOnAction(e -> dialogStage.close());

    }

    private void handleAddManager() {
        try {
            String fname = FnameField.getText().trim();
            String mname = MnameField.getText().trim();
            String lname = LnameField.getText().trim();
            String password = HashingUtility.md5Hash(PasswordField.getText().trim());

            // Add manager with RoleID = 2
            int newManagerId = UserService.addManager(fname, mname, lname, password);
            String generatedUsername = UserService.getUsernameById(newManagerId);

            AlertUtils.showSuccess("Manager Added Successfully.\nGenerated Username: " + generatedUsername);
            dialogStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

