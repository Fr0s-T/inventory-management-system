package ViewsControllers;

import Models.Session;
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

        ArrayList<Warehouse> warehouses;
        if (Session.getAllWarehouses() == null) {
            WareHouseService.getAllWarehouses();
        }
        warehouses = Session.getAllWarehouses();

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

        ShipmentType.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == ExpeditionRadioButton) {
                SourceComboBox.getSelectionModel().select(Session.getCurrentWarehouse().getName());
                SourceComboBox.setDisable(true);
                DestinationComboBox.setDisable(false);
            } else if (newToggle == ReceptionRadioButton) {
                DestinationComboBox.getSelectionModel().select(Session.getCurrentWarehouse().getName());
                DestinationComboBox.setDisable(true);
                SourceComboBox.setDisable(false);
            }
        });


        if(ReceptionRadioButton.isSelected()){
            DestinationComboBox.getSelectionModel();

        }
    }


}
