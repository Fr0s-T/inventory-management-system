import javafx.application.Application;
import javafx.stage.Stage;
import Controllers.SceneLoader;
import Services.ProductsService;

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
            // primaryStage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace(); // Print full error for debugging
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // Stop background sync gracefully when app closes
        ProductsService.stopBackgroundSync();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
