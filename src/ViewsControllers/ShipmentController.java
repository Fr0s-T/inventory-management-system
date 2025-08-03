package ViewsControllers;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Services.ProductsService;
import Services.ShipmentServices;
import Services.WareHouseService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;

public class ShipmentController {

    @FXML private RadioButton ReceptionRadioButton;
    @FXML private RadioButton ExpeditionRadioButton;
    @FXML private ToggleGroup ShipmentType;
    @FXML private ComboBox<Warehouse> SourceComboBox;
    @FXML private ComboBox<Warehouse> DestinationComboBox;
    @FXML private TextField ItemCodeTxt;
    @FXML private TextField QuantityTxt;
    @FXML private TextField TotalQuantityTxt;
    @FXML private ListView<String> ProductsListView;
    @FXML private Button SaveButton;
    @FXML private Button CancelButton;
    @FXML private Button AddBtn;
    @FXML private ComboBox<Product> ExpadistionComboBox;

    private ArrayList<Product> items = new ArrayList<>();
    private ArrayList<Integer> quantity = new ArrayList<>();
    private int totalQuantity = 0;
    private float totalPrice = 0; // ✅ Added for price calculation

    @FXML public void initialize() {
        setDefaultSelection();
        setupUIVisibility();
        setupWarehouses();
        setupProducts();
        setupRadioButtonBehavior();
        setupButtons();
    }

    // -------------------- Initialization Methods --------------------

    private void setDefaultSelection() {
        ShipmentType.selectToggle(ReceptionRadioButton); // Reception by default
    }

    private void setupUIVisibility() {
        ExpadistionComboBox.setVisible(false); // Hidden initially
    }

    private void setupWarehouses() {
        ArrayList<Warehouse> warehouses = getWarehouses();
        SourceComboBox.getItems().addAll(warehouses);
        DestinationComboBox.getItems().addAll(warehouses);

        setupWarehouseComboBox(SourceComboBox);
        setupWarehouseComboBox(DestinationComboBox);

        if (!warehouses.isEmpty()) {
            SourceComboBox.getSelectionModel().selectFirst();
            DestinationComboBox.getSelectionModel().selectFirst();
        }
    }

    private ArrayList<Warehouse> getWarehouses() {
        if (Session.getAllWarehouses() == null) {
            WareHouseService.getAllWarehouses();
        }
        return Session.getAllWarehouses();
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

        ExpadistionComboBox.setOnAction(e -> {
            Product selected = ExpadistionComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                System.out.println("Selected product: " + selected.getItemCode());
            }
        });
    }

    private void setupRadioButtonBehavior() {
        ShipmentType.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == ExpeditionRadioButton) {
                enableExpeditionMode();
            } else if (newToggle == ReceptionRadioButton) {
                enableReceptionMode();
            }
        });
    }

    private void setupButtons() {
        AddBtn.setOnAction(actionEvent -> {
            if (ReceptionRadioButton.isSelected()) {
                handleReceptionAdd();
            } else if (ExpeditionRadioButton.isSelected()) {
                handleExpeditionAdd();
            }
        });
        SaveButton.setOnAction(actionEvent -> {
            if (ReceptionRadioButton.isSelected()){
                ShipmentServices.reception();
            }
            else if (ExpeditionRadioButton.isSelected()){
                ShipmentServices.expedition(
                        getSelectedSourceWarehouse(),
                        getSelectedDestinationWarehouse(),
                        items,
                        quantity,
                        totalQuantity,
                        totalPrice // ✅ Send calculated total price
                );
            }
        });
    }

    // -------------------- Mode Switching Methods --------------------

    private void enableExpeditionMode() {
        SourceComboBox.getSelectionModel().select(Session.getCurrentWarehouse());
        SourceComboBox.setDisable(true);
        DestinationComboBox.setDisable(false);

        ItemCodeTxt.setVisible(false);
        ExpadistionComboBox.setVisible(true);
    }

    private void enableReceptionMode() {
        DestinationComboBox.getSelectionModel().select(Session.getCurrentWarehouse());
        DestinationComboBox.setDisable(true);
        SourceComboBox.setDisable(false);

        ItemCodeTxt.setVisible(true);
        ExpadistionComboBox.setVisible(false);
    }

    // -------------------- Utility Methods --------------------

    private void handleReceptionAdd() {
        String code = ItemCodeTxt.getText().trim();
        String qtyText = QuantityTxt.getText().trim();

        if (code.isEmpty()) {
            showAlert("Item Code Missing", "Please enter an item code.");
            return;
        }
        if (qtyText.isEmpty()) {
            showAlert("Quantity Missing", "Please enter a quantity.");
            return;
        }

        int qty = Integer.parseInt(qtyText);

        Product product = new Product();
        product.setItemCode(code);
        product.setUnitPrice(0); // No price info for manual reception

        addProductToList(product, qty);
    }

    private void handleExpeditionAdd() {
        Product selectedProduct = ExpadistionComboBox.getSelectionModel().getSelectedItem();
        String qtyText = QuantityTxt.getText().trim();

        if (selectedProduct == null) {
            showAlert("Product Selection Missing", "Please select a product for expedition.");
            return;
        }
        if (qtyText.isEmpty()) {
            showAlert("Quantity Missing", "Please enter a quantity.");
            return;
        }

        int qty = Integer.parseInt(qtyText);
        if (qty > selectedProduct.getQuantity()){
            showAlert("Too much","Max quantity available is: "+ selectedProduct.getQuantity());
            return;
        }

        addProductToList(selectedProduct, qty);
    }

    private void addProductToList(Product product, int qty) {
        if (items.stream().anyMatch(p -> p.getItemCode().equals(product.getItemCode()))) {
            showAlert("Duplicate Item", "This product is already added.");
            return;
        }

        items.add(product);
        quantity.add(qty);
        totalQuantity += qty;

        // ✅ Calculate total price
        totalPrice += product.getUnitPrice() * qty;

        ProductsListView.getItems().add(product.getItemCode() + " - Qty: " + qty);
        TotalQuantityTxt.setText(String.valueOf(totalQuantity));
    }

    private Warehouse getSelectedSourceWarehouse() {
        return SourceComboBox.getSelectionModel().getSelectedItem();
    }

    private Warehouse getSelectedDestinationWarehouse() {
        return DestinationComboBox.getSelectionModel().getSelectedItem();
    }

    private void setupWarehouseComboBox(ComboBox<Warehouse> comboBox) {
        comboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse warehouse, boolean empty) {
                super.updateItem(warehouse, empty);
                setText(empty || warehouse == null ? "" : warehouse.getName());
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse warehouse, boolean empty) {
                super.updateItem(warehouse, empty);
                setText(empty || warehouse == null ? "" : warehouse.getName());
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
