package ViewsControllers;

import Models.User;
import Services.UserService;
import Services.WareHouseService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * Author: @Frost
 *
 */


public class AddWarehouse {
    @FXML private TextField nameField;
    @FXML private ComboBox<User> managerComboBox; // ✅ Changed to User
    @FXML private TextField capacityField;
    @FXML private TextField locationField;
    @FXML private Button SaveButton;
    @FXML private Button CancelButton;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    public void handleAddWarehouse() {
        try {
            String name = nameField.getText();
            String location = locationField.getText();
            int capacity = Integer.parseInt(capacityField.getText());
            User selectedManager = managerComboBox.getValue(); // ✅ Now works

            if (selectedManager != null) {
                WareHouseService.addWarehouse(name, location, capacity, selectedManager.getUsername());
                dialogStage.close();
            } else {
                System.err.println("Please select a manager.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Capacity must be a number");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void initialize() {
        try {
            ArrayList<User> managers = UserService.getWarehouseManagersFromDb();
            managerComboBox.getItems().clear();
            managerComboBox.getItems().addAll(managers); // ✅ Store objects, not strings

            // Display full name in dropdown
            managerComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getFirstName() + " " +
                            item.getLastName() + " " + item.getId());
                }
            });

            // Display selected manager in button cell
            managerComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getFirstName() + " " + item.getLastName());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        SaveButton.setOnAction(actionEvent -> handleAddWarehouse());
        CancelButton.setOnAction(actionEvent -> dialogStage.close());
    }
}
