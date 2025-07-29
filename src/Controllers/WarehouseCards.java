package Controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import Models.Warehouse;

public class WarehouseCards {

    @FXML private AnchorPane card;         // Root container for clip
    @FXML private ImageView warehouseImage;  // ImageView for warehouse picture
    @FXML private Label locationlabel;
    @FXML private Label manegerlabel;
    @FXML private Label capacitylabel;

    @FXML
    public void initialize() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        card.setClip(clip);
    }

    public void setData(Warehouse warehouse) {
        locationlabel.setText(warehouse.getLocation());
        manegerlabel.setText(String.valueOf(warehouse.getManegeId()));
        capacitylabel.setText(String.valueOf(warehouse.getCapacity()));
        // Optionally set an image in warehouseImage here if available
    }
}
