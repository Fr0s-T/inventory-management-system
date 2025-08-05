package ViewsControllers;

import Models.Product;
import Models.Session;
import Services.ProductsService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * Author: @Frost
 *
 */


public class ProductsController {


    @FXML private TableView<Product> ProductsTable;
    @FXML private TableColumn<Product, String> pictureColumn;
    @FXML private TableColumn<Product, String> itemCodeColumn;
    @FXML private TableColumn<Product, String> colorColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, String> sizeColumn;
    @FXML private TableColumn<Product, String> sectionColumn;

    @FXML
    public void initialize() {

        if (Session.getProducts() == null) ProductsService.getProducts();
        // Set up columns
        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        sectionColumn.setCellValueFactory(new PropertyValueFactory<>("section"));
        pictureColumn.setCellValueFactory(new PropertyValueFactory<>("picture"));
        ProductsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        // Fill the table
        ProductsTable.setItems(FXCollections.observableArrayList(Session.getProducts()));

    }
}
