package ViewsControllers.ShipmentForm;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Services.ProductsService;
import Services.ShipmentServices;
import Services.WareHouseService;
import Utilities.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Author: @Frost
 *
 */
public class ShipmentController {

    @FXML private TextField NameTxtField;
    @FXML private RadioButton ReceptionRadioButton;
    @FXML private RadioButton ExpeditionRadioButton;
    @FXML private ToggleGroup ShipmentType;
    @FXML private ComboBox<Warehouse> SourceComboBox;
    @FXML private ComboBox<Warehouse> DestinationComboBox;
    @FXML private TextField ItemCodeTxt;
    @FXML private TextField QuantityTxt;
    @FXML private TextField TotalQuantityTxt;
    @FXML private TextField TotalPriceTxtField;
    @FXML private TextField UnitPriceField;
    @FXML private ListView<String> ProductsListView;
    @FXML private Button SaveButton;
    @FXML private Button CancelButton;
    @FXML private Button AddBtn;
    @FXML private Button EditBtn;
    @FXML private Button RemoveBtn;
    @FXML private ComboBox<Product> ExpadistionComboBox;
    @FXML private ProgressIndicator progressIndicator;

    private ShipmentFormHandler formHandler;

    @FXML
    public void initialize() {
        formHandler = new ShipmentFormHandler(
                ProductsListView,
                TotalQuantityTxt,
                TotalPriceTxtField,
                UnitPriceField,
                ItemCodeTxt,
                NameTxtField,
                QuantityTxt
        );

        setupWarehouses();
        setupProducts();
        setupRadioButtons();
        setupButtons();

        ShipmentType.selectToggle(ReceptionRadioButton);
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
    }

    private void setupWarehouses() {
        if (Session.getAllWarehouses() == null) {
            WareHouseService.getAllWarehouses();
        }

        ArrayList<Warehouse> warehouses = Session.getAllWarehouses();
        SourceComboBox.getItems().addAll(warehouses);
        DestinationComboBox.getItems().addAll(warehouses);

        SourceComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse warehouse, boolean empty) {
                super.updateItem(warehouse, empty);
                setText(empty || warehouse == null ? "" : warehouse.getName());
            }
        });
        SourceComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse warehouse, boolean empty) {
                super.updateItem(warehouse, empty);
                setText(empty || warehouse == null ? "" : warehouse.getName());
            }
        });

        DestinationComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse warehouse, boolean empty) {
                super.updateItem(warehouse, empty);
                setText(empty || warehouse == null ? "" : warehouse.getName());
            }
        });
        DestinationComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse warehouse, boolean empty) {
                super.updateItem(warehouse, empty);
                setText(empty || warehouse == null ? "" : warehouse.getName());
            }
        });

        if (!warehouses.isEmpty()) {
            SourceComboBox.getSelectionModel().selectFirst();
            DestinationComboBox.getSelectionModel().selectFirst();
        }
    }

    private void setupProducts() {
        if (Session.getProducts() == null) ProductsService.getProducts();

        ArrayList<Product> products = Session.getProducts();
        ExpadistionComboBox.getItems().addAll(products);

        ExpadistionComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                setText(empty || product == null ? "" : product.getItemCode());
            }
        });
        ExpadistionComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                setText(empty || product == null ? "" : product.getItemCode());
            }
        });

        ExpadistionComboBox.setOnAction(event -> {
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

    private void setupRadioButtons() {
        ShipmentType.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == ExpeditionRadioButton) {
                for (Warehouse warehouse : SourceComboBox.getItems()) {
                    if (warehouse.getId() == Session.getCurrentWarehouse().getId()) {
                        SourceComboBox.getSelectionModel().select(warehouse);
                        break;
                    }
                }
                SourceComboBox.setDisable(true);
                DestinationComboBox.setDisable(false);
                ItemCodeTxt.setVisible(false);
                ExpadistionComboBox.setVisible(true);
            } else {
                for (Warehouse warehouse : DestinationComboBox.getItems()) {
                    if (warehouse.getId() == Session.getCurrentWarehouse().getId()) {
                        DestinationComboBox.getSelectionModel().select(warehouse);
                        break;
                    }
                }
                DestinationComboBox.setDisable(true);
                SourceComboBox.setDisable(false);
                ItemCodeTxt.setVisible(true);
                ExpadistionComboBox.setVisible(false);
            }
        });
    }

    private void setupButtons() {
        AddBtn.setOnAction(event -> {
            if (ReceptionRadioButton.isSelected()) {
                formHandler.handleReception(ItemCodeTxt.getText(), QuantityTxt.getText());
            } else {
                formHandler.handleExpedition(ExpadistionComboBox.getValue(), QuantityTxt.getText());
            }
        });

        SaveButton.setOnAction(event -> handleSave());

        CancelButton.setOnAction(event -> formHandler.reset());

        EditBtn.setOnAction(event -> handleEdit());

        RemoveBtn.setOnAction(actionEvent -> formHandler.removeItem());
    }

    private void handleSave() {
        boolean isReception = ReceptionRadioButton.isSelected();
        Warehouse source = SourceComboBox.getValue();
        Warehouse destination = DestinationComboBox.getValue();
        ArrayList<Product> items = formHandler.getItems();
        ArrayList<Integer> quantities = formHandler.getQuantities();
        int totalQty = formHandler.getTotalQuantity();
        float totalPrice = formHandler.getTotalPrice();

        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }

        Task<Void> shipmentTask = new Task<>() {
            @Override
            protected Void call() {
                if (isReception) {
                    ShipmentServices.reception(source, destination, items, quantities, totalQty, totalPrice);
                } else {
                    ShipmentServices.expedition(source, destination, items, quantities, totalQty, totalPrice);
                }
                return null;
            }
        };

        shipmentTask.setOnRunning(e -> {
            if (progressIndicator != null) progressIndicator.setVisible(true);
            SaveButton.setDisable(true);
        });

        shipmentTask.setOnSucceeded(e -> {
            if (progressIndicator != null) progressIndicator.setVisible(false);
            formHandler.reset();
            SaveButton.setDisable(false);
            AlertUtils.showSuccess("Shipment completed successfully.");
        });

        shipmentTask.setOnFailed(e -> {
            if (progressIndicator != null) progressIndicator.setVisible(false);
            String message = shipmentTask.getException() != null ? shipmentTask.getException().getMessage() : "Unknown error";
            SaveButton.setDisable(false);
            AlertUtils.showError("Error", "Failed to process shipment: " + message);
        });

        new Thread(shipmentTask).start();
    }

    private void handleEdit() {
        String selectedEntry = ProductsListView.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            AlertUtils.showWarning("No Selection", "Please select a product to edit.");
            return;
        }

        String[] parts = selectedEntry.split(" - Qty: ");
        if (parts.length != 2) {
            AlertUtils.showError("Invalid Format", "The selected entry is not valid.");
            return;
        }

        String itemCode = parts[0].trim();
        String quantity = parts[1].trim();

        if (ReceptionRadioButton.isSelected()) {
            ItemCodeTxt.setText(itemCode);
            ItemCodeTxt.setDisable(true);
            QuantityTxt.setText(quantity);
        } else {
            for (Product product : ExpadistionComboBox.getItems()) {
                if (product.getItemCode().equalsIgnoreCase(itemCode)) {
                    ExpadistionComboBox.getSelectionModel().select(product);
                    formHandler.autoFillFromProduct(product);
                    break;
                }
            }
            QuantityTxt.setText(quantity);
        }

        ProductsListView.getItems().remove(selectedEntry);
        formHandler.removeItem(itemCode);
    }
}
