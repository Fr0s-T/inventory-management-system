package ViewsControllers;

import Controllers.SceneLoader;
import Models.Session;
import Models.Warehouse;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

/**
 *
 * Author: @Frost
 *
 */

public class WarehouseCards {


    @FXML private Pane card;
    @FXML private ImageView WarehouseImage;
    @FXML private Label LocationLabel;
    @FXML private Label ManagerLabel;
    @FXML private Label CapacityLabel;
    @FXML private Label WarehouseNameLabel;
    Warehouse warehouse;


    public void setData(Warehouse warehouse) {
        LocationLabel.setText(warehouse.getLocation());
        ManagerLabel.setText(String.valueOf(warehouse.getManegeUSerName()));
        CapacityLabel.setText(String.valueOf(warehouse.getCapacity()));
        WarehouseNameLabel.setText(warehouse.getName());
        this.warehouse = warehouse;

    }

    public void initialize() {

        card.setOnMouseClicked(mouseEvent -> {
            Session.setCurrentWarehouse(warehouse);
            SceneLoader.loadScene("/FXML/UserFrame.fxml",null);
        });

    }



}
