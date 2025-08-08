package ViewsControllers.ShipmentForm;

import Models.Product;
import Models.Warehouse;
import Models.Session;
import Services.ShipmentServices;
import Utilities.AlertUtils;
import Utilities.QRCodeUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import java.util.ArrayList;

/**
 *
 * Author: @Frost
 *
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
    private float totalPrice = 0;

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
        this.QuantityTxt=QuantityTxt;
        setupAutoFillDetails();
    }

    public void setProgressIndicator(ProgressIndicator indicator) {
        this.progressIndicator = indicator;
        if (progressIndicator != null) progressIndicator.setVisible(false);
    }

    private void setupAutoFillDetails() {
        itemCodeField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                autoFillProductDetails(itemCodeField.getText());
            }
        });
    }

    private void autoFillProductDetails(String code) {
        if (code == null || code.isEmpty()) return;

        Product existingProduct = Session.getProducts().stream()
                .filter(p -> p.getItemCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);

        if (existingProduct == null && Session.getGlobalProductCatalog() != null) {
            // Try to get from global catalog
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

    public void handleReception(String code, String qtyText) {
        if (validateInput(code, qtyText)) return;

        Product existingProduct = Session.getProducts().stream()
                .filter(p -> p.getItemCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);

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

        Product product = getProduct(code, qtyText, existingProduct);
        addProduct(product, Integer.parseInt(qtyText));
    }

    private Product getProduct(String code, String qtyText, Product existingProduct) {
        float unitPrice = !unitPriceField.getText().trim().isEmpty()
                ? Float.parseFloat(unitPriceField.getText().trim())
                : (existingProduct != null ? existingProduct.getUnitPrice() : 0);

        String productName = !nameField.getText().trim().isEmpty()
                ? nameField.getText().trim()
                : (existingProduct != null ? existingProduct.getName() : "");

        Product product = new Product(code, qtyText, unitPrice);
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
        if (items.stream().anyMatch(p -> p.getItemCode().equals(product.getItemCode()))) {
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
        totalPriceTxt.setText(String.valueOf(totalPrice));
    }


    public void save(boolean isReception, Warehouse source, Warehouse destination) {
        if (items.isEmpty()) {
            AlertUtils.showWarning("No Items", "Please add at least one product before saving the shipment.");
            return;
        }

        if (!isReception && source.equals(destination)) {
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

        // Prevent negative values (in case of bugs)
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

    public void generateQRCode(Warehouse source, boolean isInNetwork, String outsideName) {
        try {
            String filePath = "generated_qr/shipment.png";
            QRCodeUtils.generateShipmentQRCode(items, quantities, source, isInNetwork, outsideName, filePath);

            //  Confirmation alert
            AlertUtils.showSuccess("The QR code has been successfully generated and saved.");
        } catch (Exception e) {
            AlertUtils.showError("QR Generation Failed", e.getMessage());
        }
    }




    // 👉 These are helpful if ShipmentController wants to use the values directly
    public ArrayList<Product> getItems() {
        return items;
    }

    public ArrayList<Integer> getQuantities() {
        return quantities;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public float getTotalPrice() {
        return totalPrice;
    }
}
