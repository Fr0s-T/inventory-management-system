package Controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class WarehouseCards {


//    @FXML
//    private AnchorPane card;
//
//    @FXML
//    public void initialize() {
//        Rectangle clip = new Rectangle();
//        clip.widthProperty().bind(card.widthProperty());
//        clip.heightProperty().bind(card.heightProperty());
//        clip.setArcWidth(100);   // match your CSS radius
//        clip.setArcHeight(100);  // match your CSS radius
//        card.setClip(clip);
//    }

    @FXML
    private Pane card;

    public void initialize() {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);
    }



}
