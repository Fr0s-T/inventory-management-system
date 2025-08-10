package ViewsControllers.ShipmentForm.ShipmentUIHandlers;

import Models.Product;
import Models.Session;
import Models.ShipmentQRPayload;
import Models.Warehouse;
import Utilities.AlertUtils;
import Utilities.QRCodeUtils;
import ViewsControllers.ShipmentForm.QrShipmentMapper;
import ViewsControllers.ShipmentForm.ShipmentController;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;

/**
 *
 *
 * Author: @Frost
 *
 * Handles QR code generation, reading, and decoding for ShipmentController.
 */
public class ShipmentQrHandler {

    /* ---------- QR generation ---------- */

    /**
     * Generate a QR code that represents a RECEPTION (local perspective).
     * Source may be external (name) or internal (warehouse).
     */
    public void generateQRCodeForReception(ShipmentController controller) {
        Warehouse current = Session.getCurrentWarehouse();
        if (current == null) {
            AlertUtils.showWarning("No Current Warehouse", "Please select a current warehouse.");
            return;
        }

        Warehouse destination = current;
        Warehouse source = controller.getOutsideOfNetworkCheckBox().isSelected() ? null : controller.getSourceComboBox().getValue();
        String outsideName = controller.getOutsideOfNetworkTxt().getText().trim();

        File file = choosePngSaveLocation("Save Reception QR");
        if (file == null) return;

        controller.getFormHandler().generateQRCode(
                source,
                destination,
                !controller.getOutsideOfNetworkCheckBox().isSelected(),
                outsideName,
                mapToQr(ShipmentController.ShipmentKind.RECEPTION),
                file.getAbsolutePath()
        );
    }

    /**
     * Generate a QR code that represents an EXPEDITION (local perspective).
     * Destination may be external (name) or internal (warehouse).
     */
    public void generateQRCodeForExpedition(ShipmentController controller) {
        Warehouse current = Session.getCurrentWarehouse();
        if (current == null) {
            AlertUtils.showWarning("No Current Warehouse", "Please select a current warehouse.");
            return;
        }

        Warehouse source = current;
        Warehouse destination = controller.getOutsideOfNetworkCheckBox().isSelected() ? null : controller.getDestinationComboBox().getValue();
        String outsideName = controller.getOutsideOfNetworkTxt().getText().trim();

        File file = choosePngSaveLocation("Save Expedition QR");
        if (file == null) return;

        controller.getFormHandler().generateQRCode(
                source,
                destination,
                !controller.getOutsideOfNetworkCheckBox().isSelected(),
                outsideName,
                mapToQr(ShipmentController.ShipmentKind.EXPEDITION),
                file.getAbsolutePath()
        );
    }

    private File choosePngSaveLocation(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        return fc.showSaveDialog(null);
    }

    /* ---------- QR decoding with integrity check ---------- */

    /**
     * Decode a shipment QR image, infer the local kind, reconcile partner identity,
     * then populate the form with items and partner/warehouse fields.
     */
    public void decodeQRCodeAndLoadForm(ShipmentController controller, String filePath) {
        ShipmentQRPayload payload = QRCodeUtils.decodeShipmentQRCode(filePath);
        if (payload == null) {
            AlertUtils.showError("QR Code Error", "Could not decode or parse QR code.");
            return;
        }

        Warehouse current = Session.getCurrentWarehouse();
        int currentId = current != null ? current.getId() : -1;

        ShipmentController.ShipmentKind kind = controller.getQrMapper().decideLocalKind(payload, currentId);

        Integer declaredDestId = controller.getQrMapper().parsePositiveInt(payload.destination());
        if (declaredDestId != null && declaredDestId > 0 && declaredDestId != currentId) {
            boolean proceed = AlertUtils.showConfirmation(
                    "Shipment Not Intended For This Warehouse",
                    "The QR indicates destination warehouse ID " + declaredDestId +
                            ", which does not match this warehouse (ID " + currentId + ").\n\n" +
                            "Do you want to force entry into this warehouse?");
            if (!proceed) {
                return;
            }
        }

        // Temporarily suppress UI reactions while we programmatically update controls
        controller.getUiStateHandler().setSuppressUi(true);
        try {
            controller.getSourceComboBox().getSelectionModel().clearSelection();
            controller.getDestinationComboBox().getSelectionModel().clearSelection();
            controller.getOutsideOfNetworkTxt().clear();

            controller.getShipmentType().selectToggle(
                    kind == ShipmentController.ShipmentKind.RECEPTION
                            ? controller.getReceptionRadioButton()
                            : controller.getExpeditionRadioButton()
            );

            if (kind == ShipmentController.ShipmentKind.RECEPTION) {
                if (currentId > 0) controller.getComboHelper().selectById(controller.getDestinationComboBox(), currentId);
                controller.getDestinationComboBox().setDisable(true);

                QrShipmentMapper.Partner partner = controller.getQrMapper().resolvePartnerForReception(payload);
                if (partner instanceof QrShipmentMapper.PartnerId pid) {
                    controller.getComboHelper().selectById(controller.getSourceComboBox(), pid.id());
                    controller.getSourceComboBox().setDisable(false);
                    controller.getOutsideOfNetworkCheckBox().setSelected(false);
                } else if (partner instanceof QrShipmentMapper.PartnerExternal pext) {
                    controller.getOutsideOfNetworkTxt().setText(pext.name());
                    controller.getSourceComboBox().setDisable(true);
                    controller.getOutsideOfNetworkCheckBox().setSelected(true);
                }
            } else { // EXPEDITION
                if (currentId > 0) controller.getComboHelper().selectById(controller.getSourceComboBox(), currentId);
                controller.getSourceComboBox().setDisable(true);

                QrShipmentMapper.Partner partner = controller.getQrMapper().resolvePartnerForExpedition(payload);
                if (partner instanceof QrShipmentMapper.PartnerId pid) {
                    controller.getComboHelper().selectById(controller.getDestinationComboBox(), pid.id());
                    controller.getDestinationComboBox().setDisable(false);
                    controller.getOutsideOfNetworkCheckBox().setSelected(false);
                } else if (partner instanceof QrShipmentMapper.PartnerExternal pext) {
                    controller.getOutsideOfNetworkTxt().setText(pext.name());
                    controller.getDestinationComboBox().setDisable(true);
                    controller.getOutsideOfNetworkCheckBox().setSelected(true);
                }
            }
        } finally {
            controller.getUiStateHandler().setSuppressUi(false);
            controller.getUiStateHandler().applyUiState(controller);
            controller.getUiStateHandler().updateQRCodeButton(controller);
        }

        // Load items into the form
        ArrayList<Product> products = new ArrayList<>();
        ArrayList<Integer> quantities = new ArrayList<>();
        payload.items().forEach(entry -> {
            products.add(controller.getFormHandler().fromItemEntry(entry));
            quantities.add(entry.quantity());
        });
        controller.getFormHandler().setItems(products, quantities);

        AlertUtils.showSuccess("Shipment form loaded from QR code.");
    }

    /**
     * Open file chooser then decode a selected QR image.
     */
    public void onLoadQRCodeClick(ShipmentController controller) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select QR Code Image");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            decodeQRCodeAndLoadForm(controller, file.getAbsolutePath());
        }
    }

    /**
     * Map local shipment kind to the opposite label for QR payload (inversion).
     * RECEPTION → "Expedition", EXPEDITION → "Reception"
     */
    public String mapToQr(ShipmentController.ShipmentKind kind) {
        return (kind == ShipmentController.ShipmentKind.RECEPTION) ? "Expedition" : "Reception";
    }
}
