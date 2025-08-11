package ViewsControllers.ShipmentForm.ShipmentUIHandlers;

import Models.Session;
import Models.Warehouse;
import ViewsControllers.ShipmentForm.ShipmentController;
import javafx.scene.control.Control;
import javafx.scene.control.Toggle;

/**
 *
 *
 * Author: @Frost
 *
 * Handles UI state changes between Reception and Expedition.
 */
public class ShipmentUiStateHandler {

    // Prevents UI listeners from reacting while we programmatically change controls
    private boolean suppressUi = false;

    public void setSuppressUi(boolean suppressUi) {
        this.suppressUi = suppressUi;
    }

    /**
     * Current shipment kind based on selected toggle.
     */
    public ShipmentController.ShipmentKind currentKind(ShipmentController controller) {
        Toggle t = controller.getShipmentType().getSelectedToggle();
        if (t == null) return ShipmentController.ShipmentKind.RECEPTION;
        return (ShipmentController.ShipmentKind) t.getUserData();
    }

    /**
     * Apply UI enabling/disabling based on mode and outside-of-network checkbox.
     */
    public void applyUiState(ShipmentController controller) {
        if (suppressUi) return;

        ShipmentController.ShipmentKind kind = currentKind(controller);
        boolean outside = controller.getOutsideOfNetworkCheckBox().isSelected();
        Warehouse current = Session.getCurrentWarehouse();

        controller.getUiState().apply(
                kind,
                outside,
                current,
                controller.getSourceComboBox(),
                controller.getDestinationComboBox(),
                controller.getItemCodeTxt(),
                controller.getExpadistionComboBox(),
                controller.getOutsideOfNetworkTxt()
        );

        updateQRCodeButton(controller);
    }

    /**
     * Update the QR button label and behavior per mode.
     */
    public void updateQRCodeButton(ShipmentController controller) {
        /*if (currentKind(controller) == ShipmentController.ShipmentKind.RECEPTION) {
            controller.getQRCodeGeneraore().setText("Generate Reception QR");
            controller.getQRCodeGeneraore().setOnAction(e -> controller.getQrHandler().generateQRCodeForReception(controller));
        } else {
            controller.getQRCodeGeneraore().setText("Generate Expedition QR");
            controller.getQRCodeGeneraore().setOnAction(e -> controller.getQrHandler().generateQRCodeForExpedition(controller));
        }*/
        if (currentKind(controller) == ShipmentController.ShipmentKind.RECEPTION) {
            controller.getQRCodeGenerator().setVisible(false);
            controller.getQrCodeReader().setVisible(true);
        }
        else {
            controller.getQRCodeGenerator().setVisible(true);
            controller.getQrCodeReader().setVisible(false);
        }
    }

    /**
     * Helper to toggle visibility + layout management in one call.
     */
    public void setVisibleAndManaged(Control c, boolean v) {
        c.setVisible(v);
        c.setManaged(v);
    }
}
