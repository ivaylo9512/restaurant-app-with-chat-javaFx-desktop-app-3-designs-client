package sample;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoggedThirdStyle extends LoginFirstStyle{

    static Stage stage;
    static Alert alert;

    static void displayLoggedScene() throws IOException {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        FXMLLoader loader = new FXMLLoader(LoggedThirdStyle.class.getResource("/FXML/logged-third.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(LoggedThirdStyle.class.getResource("/css/logged-third.css").toString());

        stage = new Stage();
        ControllerLoggedThirdStyle controller = loader.getController();
        stage.showingProperty().addListener((observable, oldValue, isShowing) -> {
            if(isShowing) {
                try {
                    controller.displayUserInfo();
                } catch (Exception e) {
                    controller.resetStage();
                    stage.close();
                    LoggedFirstStyle.stage.show();

                    DialogPane dialog = LoginFirstStyle.alert.getDialogPane();
                    dialog.setContentText(e.getMessage());
                    LoginFirstStyle.alert.showAndWait();
                }
            }else{
                controller.resetStage();
            }
        });

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());

        AnchorPane contentRoot = (AnchorPane) root.getChildren().get(0);
        contentRoot.setLayoutY((primaryScreenBounds.getHeight() - contentRoot.getHeight()) / 2);
        contentRoot.setLayoutX((primaryScreenBounds.getWidth() - contentRoot.getWidth()) / 2);

        alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.initStyle(StageStyle.TRANSPARENT);
        DialogPane dialog = alert.getDialogPane();
        dialog.setGraphic(null);
        dialog.getStyleClass().add("alert-box");
    }
}
