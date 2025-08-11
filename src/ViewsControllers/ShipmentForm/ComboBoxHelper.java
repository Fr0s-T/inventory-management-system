package ViewsControllers.ShipmentForm;

import Models.Product;
import Models.Warehouse;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.util.function.Function;

import java.util.ArrayList;


/**
 *
 *
 * Author: @Frost
 *
 */


public class ComboBoxHelper {

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

    /**
     * Enable "type to search" ONLY while the ComboBox popup is open.
     * - When the popup opens: we snapshot items, start listening to keystrokes on the popup list,
     *   and filter by a case-insensitive substring.
     * - When the popup closes: we remove listeners, restore the original items, and clear the query.
     * <p>
     * Why this approach?
     * - No global key handling. We only listen while THIS combo box is actually open.
     * - No side effects on other ComboBoxes in the same view.
     * - Non-editable ComboBox is fine; we listen directly on the popup ListView.
     */
    public static <T> void enableSearchWhileOpen(ComboBox<T> combo, Function<T, String> toText) {
        // Attach exactly one listener for the open/close lifecycle
        combo.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (!isShowing) return;

            // --- Popup just opened ---
            // Take a fresh snapshot of current items (in case your list changes over time).
            ObservableList<T> original = FXCollections.observableArrayList(combo.getItems());
            FilteredList<T> filtered = new FilteredList<>(original, it -> true);
            combo.setItems(filtered);

            // We'll keep a small buffer of typed characters and clear it after a short pause.
            StringBuilder query = new StringBuilder();
            PauseTransition idleReset = new PauseTransition(Duration.millis(700)); // feels snappy without being jumpy
            idleReset.setOnFinished(e -> query.setLength(0));

            // Helper to (re)apply the filter and keep UX nice.
            Runnable applyFilter = () -> {
                String q = query.toString().toLowerCase();
                filtered.setPredicate(item -> {
                    if (q.isEmpty()) return true;
                    String s = toText.apply(item);
                    return s != null && s.toLowerCase().contains(q);
                });

                // Auto-select and scroll to the first match (if any).
                if (!filtered.isEmpty()) {
                    combo.getSelectionModel().select(0);
                    var skin = combo.getSkin();
                    if (skin instanceof ComboBoxListViewSkin<?> listSkin) {
                        @SuppressWarnings("unchecked")
                        ListView<T> lv = (ListView<T>) listSkin.getPopupContent();
                        lv.scrollTo(0);
                    }
                }
            };

            // We need the popup's ListView to hook key events.
            Platform.runLater(() -> {
                var skin = combo.getSkin();
                if (!(skin instanceof ComboBoxListViewSkin<?> listSkin)) return;

                @SuppressWarnings("unchecked")
                ListView<T> listView = (ListView<T>) listSkin.getPopupContent();

                // Typed characters (letters, numbers, etc.)
                EventHandler<KeyEvent> typedHandler = e -> {
                    String ch = e.getCharacter();
                    // Ignore control chars and empty input
                    if (ch != null && !ch.isEmpty() && ch.charAt(0) >= 32) {
                        query.append(ch.toLowerCase());
                        idleReset.playFromStart();
                        applyFilter.run();
                        e.consume(); // don't let the list do its default jump-to behavior at the same time
                    }
                };

                // Useful non-typed keys: BACKSPACE / ESC / ENTER
                EventHandler<KeyEvent> pressedHandler = e -> {
                    if (e.getCode() == KeyCode.BACK_SPACE) {
                        if (query.length() > 0) {
                            query.setLength(query.length() - 1);
                            applyFilter.run();
                        }
                        e.consume();
                    } else if (e.getCode() == KeyCode.ESCAPE) {
                        // Clear and close. (Feels natural when you want to cancel.)
                        query.setLength(0);
                        applyFilter.run();
                        combo.hide();
                        e.consume();
                    } else if (e.getCode() == KeyCode.ENTER) {
                        // Commit current selection and close the popup.
                        combo.hide();
                        e.consume();
                    }
                };

                // Start listening only while open
                listView.addEventFilter(KeyEvent.KEY_TYPED, typedHandler);
                listView.addEventFilter(KeyEvent.KEY_PRESSED, pressedHandler);

                // When the popup closes, restore everything and stop listening.
                combo.showingProperty().addListener(new javafx.beans.value.ChangeListener<Boolean>() {
                    @Override public void changed(javafx.beans.value.ObservableValue<? extends Boolean> o, Boolean was, Boolean now) {
                        if (!now) {
                            // --- Popup just closed ---
                            listView.removeEventFilter(KeyEvent.KEY_TYPED, typedHandler);
                            listView.removeEventFilter(KeyEvent.KEY_PRESSED, pressedHandler);
                            idleReset.stop();
                            query.setLength(0);
                            filtered.setPredicate(it -> true); // unfilter
                            combo.setItems(original);          // restore original list
                            // Remove this inner listener (avoid piling up listeners across openings)
                            combo.showingProperty().removeListener(this);
                        }
                    }
                });
            });
        });
    }
}