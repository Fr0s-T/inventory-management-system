package ViewsControllers.ShipmentForm.ShipmentUIHandlers;

import Models.Product;
import Models.Warehouse;
import Services.ShipmentServices;
import Utilities.AlertUtils;
import ViewsControllers.ShipmentForm.ShipmentController;
import javafx.concurrent.Task;

import java.util.ArrayList;

/**
 *
 *
 * Author: @Frost
 *
 * Handles saving and editing shipments in ShipmentController.
 */
public class ShipmentSaveHandler {

    /**
     * Validate inputs, then run reception/expedition in a background Task.
     */
    public void handleSave(ShipmentController controller) {
        ShipmentController.ShipmentKind kind = controller.getUiStateHandler().currentKind(controller);
        boolean isReception = (kind == ShipmentController.ShipmentKind.RECEPTION);
        boolean outside = controller.getOutsideOfNetworkCheckBox().isSelected();

        Warehouse source = controller.getSourceComboBox().getValue();
        Warehouse destination = controller.getDestinationComboBox().getValue();

        if (isReception && outside) {
            source = null;
        } else if (!isReception && outside) {
            destination = null;
        }

        ArrayList<Product> items = controller.getFormHandler().getItems();
        ArrayList<Integer> quantities = controller.getFormHandler().getQuantities();
        int totalQty = controller.getFormHandler().getTotalQuantity();
        float totalPrice = controller.getFormHandler().getTotalPrice();

        if (items.isEmpty()) {
            AlertUtils.showWarning("No Items", "Please add at least one product before saving.");
            return;
        }
        if (!isReception && source != null && destination != null && source.equals(destination)) {
            AlertUtils.showWarning("Invalid Warehouses", "Source and destination cannot be the same.");
            return;
        }

        toggleProgress(controller, true);

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

        shipmentTask.setOnRunning(e -> controller.getSaveButton().setDisable(true));
        shipmentTask.setOnSucceeded(e -> {
            toggleProgress(controller, false);
            controller.getFormHandler().reset();
            controller.getSaveButton().setDisable(false);
            AlertUtils.showSuccess("Shipment completed successfully.");
        });
        shipmentTask.setOnFailed(e -> {
            toggleProgress(controller, false);
            controller.getSaveButton().setDisable(false);
            Throwable ex = shipmentTask.getException();
            String msg = (ex != null) ? ex.getMessage() : "Unknown error";
            AlertUtils.showError("Error", "Failed to process shipment: " + msg);
        });

        new Thread(shipmentTask).start();
    }

    /**
     * Populate edit fields from selected list entry, then remove original entry to re-add after editing.
     */
    public void handleEdit(ShipmentController controller) {
        String selectedEntry = controller.getProductsListView().getSelectionModel().getSelectedItem();
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

        if (controller.getUiStateHandler().currentKind(controller) == ShipmentController.ShipmentKind.RECEPTION) {
            controller.getItemCodeTxt().setText(itemCode);
            controller.getItemCodeTxt().setDisable(true);
            controller.getQuantityTxt().setText(quantity);
        } else {
            for (Product p : controller.getExpadistionComboBox().getItems()) {
                if (p.getItemCode().equalsIgnoreCase(itemCode)) {
                    controller.getExpadistionComboBox().getSelectionModel().select(p);
                    controller.getFormHandler().autoFillFromProduct(p);
                    break;
                }
            }
            controller.getQuantityTxt().setText(quantity);
        }

        controller.getProductsListView().getItems().remove(selectedEntry);
        controller.getFormHandler().removeItem(itemCode);
    }

    /**
     * Show/hide the progress indicator during background operations.
     */
    public void toggleProgress(ShipmentController controller, boolean visible) {
        if (controller.getProgressIndicator() != null) {
            controller.getProgressIndicator().setVisible(visible);
        }
    }
}
