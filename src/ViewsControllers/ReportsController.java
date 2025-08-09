package ViewsControllers;

import Models.*;
import Services.ProductsService;
import Services.ReportsService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Author: @Ilia
 *
 */


public class ReportsController {
    @FXML private Button EmployeesInfoBtn;
    @FXML private Button StockValuationBtn;
    @FXML private Button ReceivedBtn;
    @FXML private Button SentBtn;
    @FXML private Button ReceivedItemsBtn;
    @FXML private Button SentItemsBtn;
    @FXML private TextField StockLeveltxt;
    @FXML private TextField Valuetxt;
    @FXML private Label StockLevelL;
    @FXML private Label ValueL;
    //Employees table and columns
    @FXML private TableView<User> EmployeesTable;
    @FXML private TableColumn<User, Integer> EmpIdC;
    @FXML private TableColumn<User, String> FnameC;
    @FXML private TableColumn<User, String> MnameC;
    @FXML private TableColumn<User, String> LnameC;
    @FXML private TableColumn<User, String> UsernameC;
    @FXML private TableColumn<User, String> RoleC;
    @FXML private TableColumn<User, String> StatusC;
    //Shipments table and columns
    @FXML private TableView<Shipment> ShipmentsTable;
    @FXML private TableColumn<Shipment, Integer> ShipmentIdC;
    @FXML private TableColumn<Shipment, String> DateC;
    @FXML private TableColumn<Shipment, String> SourceC;
    @FXML private TableColumn<Shipment, String> DestinationC;
    @FXML private TableColumn<Shipment, Integer> QuantityC;
    @FXML private TableColumn<Shipment, String> HandledByC;
    //Stock Level table and columns
    @FXML private TableView<Product> StockLevelTable;
    @FXML private TableColumn<Product, String> ItemCodeC;
    @FXML private TableColumn<Product, Integer> ItemQuantityC;
    @FXML private TableColumn<Product, Integer> UnitPriceC;
    @FXML private TableColumn<Product, String> TotalPriceC;
    //Items table and columns
    @FXML private TableView<ShipmentDetails> ItemsTable;
    @FXML private TableColumn<ShipmentDetails, String> ItemCodeC2;
    @FXML private TableColumn<ShipmentDetails, Integer> ItemQuantityC2;
    @FXML private TableColumn<ShipmentDetails, String> TotalPriceC2;
    @FXML private TableColumn<ShipmentDetails, String> ShipmentIdC2;


    @FXML
    public void initialize() {
        ClearPage();

        EmployeesInfoBtn.setOnAction(actionEvent -> showEmployeesTable());
        LockColumns(EmployeesTable);

        ShipmentIdC.setCellValueFactory(new PropertyValueFactory<>("id"));
        DateC.setCellValueFactory(new PropertyValueFactory<>("date"));
        SourceC.setCellValueFactory(new PropertyValueFactory<>("source"));
        DestinationC.setCellValueFactory(new PropertyValueFactory<>("destination"));
        QuantityC.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        HandledByC.setCellValueFactory(new PropertyValueFactory<>("handledBy"));

        ReceivedBtn.setOnAction(actionEvent -> showReceptionShipments());
        SentBtn.setOnAction(actionEvent -> showExpeditionShipments());
        LockColumns(ShipmentsTable);

        ReceivedItemsBtn.setOnAction(actionEvent -> showReceivedItems());
        SentItemsBtn.setOnAction(actionEvent -> showSentItems());

        // ItemsTable column mapping
        ShipmentIdC2.setCellValueFactory(new PropertyValueFactory<>("shipmentId"));
        ItemCodeC2.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        ItemQuantityC2.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TotalPriceC2.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        LockColumns(ItemsTable);

        //Initialize Valuation table and columns
        StockValuationBtn.setOnAction(actionEvent -> showValuationTable());
        LockColumns(StockLevelTable);
    }


