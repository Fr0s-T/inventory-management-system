package ViewsControllers.ShipmentForm;

import Models.ItemEntry;
import Models.Product;
import Models.Warehouse;
import Models.Session;
import Services.ShipmentServices;
import Utilities.AlertUtils;
import Utilities.QRCodeUtils;
import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import java.util.ArrayList;

/**
 * ShipmentFormHandler
 *
 * Responsibilities:
 *  - Manages the in-form list of items, quantities and totals.
 *  - Autofills product details from session data.
 *  - Performs local validation and duplicate prevention.
 *  - Generates QR payloads with the current cart.
 *
 * Notes:
 *  - Totals are kept as float to keep compatibility with existing services,
 *    but display is formatted to two decimals to reduce visual artifacts.
 */
public class ShipmentFormHandler {

    private final ListView<String> productsListView;
    private final TextField totalQuantityTxt;
    private final TextField totalPriceTxt;
    private final TextField unitPriceField;
    private final TextField itemCodeField;
    private final TextField nameField;
    private final TextField QuantityTxt;

    private final ArrayList<Product> items = new ArrayList<>();
    private final ArrayList<Integer> quantities = new ArrayList<>();
    private int totalQuantity = 0;
    private float totalPrice = 0f;

    private ProgressIndicator progressIndicator;

    public ShipmentFormHandler(ListView<String> productsListView,
                               TextField totalQuantityTxt,
                               TextField totalPriceTxt,
                               TextField unitPriceField,
                               TextField itemCodeField,
                               TextField nameField,
                               TextField QuantityTxt) {
        this.productsListView = productsListView;
        this.totalQuantityTxt = totalQuantityTxt;
        this.totalPriceTxt = totalPriceTxt;
        this.unitPriceField = unitPriceField;
        this.itemCodeField = itemCodeField;
        this.nameField = nameField;
        this.QuantityTxt = QuantityTxt;
        setupAutoFillDetails();
    }

    public void setProgressIndicator(ProgressIndicator indicator) {
        this.progressIndicator = indicator;
        if (progressIndicator != null) progressIndicator.setVisible(false);
    }

    /* ---------- Autofill ---------- */

    private void setupAutoFillProductDetails(String code) {
        if (code == null || code.isEmpty()) return;

        Product existingProduct = null;

        if (Session.getProducts() != null) {
            existingProduct = Session.getProducts().stream()
                    .filter(p -> p.getItemCode().equalsIgnoreCase(code))
                    .findFirst()
                    .orElse(null);
        }

        if (existingProduct == null && Session.getGlobalProductCatalog() != null) {
            existingProduct = Session.getGlobalProductCatalog().get(code);
        }

        if (existingProduct != null) {
            if (existingProduct.getUnitPrice() > 0) {
                unitPriceField.setText(String.valueOf(existingProduct.getUnitPrice()));
                unitPriceField.setDisable(true);
            } else {
                unitPriceField.clear();
                unitPriceField.setDisable(false);
            }
            nameField.setText(existingProduct.getName());
            nameField.setDisable(true);
        } else {
            unitPriceField.clear();
            unitPriceField.setDisable(false);
            nameField.clear();
            nameField.setDisable(false);
        }
    }

