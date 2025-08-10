package ViewsControllers;

import Controllers.SceneLoader;
import Models.Product;
import Models.Session;
import Services.ProductsService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProductsController {

    @FXML private TextField SearchTxtField;
    @FXML private TableView<Product> ProductsTable;
    @FXML private TableColumn<Product, String> pictureColumn;
    @FXML private TableColumn<Product, String> NameColumn;
    @FXML private TableColumn<Product, String> itemCodeColumn;
    @FXML private TableColumn<Product, String> colorColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, String> sizeColumn;
    @FXML private TableColumn<Product, String> sectionColumn;
    @FXML private TableColumn<Product, String> UnitPriceColumn;
    @FXML private Button RefreshButton;

    private ObservableList<Product> originalProductList;

    @FXML
    public void initialize() {
        if (Session.getProducts() == null) ProductsService.getProducts();

        originalProductList = FXCollections.observableArrayList(Session.getProducts());

        // Set up columns
        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        NameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        sectionColumn.setCellValueFactory(new PropertyValueFactory<>("section"));
        pictureColumn.setCellValueFactory(new PropertyValueFactory<>("picture"));
        UnitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        ProductsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Fill the table
        ProductsTable.setItems(originalProductList);

        RefreshButton.setOnAction(actionEvent -> SceneLoader.refreshPanel());

        // Disable column dragging
        Platform.runLater(() -> {
            ProductsTable.lookupAll(".column-header").forEach(node -> {
                node.setOnMouseDragged(Event::consume);
                node.setOnMousePressed(Event::consume);
            });
        });

        // Add listener to search field
        SearchTxtField.textProperty().addListener((observable, oldValue, newValue)
                -> searchProducts(newValue));
    }

    private void searchProducts(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            ProductsTable.setItems(originalProductList); // show all if empty
            return;
        }

        String lowerCaseSearch = searchText.toLowerCase();

        ObservableList<Product> filteredList = originalProductList.filtered(product -> {
            boolean matchesName = product.getName() != null &&
                    product.getName().toLowerCase().contains(lowerCaseSearch);

            boolean matchesCode = product.getItemCode() != null &&
                    product.getItemCode().toLowerCase().contains(lowerCaseSearch);

            return matchesName || matchesCode;
        });

        ProductsTable.setItems(filteredList);
    }

}