    @FXML
    private void showEmployeesTable() {

        EmpIdC.setCellValueFactory(new PropertyValueFactory<>("id"));
        FnameC.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        MnameC.setCellValueFactory(new PropertyValueFactory<>("middleName"));
        LnameC.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        UsernameC.setCellValueFactory(new PropertyValueFactory<>("username"));
        RoleC.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole().name()));
        StatusC.setCellValueFactory(cellData -> {
            boolean onDuty = cellData.getValue().getOnDuty();
            String statusText = onDuty ? "On Duty" : "Off Duty";
            return new SimpleStringProperty(statusText);

        });

        ItemsTable.setVisible(false);
        EmployeesTable.setVisible(true);
        ShipmentsTable.setVisible(false);
        StockLeveltxt.setVisible(false);
        Valuetxt.setVisible(false);
        StockLevelL.setVisible(false);
        ValueL.setVisible(false);
        StockLevelTable.setVisible(false);
        try {
            ArrayList<User> users = ReportsService.getEmployeesFromDb();
            EmployeesTable.setItems(javafx.collections.FXCollections.observableArrayList(users));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showValuationTable() {
        ItemsTable.setVisible(false);
        EmployeesTable.setVisible(false);
        ShipmentsTable.setVisible(false);
        StockLeveltxt.setVisible(true);
        Valuetxt.setVisible(true);
        StockLevelL.setVisible(true);
        ValueL.setVisible(true);
        StockLevelTable.setVisible(true);

        if (Session.getProducts() == null) ProductsService.getProducts();

        ItemCodeC.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        ItemQuantityC.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        UnitPriceC.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        TotalPriceC.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            double total = product.getQuantity() * product.getUnitPrice();
            return new SimpleStringProperty(String.format("%.2f", total));
        });

        StockLevelTable.setItems(FXCollections.observableArrayList(Session.getProducts()));
        int totalQuantity = StockLevelTable.getItems()
                .stream()
                .mapToInt(Product::getQuantity)
                .sum();
        StockLeveltxt.setText(String.valueOf(totalQuantity));
        double totalValue = StockLevelTable.getItems()
                .stream()
                .mapToDouble(p -> p.getQuantity() * p.getUnitPrice())
                .sum();

        Valuetxt.setText(String.format("%.2f", totalValue));





    }

    private void LockColumns(TableView<?> tableView) {
        Platform.runLater(() -> {
            tableView.lookupAll(".column-header").forEach(node -> {
                node.setOnMouseDragged(event -> event.consume());  // disable dragging
                node.setOnMousePressed(event -> event.consume());  // prevent header click-drag start
            });
        });
    }
    @FXML
    private void showReceptionShipments() {
        try {
            ArrayList<Shipment> allShipments = ReportsService.getShipmentsFromDb();
            String currentWarehouseName = Session.getCurrentWarehouse().getName();

            List<Shipment> receptions = allShipments.stream()
                    .filter(s -> s.getDestination().equals(currentWarehouseName))
                    .toList();

            ShipmentsTable.getItems().setAll(receptions);
            ShipmentsTable.setVisible(true);
            ItemsTable.setVisible(false);
            EmployeesTable.setVisible(false);
            StockLeveltxt.setVisible(false);
            Valuetxt.setVisible(false);
            StockLevelL.setVisible(false);
            ValueL.setVisible(false);
            StockLevelTable.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showExpeditionShipments() {
        try {
            ArrayList<Shipment> allShipments = ReportsService.getShipmentsFromDb();
            String currentWarehouseName = Session.getCurrentWarehouse().getName();

            List<Shipment> expeditions = allShipments.stream()
                    .filter(s -> s.getSource().equals(currentWarehouseName))
                    .toList();

            ShipmentsTable.getItems().setAll(expeditions);
            ShipmentsTable.setVisible(true);
            ItemsTable.setVisible(false);
            EmployeesTable.setVisible(false);
            StockLeveltxt.setVisible(false);
            Valuetxt.setVisible(false);
            StockLevelL.setVisible(false);
            ValueL.setVisible(false);
            StockLevelTable.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace(); // Handle error (you can show an alert)
        }
    }
    @FXML
    private void showSentItems() {
        try {

            ShipmentsTable.setVisible(false);
            ItemsTable.setVisible(true);
            EmployeesTable.setVisible(false);
            StockLeveltxt.setVisible(false);
            Valuetxt.setVisible(false);
            StockLevelL.setVisible(false);
            ValueL.setVisible(false);
            StockLevelTable.setVisible(false);

            ArrayList<ShipmentDetails> sentItems = ReportsService.getSentItems();
            ItemsTable.setItems(FXCollections.observableArrayList(sentItems));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void showReceivedItems() {
        try {
            ShipmentsTable.setVisible(false);
            ItemsTable.setVisible(true);
            EmployeesTable.setVisible(false);
            StockLeveltxt.setVisible(false);
            Valuetxt.setVisible(false);
            StockLevelL.setVisible(false);
            ValueL.setVisible(false);
            StockLevelTable.setVisible(false);

            ArrayList<ShipmentDetails> ReceivedItems = ReportsService.getReceivedItems();
            ItemsTable.setItems(FXCollections.observableArrayList(ReceivedItems));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void ClearPage(){

        StockLeveltxt.setVisible(false);
        Valuetxt.setVisible(false);
        StockLevelL.setVisible(false);
        ValueL.setVisible(false);
        StockLevelTable.setVisible(false);
        EmployeesTable.setVisible(false);
        ShipmentsTable.setVisible(false);
        ItemsTable.setVisible(false);
    }





}
