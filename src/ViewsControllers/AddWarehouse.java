package ViewsControllers;

import Models.Employee;
import Services.ManagerService;
import Services.WareHouseService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;

public class AddWarehouse {
    @FXML private TextField nameField;
    @FXML private ComboBox<Employee> managerComboBox; // You might not need this, if you set manager from session.
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
            Employee selectedManager= managerComboBox.getValue();

            WareHouseService.addWarehouse(name, location, capacity, selectedManager);
            dialogStage.close(); // Close after adding
        } catch (NumberFormatException e) {
            // Handle invalid capacity input
            System.err.println("Capacity must be a number");
            // Optionally show an alert dialog
        }catch (SQLException | ClassNotFoundException e){
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
            ArrayList<Employee> managers = ManagerService.getWarehouseManagersFromDb();
            managerComboBox.getItems().clear();
            managerComboBox.getItems().addAll(managers);

            // Optional: How to display them nicely
            managerComboBox.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Employee item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getFirstName() + " " + item.getLastName());
                }
            });

            managerComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Employee item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getFirstName() + " " + item.getLastName());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        SaveButton.setOnAction(actionEvent -> {
            try {
                String name = nameField.getText().trim();
                String location = locationField.getText().trim();
                int capacity = Integer.parseInt(capacityField.getText().trim());
                Employee selectedManager = managerComboBox.getValue();

                if (selectedManager != null) {
                    WareHouseService.addWarehouse(name, location, capacity, selectedManager);
                    dialogStage.close();
                } else {
                    System.err.println("Please select a manager.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Capacity must be a number");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        CancelButton.setOnAction(actionEvent -> dialogStage.close());
    }
}
