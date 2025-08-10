package ViewsControllers.ShipmentForm.ShipmentUIHandlers;

import Models.Product;
import Models.Session;
import Models.Warehouse;
import Services.ProductsService;
import Services.WareHouseService;
import ViewsControllers.ShipmentForm.ShipmentController;
import javafx.scene.control.TextFormatter;

import java.util.ArrayList;

/**
 *
 *
 * Author: @Frost
 *
 * Handles initial setup of UI elements in ShipmentController.
 */
public class ShipmentUiInitializer {

    /**
     * Entry point called by ShipmentController.initialize()
     * Wires up all UI pieces and delegates to other handlers.
     */
    public void setup(ShipmentController controller) {
        setupWarehouses(controller);
        setupProducts(controller);
        setupRadioButtonsAndBindings(controller);
        setupCheckBox(controller);
        setupButtons(controller);
        setupQuantityFormatter(controller);

        // QR reader button action
        controller.getQrCodeReader().setOnAction(e -> controller.getQrHandler().onLoadQRCodeClick(controller));

        // Default selection and initial UI state
        controller.getShipmentType().selectToggle(controller.getReceptionRadioButton());
        controller.getSaveHandler().toggleProgress(controller, false);
    }

    /**
     * Populate Source/Destination combos and select current warehouse.
     */
    private void setupWarehouses(ShipmentController controller) {
        if (Session.getAllWarehouses() == null) {
            WareHouseService.getAllWarehouses();
        }
        ArrayList<Warehouse> warehouses = Session.getAllWarehouses();
        controller.getComboHelper().setupWarehouseComboBox(controller.getSourceComboBox(), warehouses);
        controller.getComboHelper().setupWarehouseComboBox(controller.getDestinationComboBox(), warehouses);

        Warehouse current = Session.getCurrentWarehouse();
        if (current != null) {
            controller.getComboHelper().selectById(controller.getSourceComboBox(), current.getId());
            controller.getComboHelper().selectById(controller.getDestinationComboBox(), current.getId());
        } else if (!warehouses.isEmpty()) {
            controller.getSourceComboBox().getSelectionModel().selectFirst();
            controller.getDestinationComboBox().getSelectionModel().selectFirst();
        }
    }

    /**
     * Populate product combo and set autofill behavior.
     */
    private void setupProducts(ShipmentController controller) {
        if (Session.getProducts() == null) {
            ProductsService.getProducts();
        }
        ArrayList<Product> products = Session.getProducts();

        controller.getComboHelper().setupProductComboBox(controller.getExpadistionComboBox(), products);

        controller.getExpadistionComboBox().setOnAction(e -> {
            Product selected = controller.getExpadistionComboBox().getValue();
            if (selected != null) {
                controller.getFormHandler().autoFillFromProduct(selected);
            } else {
                controller.getNameTxtField().clear();
                controller.getNameTxtField().setDisable(false);
                controller.getUnitPriceField().clear();
                controller.getUnitPriceField().setDisable(false);
            }
        });
    }

    /**
     * Bind radio buttons to ShipmentKind and react to changes.
     */
    private void setupRadioButtonsAndBindings(ShipmentController controller) {
        controller.getReceptionRadioButton().setUserData(ShipmentController.ShipmentKind.RECEPTION);
        controller.getExpeditionRadioButton().setUserData(ShipmentController.ShipmentKind.EXPEDITION);
        controller.getShipmentType().selectedToggleProperty()
                .addListener((obs, oldT, newT) -> controller.getUiStateHandler().applyUiState(controller));
        controller.getUiStateHandler().applyUiState(controller);
    }

    /**
     * Outside-of-network checkbox toggling.
     */
    private void setupCheckBox(ShipmentController controller) {
        controller.getOutsideOfNetworkCheckBox().selectedProperty().addListener((obs, oldVal, isChecked) -> {
            if (!isChecked) {
                controller.getOutsideOfNetworkTxt().clear();
            }
            controller.getUiStateHandler().applyUiState(controller);
        });
        controller.getUiStateHandler().applyUiState(controller);
    }

    /**
     * Wire up all main buttons.
     */
    private void setupButtons(ShipmentController controller) {
        controller.getAddBtn().setOnAction(event -> {
            if (controller.getUiStateHandler().currentKind(controller) == ShipmentController.ShipmentKind.RECEPTION) {
                controller.getFormHandler().handleReception(controller.getItemCodeTxt().getText(),
                        controller.getQuantityTxt().getText());
            } else {
                controller.getFormHandler().handleExpedition(controller.getExpadistionComboBox().getValue(),
                        controller.getQuantityTxt().getText());
            }
        });

        controller.getSaveButton().setOnAction(event -> controller.getSaveHandler().handleSave(controller));
        controller.getCancelButton().setOnAction(event -> controller.getFormHandler().reset());
        controller.getEditBtn().setOnAction(event -> controller.getSaveHandler().handleEdit(controller));
        controller.getRemoveBtn().setOnAction(event -> controller.getFormHandler().removeItem());

        controller.getUiStateHandler().updateQRCodeButton(controller);
    }

    /**
     * Restrict quantity text field to digits only.
     */
    private void setupQuantityFormatter(ShipmentController controller) {
        TextFormatter<String> tf = new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,9}") ? change : null);
        controller.getQuantityTxt().setTextFormatter(tf);
    }
}
// full implementation moved from setup methods