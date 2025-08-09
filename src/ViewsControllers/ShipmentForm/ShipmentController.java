package ViewsControllers.ShipmentForm;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Models.ShipmentQRPayload;
import Services.ProductsService;
import Services.ShipmentServices;
import Services.WareHouseService;
import Utilities.AlertUtils;
import Utilities.QRCodeUtils;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;

public class ShipmentController {

    public enum ShipmentKind { RECEPTION, EXPEDITION }

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
    @FXML private Button SaveButton, CancelButton, AddBtn, EditBtn, RemoveBtn;
    @FXML private ComboBox<Product> ExpadistionComboBox; // keep FXML id
    @FXML private Button QRCodeGeneraore, QrCodeReader;
    @FXML private CheckBox OutsideOfNetworkCheckBox;
    @FXML private TextField OutsideOfNetworkTxt;
    @FXML private ProgressIndicator progressIndicator;

    private ShipmentFormHandler formHandler;
    private final WarehouseComboHelper comboHelper = new WarehouseComboHelper();
    private final UiStateManager uiState = new UiStateManager();
    private final QrShipmentMapper qrMapper = new QrShipmentMapper();

    private boolean suppressUi = false;

    @FXML
    public void initialize() {
        formHandler = new ShipmentFormHandler(
                ProductsListView, TotalQuantityTxt, TotalPriceTxtField,
                UnitPriceField, ItemCodeTxt, NameTxtField, QuantityTxt
        );
        formHandler.setProgressIndicator(progressIndicator);

        setupWarehouses();
        setupProducts();
        setupRadioButtonsAndBindings();
        setupCheckBox();
        setupButtons();
        setupQuantityFormatter();

        QrCodeReader.setOnAction(e -> onLoadQRCodeClick());

        ShipmentType.selectToggle(ReceptionRadioButton);
        toggleProgress(false);
    }

    /* ---------- Initialization helpers ---------- */

    private void setupWarehouses() {
        if (Session.getAllWarehouses() == null) {
            WareHouseService.getAllWarehouses();
        }
        ArrayList<Warehouse> warehouses = Session.getAllWarehouses();
        comboHelper.setupWarehouseComboBox(SourceComboBox, warehouses);
        comboHelper.setupWarehouseComboBox(DestinationComboBox, warehouses);

        Warehouse current = Session.getCurrentWarehouse();
        if (current != null) {
            comboHelper.selectById(SourceComboBox, current.getId());
            comboHelper.selectById(DestinationComboBox, current.getId());
        } else if (!warehouses.isEmpty()) {
            SourceComboBox.getSelectionModel().selectFirst();
            DestinationComboBox.getSelectionModel().selectFirst();
        }
    }

    private void setupProducts() {
        if (Session.getProducts() == null) {
            ProductsService.getProducts();
        }
        ArrayList<Product> products = Session.getProducts();

        comboHelper.setupProductComboBox(ExpadistionComboBox, products);

        ExpadistionComboBox.setOnAction(e -> {
            Product selected = ExpadistionComboBox.getValue();
            if (selected != null) {
                formHandler.autoFillFromProduct(selected);
            } else {
                NameTxtField.clear();
                NameTxtField.setDisable(false);
                UnitPriceField.clear();
                UnitPriceField.setDisable(false);
            }
        });
    }

    private void setupRadioButtonsAndBindings() {
        ReceptionRadioButton.setUserData(ShipmentKind.RECEPTION);
        ExpeditionRadioButton.setUserData(ShipmentKind.EXPEDITION);
        ShipmentType.selectedToggleProperty().addListener((obs, oldT, newT) -> applyUiState());
        applyUiState();
    }

    private void setupCheckBox() {
        OutsideOfNetworkCheckBox.selectedProperty().addListener((obs, oldVal, isChecked) -> {
            if (!isChecked) {
                OutsideOfNetworkTxt.clear();
            }
            applyUiState();
        });
        applyUiState();
    }

    private void setupButtons() {
        AddBtn.setOnAction(event -> {
            if (currentKind() == ShipmentKind.RECEPTION) {
                formHandler.handleReception(ItemCodeTxt.getText(), QuantityTxt.getText());
            } else {
                formHandler.handleExpedition(ExpadistionComboBox.getValue(), QuantityTxt.getText());
            }
        });

        SaveButton.setOnAction(event -> handleSave());
        CancelButton.setOnAction(event -> formHandler.reset());
        EditBtn.setOnAction(event -> handleEdit());
        RemoveBtn.setOnAction(event -> formHandler.removeItem());
        updateQRCodeButton();
    }