    private void setupAutoFillDetails() {
        itemCodeField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                setupAutoFillProductDetails(itemCodeField.getText());
            }
        });
    }

    public void autoFillFromProduct(Product product) {
        if (product == null) return;

        if (product.getUnitPrice() > 0) {
            unitPriceField.setText(String.valueOf(product.getUnitPrice()));
            unitPriceField.setDisable(true);
        } else {
            unitPriceField.clear();
            unitPriceField.setDisable(false);
        }
        nameField.setText(product.getName());
        nameField.setDisable(true);
    }

    /* ---------- Add items ---------- */

    public void handleReception(String code, String qtyText) {
        if (validateInput(code, qtyText)) return;

        Product existingProduct = null;
        if (Session.getProducts() != null) {
            existingProduct = Session.getProducts().stream()
                    .filter(p -> p.getItemCode().equalsIgnoreCase(code))
                    .findFirst()
                    .orElse(null);
        }

        if ((existingProduct == null || existingProduct.getUnitPrice() <= 0) &&
                unitPriceField.getText().trim().isEmpty()) {
            AlertUtils.showWarning("Missing Price", "You must enter a unit price for new products.");
            return;
        }

        if ((existingProduct == null || existingProduct.getName() == null || existingProduct.getName().isEmpty()) &&
                nameField.getText().trim().isEmpty()) {
            AlertUtils.showWarning("Missing Name", "You must enter a name for new products.");
            return;
        }

        Product product = getProduct(code, existingProduct);
        addProduct(product, Integer.parseInt(qtyText));
    }

    private Product getProduct(String code, Product existingProduct) {
        float unitPrice = !unitPriceField.getText().trim().isEmpty()
                ? Float.parseFloat(unitPriceField.getText().trim())
                : (existingProduct != null ? existingProduct.getUnitPrice() : 0);

        String productName = !nameField.getText().trim().isEmpty()
                ? nameField.getText().trim()
                : (existingProduct != null ? existingProduct.getName() : "");

        Product product = new Product(code, "0", unitPrice);
        product.setItemCode(code);
        product.setName(productName);
        return product;
    }

    public void handleExpedition(Product selectedProduct, String qtyText) {
        if (selectedProduct == null) {
            AlertUtils.showWarning("Product Selection Missing", "Please select a product for expedition.");
            return;
        }
        if (validateInput(selectedProduct.getItemCode(), qtyText)) return;

        autoFillFromProduct(selectedProduct);

        int qty = Integer.parseInt(qtyText);
        if (qty > selectedProduct.getQuantity()) {
            AlertUtils.showWarning("Too much", "Max quantity available is: " + selectedProduct.getQuantity());
            return;
        }
        addProduct(selectedProduct, qty);
    }

    private boolean validateInput(String code, String qtyText) {
        if (code == null || code.isEmpty()) {
            AlertUtils.showWarning("Item Code Missing", "Please enter an item code.");
            return true;
        }
        try {
            int qty = Integer.parseInt(qtyText);
            if (qty <= 0) {
                AlertUtils.showWarning("Invalid Quantity", "Quantity must be greater than 0.");
                return true;
            }
        } catch (NumberFormatException e) {
            AlertUtils.showWarning("Invalid Input", "Quantity must be a number.");
            return true;
        }
        return false;
    }

    private void addProduct(Product product, int qty) {
        if (items.stream().anyMatch(p -> p.getItemCode().equalsIgnoreCase(product.getItemCode()))) {
            AlertUtils.showWarning("Duplicate Item", "This product is already added.");
            return;
        }
        items.add(product);
        quantities.add(qty);
        totalQuantity += qty;
        totalPrice += product.getUnitPrice() * qty;

        String display = String.format(
                "%s | %s | Qty: %d | Price: %.2f",
                product.getItemCode(),
                product.getName(),
                qty,
                product.getUnitPrice()
        );

        productsListView.getItems().add(display);
        totalQuantityTxt.setText(String.valueOf(totalQuantity));
        totalPriceTxt.setText(String.format("%.2f", totalPrice));
    }

    /* ---------- Save and reset ---------- */

    public void save(boolean isReception, Warehouse source, Warehouse destination) {
        if (items.isEmpty()) {
            AlertUtils.showWarning("No Items", "Please add at least one product before saving the shipment.");
            return;
        }

        if (!isReception && source != null && destination != null && source.equals(destination)) {
            AlertUtils.showWarning("Invalid Warehouses", "Source and destination cannot be the same.");
            return;
        }

        if (progressIndicator != null) progressIndicator.setVisible(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (isReception) {
                    ShipmentServices.reception(source, destination, items, quantities, totalQuantity, totalPrice);
                } else {
                    ShipmentServices.expedition(source, destination, items, quantities, totalQuantity, totalPrice);
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            if (progressIndicator != null) progressIndicator.setVisible(false);
            AlertUtils.showSuccess("Shipment completed successfully.");
            reset();
        });

        task.setOnFailed(event -> {
            if (progressIndicator != null) progressIndicator.setVisible(false);
            String msg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
            AlertUtils.showError("Error", "Failed to process shipment: " + msg);
        });

        new Thread(task).start();
    }

    public void reset() {
        items.clear();
        quantities.clear();
        totalPrice = 0;
        totalQuantity = 0;
        productsListView.getItems().clear();
        totalQuantityTxt.clear();
        totalPriceTxt.clear();
        unitPriceField.clear();
        QuantityTxt.clear();
        unitPriceField.setDisable(false);
        itemCodeField.clear();
        nameField.clear();
        nameField.setDisable(false);
    }

    /* ---------- Removal ---------- */

    public void removeItem(String itemCode) {
        for (int i = 0; i < items.size(); i++) {
            Product product = items.get(i);
            if (product.getItemCode().equalsIgnoreCase(itemCode)) {
                int removedQty = quantities.get(i);
                float removedPrice = product.getUnitPrice() * removedQty;

                totalQuantity -= removedQty;
                totalPrice -= removedPrice;

                items.remove(i);
                quantities.remove(i);
                break;
            }
        }

        totalQuantity = Math.max(0, totalQuantity);
        totalPrice = Math.max(0, totalPrice);

        totalQuantityTxt.setText(String.valueOf(totalQuantity));
        totalPriceTxt.setText(String.format("%.2f", totalPrice));
    }

    public void removeItem() {
        String selectedEntry = productsListView.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            AlertUtils.showWarning("No Selection", "Please select an item to remove.");
            return;
        }

        String[] parts = selectedEntry.split(" \\| ");
        if (parts.length < 1) {
            AlertUtils.showWarning("Invalid Format", "Cannot identify the selected product.");
            return;
        }

        String itemCode = parts[0].trim();
        removeItem(itemCode);
        productsListView.getItems().remove(selectedEntry);
    }

    /* ---------- QR generation ---------- */

    /**
     * Generates a QR for the current in-form items and parameters.
     *
     * @param source         source warehouse (can be null when outside network)
     * @param destination    destination warehouse (can be null when outside network)
     * @param isInNetwork    true if both ends are inside the network
     * @param outsideName    non-null when outside network and the external party needs a name
     * @param shipmentType   QR-facing type label (e.g., "Expedition" or "Reception")
     * @param filePath       target PNG path selected by user
     */
    public void generateQRCode(Warehouse source,
                               Warehouse destination,
                               boolean isInNetwork,
                               String outsideName,
                               String shipmentType,
                               String filePath) {
        try {
            QRCodeUtils.generateShipmentQRCode(
                    items, quantities, source, destination,
                    isInNetwork, outsideName, filePath, shipmentType
            );

            AlertUtils.showSuccess("The QR code has been successfully generated and saved.");
        } catch (Exception e) {
            AlertUtils.showError("QR Generation Failed", e.getMessage());
        }
    }

    /* ---------- Mapping helpers used by controller ---------- */

    public Product fromItemEntry(ItemEntry entry) {
        return new Product(
                entry.itemCode(),
                String.valueOf(entry.quantity()),
                entry.unitPrice()
        );
    }

    public void setItems(ArrayList<Product> newItems, ArrayList<Integer> newQuantities) {
        if (newItems == null || newQuantities == null || newItems.size() != newQuantities.size()) {
            AlertUtils.showError("Invalid Data", "Mismatch between products and quantities.");
            return;
        }

        reset(); // Clear existing form

        for (int i = 0; i < newItems.size(); i++) {
            Product product = newItems.get(i);
            int quantity = newQuantities.get(i);
            addProduct(product, quantity);
        }
    }

    /* ---------- Accessors ---------- */

    public ArrayList<Product> getItems() { return items; }
    public ArrayList<Integer> getQuantities() { return quantities; }
    public int getTotalQuantity() { return totalQuantity; }
    public float getTotalPrice() { return totalPrice; }
}
