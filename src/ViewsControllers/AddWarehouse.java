package ViewsControllers;

import Services.WareHouseService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddWarehouse {
    @FXML private TextField nameField;
    @FXML private TextField managerUsernameField; // You might not need this, if you set manager from session.
    @FXML private TextField capacityField;
    @FXML private TextField locationField;
    @FXML private Button Addbutton;

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

            WareHouseService.addWarehouse(name, location, capacity);
            dialogStage.close(); // Close after adding
        } catch (NumberFormatException e) {
            // Handle invalid capacity input
            System.err.println("Capacity must be a number");
            // Optionally show an alert dialog
        }
    }

    @FXML
    public void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void initialize() {
        // Optional initialization code here
        Addbutton.setOnAction(actionEvent -> WareHouseService.addWarehouse(nameField.getText().trim(),
                locationField.getText().trim(),Integer.parseInt(capacityField.getText().trim())));
    }
}
