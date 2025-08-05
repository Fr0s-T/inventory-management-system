package ViewsControllers;

import Services.DeletedLogsService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * Author: @Frost
 *
 */


public class DeletedLogs {
    @FXML private TextField Pathtxt;
    @FXML private Button SaveButton;
    @FXML private Button CancelButton;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void initialize(){

        CancelButton.setOnAction(e -> dialogStage.close());
        SaveButton.setOnAction(e -> {
            DeletedLogsService.saveDeletedLogsToAFileAsync(Pathtxt.getText());
            dialogStage.close();
        });


    }

}
