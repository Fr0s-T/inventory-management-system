package ViewsControllers;

import Models.Session;
import Models.Shipment;
import Models.User;
import Models.Warehouse;
import Services.ReportsService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

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
    @FXML private TableColumn<Shipment, String> HandeledByC;
    //Stock Level table and columns
    @FXML private TableView<Shipment> StockLevelTable;
    @FXML private TableColumn<Shipment, Integer> ItemCodeC;
    @FXML private TableColumn<Shipment, String> ItemQuantityC;
    @FXML private TableColumn<Shipment, String> UnitPriceC;
    @FXML private TableColumn<Shipment, String> TotalPriceC;


    @FXML public void initialize(){

        EmployeesTable.setVisible(false);
        ShipmentsTable.setVisible(false);

        //Initialize Employees Table Columns

        EmpIdC.setCellValueFactory(new PropertyValueFactory<>("id"));
        FnameC.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        MnameC.setCellValueFactory(new PropertyValueFactory<>("middleName"));
        LnameC.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        UsernameC.setCellValueFactory(new PropertyValueFactory<>("username"));
        RoleC.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole().name())); // enum as string
        StatusC.setCellValueFactory(cellData -> {
            boolean onDuty = cellData.getValue().getOnDuty();
            String statusText = onDuty ? "On Duty" : "Off Duty";
            return new SimpleStringProperty(statusText);

        });

        EmployeesInfoBtn.setOnAction(actionEvent -> showEmployeesTable());
        LockColumns(EmployeesTable);

        //Initialize Shipments Table Columns

        ShipmentIdC.setCellValueFactory(new PropertyValueFactory<>("id"));
        DateC.setCellValueFactory(new PropertyValueFactory<>("date"));
        SourceC.setCellValueFactory(new PropertyValueFactory<>("source"));
        DestinationC.setCellValueFactory(new PropertyValueFactory<>("destination"));
        QuantityC.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        HandeledByC.setCellValueFactory(new PropertyValueFactory<>("handledBy"));

        ReceivedBtn.setOnAction(actionEvent -> showReceptionShipments());
        SentBtn.setOnAction(actionEvent -> showExpeditionShipments());
        LockColumns(ShipmentsTable);
    }

    @FXML
    private void showEmployeesTable() {
        EmployeesTable.setVisible(true);
        ShipmentsTable.setVisible(false);
        try {
            ArrayList<User> users = ReportsService.getEmployeesFromDb();
            EmployeesTable.setItems(javafx.collections.FXCollections.observableArrayList(users));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            EmployeesTable.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace(); // Handle error (you can show an alert)
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
            EmployeesTable.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace(); // Handle error (you can show an alert)
        }
    }





}
