package ViewsControllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.*;

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



    }


}
