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
import javafx.util.StringConverter;

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
    @FXML private ComboBox<Product> ExpadistionComboBox; // keep name to match FXML
    @FXML private Button QRCodeGeneraore, QrCodeReader;
    @FXML private CheckBox OutsideOfNetworkCheckBox;
    @FXML private TextField OutsideOfNetworkTxt;
    @FXML private ProgressIndicator progressIndicator;

    private ShipmentFormHandler formHandler;
    private boolean suppressUi = false; // guard during programmatic changes

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
        setupWarehouseComboBox(SourceComboBox, warehouses);
        setupWarehouseComboBox(DestinationComboBox, warehouses);

        Warehouse current = Session.getCurrentWarehouse();
        if (current != null) {
            selectWarehouseById(SourceComboBox, current.getId());
            selectWarehouseById(DestinationComboBox, current.getId());
        } else if (!warehouses.isEmpty()) {
            SourceComboBox.getSelectionModel().selectFirst();
            DestinationComboBox.getSelectionModel().selectFirst();
        }
    }

    private void setupWarehouseComboBox(ComboBox<Warehouse> combo, ArrayList<Warehouse> warehouses) {
        combo.getItems().setAll(warehouses);

        combo.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(Warehouse w, boolean empty) {
                super.updateItem(w, empty);
                setText(empty || w == null ? "" : safeName(w));
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Warehouse w, boolean empty) {
                super.updateItem(w, empty);
                setText(empty || w == null ? "" : safeName(w));
            }
        });
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Warehouse w) { return w == null ? "" : safeName(w); }
            @Override public Warehouse fromString(String s) { return null; }
        });
    }

    private static String safeName(Warehouse w) {
        String n = w.getName();
        return (n == null) ? "" : n;
    }

    private void setupProducts() {
        if (Session.getProducts() == null) ProductsService.getProducts();
        ArrayList<Product> products = Session.getProducts();

        ExpadistionComboBox.getItems().setAll(products);
        ExpadistionComboBox.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getItemCode());
            }
        });
        ExpadistionComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getItemCode());
            }
        });

        ExpadistionComboBox.setOnAction(e -> {
            Product selected = ExpadistionComboBox.getValue();
            if (selected != null) {
                formHandler.autoFillFromProduct(selected);
            } else {
                NameTxtField.clear(); NameTxtField.setDisable(false);
                UnitPriceField.clear(); UnitPriceField.setDisable(false);
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
            if (!isChecked) OutsideOfNetworkTxt.clear();
            applyUiState();
        });
        applyUiState();
    }

    private void setupButtons() {
        AddBtn.setOnAction(event -> {
            if (currentKind() == ShipmentKind.RECEPTION)
                formHandler.handleReception(ItemCodeTxt.getText(), QuantityTxt.getText());
            else
                formHandler.handleExpedition(ExpadistionComboBox.getValue(), QuantityTxt.getText());
        });

        SaveButton.setOnAction(event -> handleSave());
        CancelButton.setOnAction(event -> formHandler.reset());
        EditBtn.setOnAction(event -> handleEdit());
        RemoveBtn.setOnAction(event -> formHandler.removeItem());
        updateQRCodeButton();
    }

    private void setupQuantityFormatter() {
        QuantityTxt.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,9}") ? change : null));
    }

    /* ---------- UI state ---------- */

    private ShipmentKind currentKind() {
        Toggle t = ShipmentType.getSelectedToggle();
        return t == null ? ShipmentKind.RECEPTION : (ShipmentKind) t.getUserData();
    }

    private void setVisibleAndManaged(Control c, boolean v) { c.setVisible(v); c.setManaged(v); }

    private void applyUiState() {
        if (suppressUi) return;

        ShipmentKind kind = currentKind();
        boolean outside = OutsideOfNetworkCheckBox.isSelected();
        Warehouse current = Session.getCurrentWarehouse();
        int currentId = current != null ? current.getId() : -1;

        if (kind == ShipmentKind.EXPEDITION) {
            if (currentId > 0) selectWarehouseById(SourceComboBox, currentId);
            SourceComboBox.setDisable(true);

            DestinationComboBox.setDisable(outside);
            OutsideOfNetworkTxt.setDisable(!outside);

            setVisibleAndManaged(ItemCodeTxt, false);
            setVisibleAndManaged(ExpadistionComboBox, true);
        } else {
            if (currentId > 0) selectWarehouseById(DestinationComboBox, currentId);
            DestinationComboBox.setDisable(true);

            SourceComboBox.setDisable(outside);
            OutsideOfNetworkTxt.setDisable(!outside);

            setVisibleAndManaged(ItemCodeTxt, true);
            setVisibleAndManaged(ExpadistionComboBox, false);
        }

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

        Warehouse destination = current; // locked to current
        Warehouse source = OutsideOfNetworkCheckBox.isSelected() ? null : SourceComboBox.getValue();
        String outsideName = OutsideOfNetworkTxt.getText().trim();

        File file = choosePngSaveLocation("Save Reception QR");
        if (file == null) return;

        formHandler.generateQRCode(source, destination, !OutsideOfNetworkCheckBox.isSelected(),
                outsideName, mapToQr(ShipmentKind.RECEPTION), file.getAbsolutePath());
    }

    private void generateQRCodeForExpedition() {
        Warehouse current = Session.getCurrentWarehouse();
        if (current == null) {
            AlertUtils.showWarning("No Current Warehouse", "Please select a current warehouse.");
            return;
        }

        Warehouse source = current; // locked to current
        Warehouse destination = OutsideOfNetworkCheckBox.isSelected() ? null : DestinationComboBox.getValue();
        String outsideName = OutsideOfNetworkTxt.getText().trim();

        File file = choosePngSaveLocation("Save Expedition QR");
        if (file == null) return;

        formHandler.generateQRCode(source, destination, !OutsideOfNetworkCheckBox.isSelected(),
                outsideName, mapToQr(ShipmentKind.EXPEDITION), file.getAbsolutePath());
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

        // Decide local kind using ID-first heuristic with label fallback
        ShipmentKind kind = mapFromQr(payload.getType(), payload);

        // Optional guard: if QR declares a destination id that isn't me, warn
        Integer declaredDestId = parsePositiveInt(payload.destination());
        if (declaredDestId != null && declaredDestId > 0 && declaredDestId != currentId) {
            boolean proceed = AlertUtils.showConfirmation(
                    "Shipment Not Intended For This Warehouse",
                    "The QR indicates destination warehouse ID " + declaredDestId +
                            ", which does not match this warehouse (ID " + currentId + ").\n\n" +
                            "Do you want to force entry into this warehouse?");
            if (!proceed) return;
        }

        suppressUi = true;
        try {
            SourceComboBox.getSelectionModel().clearSelection();
            DestinationComboBox.getSelectionModel().clearSelection();
            OutsideOfNetworkTxt.clear();

            ShipmentType.selectToggle(kind == ShipmentKind.RECEPTION ? ReceptionRadioButton : ExpeditionRadioButton);

            // Partner derivation based on local kind
            if (kind == ShipmentKind.RECEPTION) {
                // I'm receiving; partner is the SOURCE given in QR
                if (currentId > 0) selectWarehouseById(DestinationComboBox, currentId);
                DestinationComboBox.setDisable(true);

                String partnerRaw = payload.source();
                Integer partnerId = payload.isInNetwork() ? parsePositiveInt(partnerRaw) : null;
                boolean outside = (partnerId == null);
                OutsideOfNetworkCheckBox.setSelected(outside);

                if (outside) {
                    OutsideOfNetworkTxt.setText(nz(partnerRaw));
                    SourceComboBox.setDisable(true);
                } else {
                    selectWarehouseById(SourceComboBox, partnerId);
                    SourceComboBox.setDisable(false);
                }
            } else {
                // I'm sending; partner is the DESTINATION given in QR
                if (currentId > 0) selectWarehouseById(SourceComboBox, currentId);
                SourceComboBox.setDisable(true);

                String partnerRaw = payload.destination();
                Integer partnerId = payload.isInNetwork() ? parsePositiveInt(partnerRaw) : null;
                boolean outside = (partnerId == null);
                OutsideOfNetworkCheckBox.setSelected(outside);

                if (outside) {
                    OutsideOfNetworkTxt.setText(nz(partnerRaw));
                    DestinationComboBox.setDisable(true);
                } else {
                    selectWarehouseById(DestinationComboBox, partnerId);
                    DestinationComboBox.setDisable(false);
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
        if (id <= 0) return;
        for (Warehouse w : combo.getItems()) {
            if (w.getId() == id) {
                combo.getSelectionModel().select(w);
                return;
            }
        }
    }

    private void onLoadQRCodeClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select QR Code Image");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) decodeQRCodeAndLoadForm(file.getAbsolutePath());
    }

    private void handleSave() {
        ShipmentKind kind = currentKind();
        boolean isReception = (kind == ShipmentKind.RECEPTION);
        boolean outside = OutsideOfNetworkCheckBox.isSelected();

        Warehouse source = SourceComboBox.getValue();
        Warehouse destination = DestinationComboBox.getValue();

        if (isReception && outside) {
            source = null; // external source
        } else if (!isReception && outside) {
            destination = null; // external destination
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
                if (isReception)
                    ShipmentServices.reception(finalSource, finalDestination, items, quantities, totalQty, totalPrice);
                else
                    ShipmentServices.expedition(finalSource, finalDestination, items, quantities, totalQty, totalPrice);
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
            AlertUtils.showError("Error", "Failed to process shipment: " +
                    (shipmentTask.getException() != null ? shipmentTask.getException().getMessage() : "Unknown error"));
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
        if (progressIndicator != null) progressIndicator.setVisible(visible);
    }

    /* ---------- Type mapping helpers ---------- */

    private ShipmentKind mapFromQr(String qrType, ShipmentQRPayload payload) {
        // 1) Prefer ID-based heuristic
        Warehouse current = Session.getCurrentWarehouse();
        int currentId = (current != null) ? current.getId() : -1;

        // destination == me  -> I'm receiving
        Integer destId = parsePositiveInt(payload.destination());
        if (destId != null && destId == currentId) return ShipmentKind.RECEPTION;

        // source == me -> I'm sending
        Integer srcId = parsePositiveInt(payload.source());
        if (srcId != null && srcId == currentId) return ShipmentKind.EXPEDITION;

        // 2) Fall back to label inversion if IDs didn't decide it
        if (qrType != null) {
            String t = qrType.trim().toLowerCase();
            if ("expedition".equals(t)) return ShipmentKind.RECEPTION;
            if ("reception".equals(t))  return ShipmentKind.EXPEDITION;
        }
        // 3) Safe default
        return ShipmentKind.RECEPTION;
    }

    private String mapToQr(ShipmentKind kind) {
        return (kind == ShipmentKind.RECEPTION) ? "Expedition" : "Reception";
    }

    /* ---------- Utils ---------- */

    private static Integer parsePositiveInt(String s) {
        if (s == null) return null;
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : null;
        } catch (Exception e) { return null; }
    }

    private static String nz(String s) { return s == null ? "" : s; }

}
