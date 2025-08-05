package ViewsControllers.ShipmentForm;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Services.ProductsService;
import Services.WareHouseService;
import Utilities.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;

/**
 *
 * Author: @Frost
 *
 */

public class ShipmentController {

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

    private ShipmentFormHandler formHandler;

    @FXML
    public void initialize() {
        formHandler = new ShipmentFormHandler(
                ProductsListView,
                TotalQuantityTxt,
                TotalPriceTxtField,
                UnitPriceField,
                ItemCodeTxt
        );


        setupWarehouses();
        setupProducts();
        setupRadioButtons();
        setupButtons();

        ShipmentType.selectToggle(ReceptionRadioButton); // Default
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
        ProductsService.getProducts();
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

        // ✅ Auto-fill unit price on selection
        ExpadistionComboBox.setOnAction(event -> {
            Product selected = ExpadistionComboBox.getValue();
            if (selected != null && selected.getUnitPrice() > 0) {
                UnitPriceField.setText(String.valueOf(selected.getUnitPrice()));
                UnitPriceField.setDisable(true);
            } else {
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

        SaveButton.setOnAction(event -> formHandler.save(
                ReceptionRadioButton.isSelected(),
                SourceComboBox.getValue(),
                DestinationComboBox.getValue()
        ));

        CancelButton.setOnAction(event -> formHandler.reset());

        EditBtn.setOnAction(event -> {
            if (ReceptionRadioButton.isSelected()) {
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

                ItemCodeTxt.setText(itemCode);
                ItemCodeTxt.setDisable(true);
                QuantityTxt.setText(quantity);

                ProductsListView.getItems().remove(selectedEntry);
                formHandler.removeItem(itemCode);
            } else if (ExpeditionRadioButton.isSelected()) {
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

                // Find and set the product in ComboBox
                for (Product product : ExpadistionComboBox.getItems()) {
                    if (product.getItemCode().equalsIgnoreCase(itemCode)) {
                        ExpadistionComboBox.getSelectionModel().select(product);

                        // Auto-fill unit price for expedition edit
                        if (product.getUnitPrice() > 0) {
                            UnitPriceField.setText(String.valueOf(product.getUnitPrice()));
                            UnitPriceField.setDisable(true);
                        } else {
                            UnitPriceField.clear();
                            UnitPriceField.setDisable(false);
                        }
                        break;
                    }
                }

                QuantityTxt.setText(quantity);

                ProductsListView.getItems().remove(selectedEntry);
                formHandler.removeItem(itemCode);
            }
        });
        RemoveBtn.setOnAction(actionEvent -> {
            formHandler.removeItem();
        });

    }

}
