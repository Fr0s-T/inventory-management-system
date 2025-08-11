package ViewsControllers;

import Models.Session;
import Models.User;
import Services.EditUserServices;
import Services.UserService;
import Utilities.HashingUtility;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.nio.Buffer;

/**
 *
 * Author: @Ilia
 *
 */


public class UsersController {

    @FXML private TextField IDtxt;
    @FXML private TextField Fnametxt;
    @FXML private TextField Mnametxt;
    @FXML private TextField Lnametxt;
    @FXML private TextField Usernametxt;
    @FXML private PasswordField Passwordtxt;
    @FXML private CheckBox ShiftManagerCheckBox;
    @FXML private CheckBox StatusCheckBox;
    @FXML private Button FetchBtn;
    @FXML private Button SaveBtn;
    @FXML private Button ClearBtn;
    private User fetchedUser= null;

    @FXML public void initialize(){
        SaveBtn.setDisable(true);

        FetchBtn.setOnAction(actionEvent -> {
            try {
                ShiftManagerCheckBox.setVisible(true);
                int userId = Integer.parseInt(IDtxt.getText());
                User user = EditUserServices.fetchUser(userId);

                User.Role currentRole = Session.getCurrentUser().getRole();

                if (currentRole == User.Role.REGIONAL_MANAGER) {
                    // Block only if target user is Regional Manager
                    if (user.getRole() == User.Role.REGIONAL_MANAGER) {
                        showAccessDenied("You don't have permission to access this user's information.");
                        return;
                    } else if (user.getRole()== User.Role.WAREHOUSE_MANAGER) {
                        ShiftManagerCheckBox.setVisible(false);
                    }
                } else if (currentRole == User.Role.WAREHOUSE_MANAGER) {

                    if (user.getRole() == User.Role.WAREHOUSE_MANAGER || user.getRole() == User.Role.REGIONAL_MANAGER) {
                        showAccessDenied("You don't have permission to access this user's information.");
                        return;
                    }

                }
                if (user.getWarehouseId() != Session.getCurrentWarehouse().getId()) {
                    showAccessDenied("This user does not work in your warehouse.");
                    return;
                }


                Fnametxt.setText(user.getFirstName());
                Mnametxt.setText(user.getMiddleName());
                Lnametxt.setText(user.getLastName());
                Usernametxt.setText(user.getUsername());

                ShiftManagerCheckBox.setSelected(user.getRole() == User.Role.SHIFT_MANAGER);
                StatusCheckBox.setSelected(Boolean.TRUE.equals(user.getOnDuty()));
                SaveBtn.setDisable(false);
                fetchedUser = user;

            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText("ID Format Error");
                alert.setContentText("Please enter a valid numeric user ID.");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Unexpected Error");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });


        ClearBtn.setOnAction(actionEvent ->{
            IDtxt.clear();
            Fnametxt.clear();
            Mnametxt.clear();
            Lnametxt.clear();
            Usernametxt.clear();
            Passwordtxt.clear();
            ShiftManagerCheckBox.setSelected(false);
            StatusCheckBox.setSelected(false);
        } );
        SaveBtn.setOnAction(actionEvent -> {
            try {
                int id = Integer.parseInt(IDtxt.getText());
                String fname = Fnametxt.getText();
                String mname = Mnametxt.getText();
                String lname = Lnametxt.getText();
                String rawPassword = Passwordtxt.getText();
                boolean onDuty = StatusCheckBox.isSelected();
                int newRoleId = ShiftManagerCheckBox.isSelected() ? 3 : 4;
                if (!ShiftManagerCheckBox.isVisible()) {
                    newRoleId = 2;
                }

                String hashedPassword = (rawPassword != null && !rawPassword.trim().isEmpty())
                        ? HashingUtility.md5Hash(rawPassword)
                        : "";

                // Get the current role before updating
                int currentRoleId = fetchedUser.getRole().getId();

                // If role has changed, update hierarchy
                if (currentRoleId != newRoleId) {
                    // 1. Set EndDate on the current record
                    EditUserServices.endCurrentHierarchy(id);

                    // 2. Insert new hierarchy row
                    int managerId = Session.getCurrentUser().getId();
                    EditUserServices.insertToHierarchy(id, newRoleId, managerId);
                }

                // Update employee info
                EditUserServices.updateEmployee(id, fname, mname, lname, newRoleId, onDuty, hashedPassword);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("User updated");
                alert.setContentText("Employee data updated successfully.");
                alert.showAndWait();
                SaveBtn.setDisable(true);

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to update employee");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });


    }

    private void showAccessDenied(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Permission Denied");
        alert.setContentText(message);
        alert.showAndWait();
    }


}
