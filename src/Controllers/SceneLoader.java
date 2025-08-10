package Controllers;

import ViewsControllers.*;
import ViewsControllers.ShipmentForm.ShipmentController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import Models.Warehouse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Utility class for managing scene/card loading and lightweight navigation.
 *
 * Core ideas:
 * - Keep all FXML loads in ONE place (this class).
 * - Cache the main dynamic panel so child controllers (e.g., product/shipment cards) can trigger refresh
 *   WITHOUT needing a direct reference to UserFrame or the panel itself.
 * - Provide overloads: loadX(panel) for first-time mounting, loadX() for refresh.
 * - Track which view is currently mounted in the panel to support universal refresh().
 *
 * Author: @Frost
 */
public class SceneLoader {

    /** Primary (top-level) stage for full scene swaps (e.g., Login → UserFrame). */
    private static Stage primaryStage;

    /** Name of the last loaded view inside the dynamic panel (e.g., "Products.fxml"). */
    private static String currentPanelView = "";

    /** Cached reference to the dynamic panel area inside UserFrame. */
    private static AnchorPane cachedDynamicPanel = null;

    /** Optional: keep last loader for advanced cases (not required for basic refresh). */
    // private static FXMLLoader lastPanelLoader = null;

    /* ===================== Public bootstrap APIs ===================== */

    /** Set the app's primary stage so full scene loads (non-panel) can be applied. */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /** Store the dynamic panel reference so later calls can refresh without passing it each time. */
    public static void setDynamicPanel(AnchorPane panel) {
        cachedDynamicPanel = panel;
    }

    /** Get current panel view name ("Products.fxml", "Shipment.fxml", ...). */
    public static String getCurrentPanelView() {
        return currentPanelView;
    }

    /* ===================== Full-scene load (rare) ===================== */

    /**
     * Load a full screen scene by FXML path.
     * Use this for major screens (LoginPage.fxml → UserFrame.fxml), not for dynamic panel cards.
     *
     * @param fxmlPath  Classpath to the FXML (e.g., "/FXML/UserFrame.fxml")
     * @param controller Optional controller instance (null if you rely on fx:controller in FXML)
     */
    public static void loadScene(String fxmlPath, Object controller) {
        runOnFxThread(() -> {
            if (primaryStage == null) {
                throw new IllegalStateException("Primary stage not set. Call SceneLoader.setPrimaryStage(stage) first.");
            }
            try {
                FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
                if (controller != null) {
                    loader.setController(controller);
                }
                Parent root = loader.load();

                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                primaryStage.centerOnScreen();
                primaryStage.show();

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to load scene: " + fxmlPath, e);
            }
        });
    }

    /* ===================== Dynamic panel card loaders ===================== */

    /** Load Warehouse cards into a FlowPane (utility for dashboard-like views). */
    public static void loadWarehouseCards(ArrayList<Warehouse> warehouses, FlowPane flowPane) {
        runOnFxThread(() -> {
            flowPane.getChildren().clear();
            flowPane.setOrientation(Orientation.HORIZONTAL);
            flowPane.setPrefWrapLength(600);
            flowPane.setHgap(20);
            flowPane.setVgap(20);

            for (Warehouse warehouse : warehouses) {
                try {
                    FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource("/Views/WarehouseCards.fxml"));
                    Node card = loader.load();

                    WarehouseCards controller = loader.getController();
                    controller.setData(warehouse);

                    flowPane.getChildren().add(card);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* ---------- Products ---------- */

    /** First-time mount or explicit refresh when you already have the panel. */
    public static void loadProducts(AnchorPane dynamicPanel) throws IOException {
        mountIntoPanel(dynamicPanel, "/Views/Products.fxml", "Products.fxml");
    }

    /** Refresh using cached panel (for refresh buttons inside the product card). */
    public static void loadProducts() throws IOException {
        ensurePanelCached();
        loadProducts(cachedDynamicPanel);
    }

    /* ---------- Shipment ---------- */

    public static void loadShipment(AnchorPane dynamicPanel) throws IOException {
        mountIntoPanel(dynamicPanel, "/Views/Shipment.fxml", "Shipment.fxml");
    }

    public static void loadShipment() throws IOException {
        ensurePanelCached();
        loadShipment(cachedDynamicPanel);
    }

    /* ---------- Users (Edit Employee) ---------- */

    public static void loadEditEmployee(AnchorPane dynamicPanel) throws IOException {
        mountIntoPanel(dynamicPanel, "/Views/Users.fxml", "Users.fxml");
    }

    public static void loadEditEmployee() throws IOException {
        ensurePanelCached();
        loadEditEmployee(cachedDynamicPanel);
    }

    /* ---------- Reports ---------- */

    public static void loadReports(AnchorPane dynamicPanel) throws IOException {
        mountIntoPanel(dynamicPanel, "/Views/Reports.fxml", "Reports.fxml");
    }

    public static void loadReports() throws IOException {
        ensurePanelCached();
        loadReports(cachedDynamicPanel);
    }

    /* ===================== Universal refresh ===================== */

    /**
     * Reload the currently mounted panel view.
     * Works after any loadX(...) call that set currentPanelView.
     *
     * Example use from inside a card controller:
     *   RefreshButton.setOnAction(e -> SceneLoader.refreshPanel());
     */
    public static void refreshPanel() {
        ensurePanelCached();
        runOnFxThread(() -> {
            try {
                System.out.println("🔄 Refresh requested for panel: " + currentPanelView);

                switch (currentPanelView) {
                    case "Products.fxml" -> loadProducts(cachedDynamicPanel);
                    case "Shipment.fxml" -> loadShipment(cachedDynamicPanel);
                    case "Users.fxml"    -> loadEditEmployee(cachedDynamicPanel);
                    case "Reports.fxml"  -> loadReports(cachedDynamicPanel);
                    default -> {
                        System.out.println("⚠ No matching panel found for: " + currentPanelView);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }


    /* ===================== Internals ===================== */

    /** Ensure we have a cached panel for no-arg loaders. */
    private static void ensurePanelCached() {
        if (cachedDynamicPanel == null) {
            throw new IllegalStateException("No dynamic panel cached. Call setDynamicPanel(panel) or use loadX(panel) first.");
        }
    }

    /** Mount a single FXML card into the given panel and anchor it to all sides. */
    private static void mountIntoPanel(AnchorPane dynamicPanel, String resourcePath, String logicalName) throws IOException {
        // Keep cached reference updated so later refresh calls work
        cachedDynamicPanel = dynamicPanel;

        // Load and anchor child
        FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(resourcePath));
        Node card = loader.load();

        runOnFxThread(() -> {
            dynamicPanel.getChildren().clear();
            dynamicPanel.getChildren().add(card);
            AnchorPane.setTopAnchor(card, 0.0);
            AnchorPane.setBottomAnchor(card, 0.0);
            AnchorPane.setLeftAnchor(card, 0.0);
            AnchorPane.setRightAnchor(card, 0.0);

            currentPanelView = logicalName;
        });

        // Optionally keep the loader if you want controller access:
        // lastPanelLoader = loader;
    }

    /** Ensure we always update UI from the FX Application Thread. */
    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
