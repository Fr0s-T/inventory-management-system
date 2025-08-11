package ViewsControllers.ShipmentForm;

import ViewsControllers.ShipmentForm.ShipmentUIHandlers.ShipmentUiInitializer;
import ViewsControllers.ShipmentForm.ShipmentUIHandlers.ShipmentUiStateHandler;
import ViewsControllers.ShipmentForm.ShipmentUIHandlers.ShipmentQrHandler;
import ViewsControllers.ShipmentForm.ShipmentUIHandlers.ShipmentSaveHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ProgressIndicator;
import Models.Product;
import Models.Warehouse;

/**
 * Author: @Frost
 * Main controller for shipment form.
 * Delegates actual logic to specialized UI handlers for clarity and maintainability.
 */
public class ShipmentController {

    public Button getRefreshButton() {
        return RefreshButton;
    }

    public void setRefreshButton(Button refreshButton) {
        RefreshButton = refreshButton;
    }

    public enum ShipmentKind { RECEPTION, EXPEDITION }

    // ==== FXML UI Elements ====
    @FXML private TextField NameTxtField;
    @FXML public RadioButton ReceptionRadioButton;
    @FXML public RadioButton ExpeditionRadioButton;
    @FXML private ToggleGroup ShipmentType;
    @FXML private ComboBox<Warehouse> SourceComboBox;
    @FXML private ComboBox<Warehouse> DestinationComboBox;
    @FXML private TextField ItemCodeTxt;
    @FXML private TextField QuantityTxt;
    @FXML private TextField TotalQuantityTxt;
    @FXML private TextField TotalPriceTxtField;
    @FXML private TextField UnitPriceField;
    @FXML private ListView<String> ProductsListView;
    @FXML private Button SaveButton, CancelButton, AddBtn, EditBtn, RemoveBtn, RefreshButton;
    @FXML private ComboBox<Product> ExpadistionComboBox;
    @FXML private Button QRCodeGenerator, QrCodeReader;
    @FXML private CheckBox OutsideOfNetworkCheckBox;
    @FXML private TextField OutsideOfNetworkTxt;
    @FXML private ProgressIndicator progressIndicator;

    // ==== Handlers & Utilities ====
    private final WarehouseComboHelper comboHelper = new WarehouseComboHelper();
    private final UiStateManager uiState = new UiStateManager();
    private final QrShipmentMapper qrMapper = new QrShipmentMapper();
    private ShipmentFormHandler formHandler;

    private final ShipmentUiInitializer uiInitializer = new ShipmentUiInitializer();
    private final ShipmentUiStateHandler uiStateHandler = new ShipmentUiStateHandler();
    private final ShipmentQrHandler qrHandler = new ShipmentQrHandler();
    private final ShipmentSaveHandler saveHandler = new ShipmentSaveHandler();

    @FXML
    public void initialize() {
        formHandler = new ShipmentFormHandler(
            ProductsListView, TotalQuantityTxt, TotalPriceTxtField,
            UnitPriceField, ItemCodeTxt, NameTxtField, QuantityTxt
        );
        formHandler.setProgressIndicator(progressIndicator);

        uiInitializer.setup(this);
    }

    // ==== Getters for Handlers ====
    public ShipmentFormHandler getFormHandler() { return formHandler; }
    public WarehouseComboHelper getComboHelper() { return comboHelper; }
    public UiStateManager getUiState() { return uiState; }
    public QrShipmentMapper getQrMapper() { return qrMapper; }
    public ShipmentUiStateHandler getUiStateHandler() { return uiStateHandler; }
    public ShipmentQrHandler getQrHandler() { return qrHandler; }
    public ShipmentSaveHandler getSaveHandler() { return saveHandler; }

    // ==== Getters for UI Elements ====
    public TextField getNameTxtField() { return NameTxtField; }
    public RadioButton getReceptionRadioButton() { return ReceptionRadioButton; }
    public RadioButton getExpeditionRadioButton() { return ExpeditionRadioButton; }
    public ToggleGroup getShipmentType() { return ShipmentType; }
    public ComboBox<Warehouse> getSourceComboBox() { return SourceComboBox; }
    public ComboBox<Warehouse> getDestinationComboBox() { return DestinationComboBox; }
    public TextField getItemCodeTxt() { return ItemCodeTxt; }
    public TextField getQuantityTxt() { return QuantityTxt; }
    public TextField getTotalQuantityTxt() { return TotalQuantityTxt; }
    public TextField getTotalPriceTxtField() { return TotalPriceTxtField; }
    public TextField getUnitPriceField() { return UnitPriceField; }
    public ListView<String> getProductsListView() { return ProductsListView; }
    public Button getSaveButton() { return SaveButton; }
    public Button getCancelButton() { return CancelButton; }
    public Button getAddBtn() { return AddBtn; }
    public Button getEditBtn() { return EditBtn; }
    public Button getRemoveBtn() { return RemoveBtn; }
    public ComboBox<Product> getExpadistionComboBox() { return ExpadistionComboBox; }
    public Button getQRCodeGenerator() { return QRCodeGenerator; }
    public Button getQrCodeReader() { return QrCodeReader; }
    public CheckBox getOutsideOfNetworkCheckBox() { return OutsideOfNetworkCheckBox; }
    public TextField getOutsideOfNetworkTxt() { return OutsideOfNetworkTxt; }
    public ProgressIndicator getProgressIndicator() { return progressIndicator; }
}


/*
 *
 *
 * there is a set of logic ready to be used to generate qr code for reception but its functionality is ignored for now
 * due to it need to be tested extensively and over minor tweaks in the logic of the reader
 *
 *
 */