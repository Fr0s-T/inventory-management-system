package ViewsControllers;

import Models.Product;
import Models.Warehouse;
import Services.ShipmentServices;
import Utilities.AlertUtils;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class ShipmentFormHandler {

    private final ListView<String> productsListView;
    private final TextField totalQuantityTxt;
    private final TextField totalPriceTxt;

    private final ArrayList<Product> items = new ArrayList<>();
    private final ArrayList<Integer> quantities = new ArrayList<>();
    private int totalQuantity = 0;
    private float totalPrice = 0;

    public ShipmentFormHandler(ListView<String> productsListView,
                               TextField totalQuantityTxt,
                               TextField totalPriceTxt) {
        this.productsListView = productsListView;
        this.totalQuantityTxt = totalQuantityTxt;
        this.totalPriceTxt = totalPriceTxt;
    }

    public void handleReception(String code, String qtyText) {
        if (validateInput(code, qtyText)) {
            Product product = new Product();
            product.setItemCode(code);
            addProduct(product, Integer.parseInt(qtyText));
        }
    }

    public void handleExpedition(Product selectedProduct, String qtyText) {
        if (selectedProduct == null) {
            AlertUtils.showWarning("Product Selection Missing", "Please select a product for expedition.");
            return;
        }
        if (!validateInput(selectedProduct.getItemCode(), qtyText)) return;

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
            return false;
        }
        try {
            int qty = Integer.parseInt(qtyText);
            if (qty <= 0) {
                AlertUtils.showWarning("Invalid Quantity", "Quantity must be greater than 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtils.showWarning("Invalid Input", "Quantity must be a number.");
            return false;
        }
        return true;
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

        productsListView.getItems().add(product.getItemCode() + " - Qty: " + qty);
        totalQuantityTxt.setText(String.valueOf(totalQuantity));
        totalPriceTxt.setText(String.valueOf(totalPrice));
    }

    public void save(boolean isReception, Warehouse source, Warehouse destination) {

        if (items.isEmpty()) {
            AlertUtils.showWarning("No Items", "Please add at least one product before saving the shipment.");
            return;
        }

        if (isReception) {
            ShipmentServices.reception(source, destination, items, quantities, totalQuantity, totalPrice);
        } else {
            if (source.equals(destination)) {
                AlertUtils.showWarning("Invalid Warehouses", "Source and destination cannot be the same.");
                return;
            }
            ShipmentServices.expedition(source, destination, items, quantities, totalQuantity, totalPrice);
        }
        AlertUtils.showSuccess("Shipment completed successfully.");
        reset();
    }

    public void reset() {
        items.clear();
        quantities.clear();
        totalPrice = 0;
        totalQuantity = 0;
        productsListView.getItems().clear();
        totalQuantityTxt.clear();
        totalPriceTxt.clear();
    }
}
