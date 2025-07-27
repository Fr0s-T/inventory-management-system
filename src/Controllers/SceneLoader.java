package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Utility class for managing scene transitions across the application.
 * Author: @Frost
 * Improvements:
 * - Better error handling with specific exceptions
 * - Support for custom root and scene creation
 * - Resource existence validation
 * - Stage management options
 */
public class SceneLoader {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Primary stage cannot be null");
        }
        primaryStage = stage;
    }

    /**
     * Loads a new scene with optional controller
     * @param fxmlPath Path to FXML file (relative to resources folder)
     * @param controller Controller instance (can be null)
     */
    public static void loadScene(String fxmlPath, Object controller) {
        try {
            // Validate resource exists
            URL resource = SceneLoader.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }

            // Load FXML
            FXMLLoader loader = new FXMLLoader(resource);
            if (controller != null) {
                loader.setController(controller);
            }

            Parent root = loader.load();
            showScene(root);
        } catch (IOException e) {
            throw new SceneLoadException("Failed to load scene: " + fxmlPath, e);
        }
    }

    /**
     * Displays a scene with default settings
     * @param root The root node of the scene
     */
    public static void showScene(Parent root) {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage not set. Call setPrimaryStage() first.");
        }

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Custom exception for scene loading failures
     */
    public static class SceneLoadException extends RuntimeException {
        public SceneLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}