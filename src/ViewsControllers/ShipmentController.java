package ViewsControllers;

import Models.Warehouse;
import Services.WareHouseService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShipmentController {

    @FXML private RadioButton ReceptionRadioButton;
    @FXML private RadioButton ExpeditionRadioButton;
    @FXML private ToggleGroup ShipmentType;
    @FXML private ComboBox<String> SourceComboBox;
    @FXML private ComboBox<String> DestinationComboBox;
    @FXML private TextField ItemCodeTxt;
    @FXML private TextField QuantityTxt;
    @FXML private TextField TotalQuantityTxt;
    @FXML private ListView<String> ProductsListView;
    @FXML private Button SaveButton;
    @FXML private Button CancelButton;

    @FXML public void initialize(){

        RadioButton selected = (RadioButton) ShipmentType.getSelectedToggle();

        try {
            List<Warehouse> warehouses = WareHouseService.getWarehousesFromDb();

            // Extract warehouse names
            List<String> warehouseNames = new ArrayList<>();
            for (Warehouse warehouse : warehouses) {
                warehouseNames.add(warehouse.getName());
            }

            // Add to both ComboBoxes
            SourceComboBox.getItems().addAll(warehouseNames);
            DestinationComboBox.getItems().addAll(warehouseNames);

            // Optional: Set default selections
            if (!warehouseNames.isEmpty()) {
                SourceComboBox.getSelectionModel().selectFirst();
                DestinationComboBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            // Optional: show an alert to the user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to load warehouses");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

        if(ReceptionRadioButton.isSelected()){
            DestinationComboBox.getSelectionModel();

        }
    }


}
