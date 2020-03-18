package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginFirstStyle extends Application {
    static Alert alert;
    static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-first.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-first.css").toString());
        scene.setFill(Color.TRANSPARENT);

        stage = primaryStage;
        ControllerLoginFirstStyle controller = loader.getController();
        stage.showingProperty().addListener((observable, oldValue, isShowing) -> {
            if(!isShowing) {
                controller.resetStage();
            }
        });

        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();

        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());

        Pane loginPane = (Pane) root.getChildren().get(1);
        loginPane.setLayoutY((primaryScreenBounds.getHeight() - loginPane.getHeight()) / 2);
        loginPane.setLayoutX((primaryScreenBounds.getWidth() - loginPane.getWidth()) / 2);

        alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(primaryStage);
        alert.initStyle(StageStyle.TRANSPARENT);
        DialogPane dialog = alert.getDialogPane();
        dialog.setGraphic(null);
        dialog.getStyleClass().add("alert-box");
    }

}
