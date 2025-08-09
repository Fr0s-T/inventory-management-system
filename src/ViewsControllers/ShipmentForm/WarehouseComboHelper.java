package ViewsControllers.ShipmentForm;

import Models.Product;
import Models.Warehouse;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.ArrayList;

public class WarehouseComboHelper {

    public void setupWarehouseComboBox(ComboBox<Warehouse> combo, ArrayList<Warehouse> warehouses) {
        combo.getItems().setAll(warehouses);

        combo.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(Warehouse w, boolean empty) {
                super.updateItem(w, empty);
                setText(empty || w == null ? "" : safeName(w));
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Warehouse w, boolean empty) {
                super.updateItem(w, empty);
                setText(empty || w == null ? "" : safeName(w));
            }
        });
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Warehouse w) { return w == null ? "" : safeName(w); }
            @Override public Warehouse fromString(String s) { return null; }
        });
    }

    public void setupProductComboBox(ComboBox<Product> combo, ArrayList<Product> products) {
        combo.getItems().setAll(products);
        combo.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getItemCode());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getItemCode());
            }
        });
    }

    public void selectById(ComboBox<Warehouse> combo, int id) {
        if (id <= 0) return;
        for (Warehouse w : combo.getItems()) {
            if (w.getId() == id) {
                combo.getSelectionModel().select(w);
                return;
            }
        }
    }

    private static String safeName(Warehouse w) {
        String n = w.getName();
        return (n == null) ? "" : n;
    }
}