    private void setupQuantityFormatter() {
        TextFormatter<String> tf = new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,9}") ? change : null);
        QuantityTxt.setTextFormatter(tf);
    }

    /* ---------- UI state ---------- */

    private ShipmentKind currentKind() {
        Toggle t = ShipmentType.getSelectedToggle();
        if (t == null) {
            return ShipmentKind.RECEPTION;
        }
        return (ShipmentKind) t.getUserData();
    }

    private void setVisibleAndManaged(Control c, boolean v) {
        c.setVisible(v);
        c.setManaged(v);
    }

    private void applyUiState() {
        if (suppressUi) {
            return;
        }

        ShipmentKind kind = currentKind();
        boolean outside = OutsideOfNetworkCheckBox.isSelected();
        Warehouse current = Session.getCurrentWarehouse();

        uiState.apply(kind,
                outside,
                current,
                SourceComboBox,
                DestinationComboBox,
                ItemCodeTxt,
                ExpadistionComboBox,
                OutsideOfNetworkTxt);

        updateQRCodeButton();
    }

    private void updateQRCodeButton() {
        if (currentKind() == ShipmentKind.RECEPTION) {
            QRCodeGeneraore.setText("Generate Reception QR");
            QRCodeGeneraore.setOnAction(e -> generateQRCodeForReception());
        } else {
            QRCodeGeneraore.setText("Generate Expedition QR");
            QRCodeGeneraore.setOnAction(e -> generateQRCodeForExpedition());
        }
    }

    /* ---------- QR generation ---------- */

    private void generateQRCodeForReception() {
        Warehouse current = Session.getCurrentWarehouse();
        if (current == null) {
            AlertUtils.showWarning("No Current Warehouse", "Please select a current warehouse.");
            return;
        }

        Warehouse destination = current;
        Warehouse source = OutsideOfNetworkCheckBox.isSelected() ? null : SourceComboBox.getValue();
        String outsideName = OutsideOfNetworkTxt.getText().trim();

        File file = choosePngSaveLocation("Save Reception QR");
        if (file == null) return;

        formHandler.generateQRCode(
                source,
                destination,
                !OutsideOfNetworkCheckBox.isSelected(),
                outsideName,
                mapToQr(ShipmentKind.RECEPTION),
                file.getAbsolutePath()
        );
    }

    private void generateQRCodeForExpedition() {
        Warehouse current = Session.getCurrentWarehouse();
        if (current == null) {
            AlertUtils.showWarning("No Current Warehouse", "Please select a current warehouse.");
            return;
        }

        Warehouse source = current;
        Warehouse destination = OutsideOfNetworkCheckBox.isSelected() ? null : DestinationComboBox.getValue();
        String outsideName = OutsideOfNetworkTxt.getText().trim();

        File file = choosePngSaveLocation("Save Expedition QR");
        if (file == null) return;

        formHandler.generateQRCode(
                source,
                destination,
                !OutsideOfNetworkCheckBox.isSelected(),
                outsideName,
                mapToQr(ShipmentKind.EXPEDITION),
                file.getAbsolutePath()
        );
    }

    private File choosePngSaveLocation(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        return fc.showSaveDialog(null);
    }

    /* ---------- QR decoding with integrity check ---------- */

    public void decodeQRCodeAndLoadForm(String filePath) {
        ShipmentQRPayload payload = QRCodeUtils.decodeShipmentQRCode(filePath);
        if (payload == null) {
            AlertUtils.showError("QR Code Error", "Could not decode or parse QR code.");
            return;
        }

        Warehouse current = Session.getCurrentWarehouse();
        int currentId = current != null ? current.getId() : -1;

        ShipmentKind kind = qrMapper.decideLocalKind(payload, currentId);

        Integer declaredDestId = qrMapper.parsePositiveInt(payload.destination());
        if (declaredDestId != null && declaredDestId > 0 && declaredDestId != currentId) {
            boolean proceed = AlertUtils.showConfirmation(
                    "Shipment Not Intended For This Warehouse",
                    "The QR indicates destination warehouse ID " + declaredDestId +
                            ", which does not match this warehouse (ID " + currentId + ").\n\n" +
                            "Do you want to force entry into this warehouse?");
            if (!proceed) {
                return;
            }
        }

        suppressUi = true;
        try {
            SourceComboBox.getSelectionModel().clearSelection();
            DestinationComboBox.getSelectionModel().clearSelection();
            OutsideOfNetworkTxt.clear();

            ShipmentType.selectToggle(kind == ShipmentKind.RECEPTION ? ReceptionRadioButton : ExpeditionRadioButton);

            if (kind == ShipmentKind.RECEPTION) {
                if (currentId > 0) comboHelper.selectById(DestinationComboBox, currentId);
                DestinationComboBox.setDisable(true);

                QrShipmentMapper.Partner partner = qrMapper.resolvePartnerForReception(payload);
                if (partner instanceof QrShipmentMapper.PartnerId pid) {
                    comboHelper.selectById(SourceComboBox, pid.id());
                    SourceComboBox.setDisable(false);
                    OutsideOfNetworkCheckBox.setSelected(false);
                } else if (partner instanceof QrShipmentMapper.PartnerExternal pext) {
                    OutsideOfNetworkTxt.setText(pext.name());
                    SourceComboBox.setDisable(true);
                    OutsideOfNetworkCheckBox.setSelected(true);
                }
            } else {
                if (currentId > 0) comboHelper.selectById(SourceComboBox, currentId);
                SourceComboBox.setDisable(true);

                QrShipmentMapper.Partner partner = qrMapper.resolvePartnerForExpedition(payload);
                if (partner instanceof QrShipmentMapper.PartnerId pid) {
                    comboHelper.selectById(DestinationComboBox, pid.id());
                    DestinationComboBox.setDisable(false);
                    OutsideOfNetworkCheckBox.setSelected(false);
                } else if (partner instanceof QrShipmentMapper.PartnerExternal pext) {
                    OutsideOfNetworkTxt.setText(pext.name());
                    DestinationComboBox.setDisable(true);
                    OutsideOfNetworkCheckBox.setSelected(true);
                }
            }
        } finally {
            suppressUi = false;
            applyUiState();
            updateQRCodeButton();
        }

        ArrayList<Product> products = new ArrayList<>();
        ArrayList<Integer> quantities = new ArrayList<>();
        payload.items().forEach(entry -> {
            products.add(formHandler.fromItemEntry(entry));
            quantities.add(entry.quantity());
        });
        formHandler.setItems(products, quantities);

        AlertUtils.showSuccess("Shipment form loaded from QR code.");
    }

    /* ---------- Save / Edit / Misc ---------- */

    private void selectWarehouseById(ComboBox<Warehouse> combo, int id) {
        comboHelper.selectById(combo, id);
    }

    private void onLoadQRCodeClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select QR Code Image");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            decodeQRCodeAndLoadForm(file.getAbsolutePath());
        }
    }

    private void handleSave() {
        ShipmentKind kind = currentKind();
        boolean isReception = (kind == ShipmentKind.RECEPTION);
        boolean outside = OutsideOfNetworkCheckBox.isSelected();

        Warehouse source = SourceComboBox.getValue();
        Warehouse destination = DestinationComboBox.getValue();

        if (isReception && outside) {
            source = null;
        } else if (!isReception && outside) {
            destination = null;
        }

        ArrayList<Product> items = formHandler.getItems();
        ArrayList<Integer> quantities = formHandler.getQuantities();
        int totalQty = formHandler.getTotalQuantity();
        float totalPrice = formHandler.getTotalPrice();

        if (items.isEmpty()) {
            AlertUtils.showWarning("No Items", "Please add at least one product before saving.");
            return;
        }
        if (!isReception && source != null && destination != null && source.equals(destination)) {
            AlertUtils.showWarning("Invalid Warehouses", "Source and destination cannot be the same.");
            return;
        }

        toggleProgress(true);

        Warehouse finalSource = source;
        Warehouse finalDestination = destination;

        Task<Void> shipmentTask = new Task<>() {
            @Override protected Void call() {
                if (isReception) {
                    ShipmentServices.reception(finalSource, finalDestination, items, quantities, totalQty, totalPrice);
                } else {
                    ShipmentServices.expedition(finalSource, finalDestination, items, quantities, totalQty, totalPrice);
                }
                return null;
            }
        };

        shipmentTask.setOnRunning(e -> SaveButton.setDisable(true));
        shipmentTask.setOnSucceeded(e -> {
            toggleProgress(false);
            formHandler.reset();
            SaveButton.setDisable(false);
            AlertUtils.showSuccess("Shipment completed successfully.");
        });
        shipmentTask.setOnFailed(e -> {
            toggleProgress(false);
            SaveButton.setDisable(false);
            Throwable ex = shipmentTask.getException();
            String msg = (ex != null) ? ex.getMessage() : "Unknown error";
            AlertUtils.showError("Error", "Failed to process shipment: " + msg);
        });

        new Thread(shipmentTask).start();
    }

    private void handleEdit() {
        String selectedEntry = ProductsListView.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            AlertUtils.showWarning("No Selection", "Please select a product to edit.");
            return;
        }

        String[] parts = selectedEntry.split(" \\| ");
        if (parts.length < 3) {
            AlertUtils.showError("Invalid Format", "The selected entry is not valid.");
            return;
        }

        String itemCode = parts[0].trim();
        String qtyPart = parts[2].trim();
        String quantity = qtyPart.startsWith("Qty:") ? qtyPart.substring(4).trim() : null;

        if (quantity == null) {
            AlertUtils.showError("Invalid Format", "Cannot extract quantity.");
            return;
        }

        if (currentKind() == ShipmentKind.RECEPTION) {
            ItemCodeTxt.setText(itemCode);
            ItemCodeTxt.setDisable(true);
            QuantityTxt.setText(quantity);
        } else {
            for (Product p : ExpadistionComboBox.getItems()) {
                if (p.getItemCode().equalsIgnoreCase(itemCode)) {
                    ExpadistionComboBox.getSelectionModel().select(p);
                    formHandler.autoFillFromProduct(p);
                    break;
                }
            }
            QuantityTxt.setText(quantity);
        }

        ProductsListView.getItems().remove(selectedEntry);
        formHandler.removeItem(itemCode);
    }

    private void toggleProgress(boolean visible) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(visible);
        }
    }

    private String mapToQr(ShipmentKind kind) {
        if (kind == ShipmentKind.RECEPTION) {
            return "Expedition";
        }
        return "Reception";
    }
}
