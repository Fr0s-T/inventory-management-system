import Models.Session;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import Controllers.SceneLoader;
import Services.ProductsService;

import java.awt.*;

/**
 *
 * Authors: @Frost, @Ilia
 * Entry point of the program
 *
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            SceneLoader.setPrimaryStage(primaryStage);
            SceneLoader.loadScene("/FXML/LoginPage.fxml", null);
            primaryStage.setTitle("Inventory Management System");
            primaryStage.getIcons().clear();
            primaryStage.getIcons().add(new Image("/Icons/Possible logos/Screenshot__79_-removebg-preview.png"));




            primaryStage.setOnCloseRequest(event -> {
                Services.LogOutService.handleExit(); // Safely logs out current user
                Services.ProductsService.stopBackgroundSync(); // Optional cleanup
                System.out.println("🔚 Application is closing...");
                Models.Session.logOut();
                javafx.application.Platform.exit();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // Optional: ensure services shut down
        ProductsService.stopBackgroundSync();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
