package Controllers;

import Models.Warehouse;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
public class WarehouseCards {


    @FXML private Pane card;
    @FXML private ImageView WarehouseImage;
    @FXML private Label LocationLabel;
    @FXML private Label ManagerLabel;
    @FXML private Label CapacityLabel;
    @FXML private Label WarehouseNameLabel;


    public void setData(Warehouse warehouse) {
        LocationLabel.setText(warehouse.getLocation());
        ManagerLabel.setText(String.valueOf(warehouse.getManegeId()));
        CapacityLabel.setText(String.valueOf(warehouse.getCapacity()));
        WarehouseNameLabel.setText(warehouse.getName());
    }

    public void initialize() {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);
    }



}
