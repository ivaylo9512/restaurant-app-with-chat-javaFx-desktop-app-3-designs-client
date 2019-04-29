package sample;

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

import java.io.IOException;

public class LoginThirdStyle extends LoginFirstStyle {
    static Alert alert;
    static Stage stage;


    public static void displayLoginScene() throws IOException {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        FXMLLoader loader = new FXMLLoader(LoggedSecondStyle.class.getResource("/FXML/login-third.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(LoggedSecondStyle.class.getResource("/css/login-third.css").toString());
        scene.setFill(Color.TRANSPARENT);

        stage = new Stage();
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();

        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());

        Pane loginPane = (Pane) root.getChildren().get(0);
        loginPane.setLayoutY((primaryScreenBounds.getHeight() - loginPane.getHeight()) / 2);
        loginPane.setLayoutX((primaryScreenBounds.getWidth() - loginPane.getWidth()) / 2 - 17.2);

        alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.initStyle(StageStyle.TRANSPARENT);
        DialogPane dialog = alert.getDialogPane();
        dialog.setGraphic(null);
        dialog.getStyleClass().add("alert-box");
    }

}
