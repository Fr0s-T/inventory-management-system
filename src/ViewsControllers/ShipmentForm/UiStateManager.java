package ViewsControllers.ShipmentForm;

import Models.Warehouse;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

public class UiStateManager {

    public void apply(ShipmentController.ShipmentKind kind,
                      boolean outside,
                      Warehouse current,
                      ComboBox<Warehouse> sourceCombo,
                      ComboBox<Warehouse> destCombo,
                      Control itemCodeTxt,
                      Control expeditionCombo,
                      TextField outsideTxt) {

        int currentId = (current != null) ? current.getId() : -1;

        if (kind == ShipmentController.ShipmentKind.EXPEDITION) {
            if (currentId > 0) {
                selectById(sourceCombo, currentId);
            }
            sourceCombo.setDisable(true);

            destCombo.setDisable(outside);
            outsideTxt.setDisable(!outside);

            setVisibleAndManaged(itemCodeTxt, false);
            setVisibleAndManaged(expeditionCombo, true);
        } else {
            if (currentId > 0) {
                selectById(destCombo, currentId);
            }
            destCombo.setDisable(true);

            sourceCombo.setDisable(outside);
            outsideTxt.setDisable(!outside);

            setVisibleAndManaged(itemCodeTxt, true);
            setVisibleAndManaged(expeditionCombo, false);
        }
    }

    private void setVisibleAndManaged(Control c, boolean v) {
        c.setVisible(v);
        c.setManaged(v);
    }

    private void selectById(ComboBox<Warehouse> combo, int id) {
        if (id <= 0) return;
        for (Warehouse w : combo.getItems()) {
            if (w.getId() == id) {
                combo.getSelectionModel().select(w);
                return;
            }
        }
    }
}
