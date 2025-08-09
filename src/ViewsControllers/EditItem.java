package ViewsControllers;

import Models.Product;
import Services.EditProductService;
import Services.ProductsService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Map;

public class EditItem {

    @FXML private TextField itemCodetxt;
    @FXML private TextField productNametxt;
    @FXML private TextField unitPricetxt;
    @FXML private TextField colortxt;
    @FXML private TextField sizetxt;
    @FXML private TextField sectiontxt;
    @FXML private Button FetchButton;
    @FXML private Button ClearButton;
    @FXML private Button SaveButton;
    @FXML private Button CancelButton;



    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void initialize(){

        CancelButton.setOnAction(e -> dialogStage.close());
        ClearButton.setOnAction(actionEvent -> {

            itemCodetxt.clear();
            productNametxt.clear();
            unitPricetxt.clear();
            colortxt.clear();
            sizetxt.clear();
            sectiontxt.clear();

        });
        FetchButton.setOnAction(actionEvent -> {
            String itemCode = itemCodetxt.getText().trim();
            if (itemCode.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Required", null, "Please enter an Item Code.");
                return;
            }

            try {
                Map<String, Product> catalog = ProductsService.getGlobalProductCatalog();
                Product product = catalog.get(itemCode);

                if (product != null) {
                    productNametxt.setText(product.getName());
                    unitPricetxt.setText(String.valueOf(product.getUnitPrice()));
                    colortxt.setText(product.getColor());
                    sizetxt.setText(product.getSize());
                    sectiontxt.setText(product.getSection());
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Not Found", null, "No product found for code: " + itemCode);
                    productNametxt.clear();
                    unitPricetxt.clear();
                    colortxt.clear();
                    sizetxt.clear();
                    sectiontxt.clear();
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", null, "An error occurred while fetching product data.");
            }
        });


        SaveButton.setOnAction(actionEvent -> {
            try {
                // Validate required fields
                if (itemCodetxt.getText().isEmpty() ||
                        productNametxt.getText().isEmpty() ||
                        unitPricetxt.getText().isEmpty()) {

                    showAlert(Alert.AlertType.WARNING, "Validation Error", null,
                            "Please fill in Item Code, Product Name, and Unit Price.");
                    return;
                }

                // Parse unit price
                float unitPrice;
                try {
                    unitPrice = Float.parseFloat(unitPricetxt.getText().trim());
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", null,
                            "Unit Price must be a valid number.");
                    return;
                }

                Product product = new Product(
                        itemCodetxt.getText().trim(),
                        colortxt.getText().trim(),
                        0,  // quantity unknown or irrelevant here
                        sizetxt.getText().trim(),
                        sectiontxt.getText().trim(),
                        null,  // Picture remains unchanged
                        unitPrice,
                        productNametxt.getText().trim()
                );

                boolean success = EditProductService.updateProduct(product);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", null, "Product updated successfully.");
                    dialogStage.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Update Failed", null,
                            "No product found with the given Item Code.");
                }

            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", null, "An error occurred while updating the product.");
            }
        });


    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}