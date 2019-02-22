package sample;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoggedFirstStyle extends LoginFirstStyle {
    public static void displayLoggedScene() throws IOException {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        Pane root = new FXMLLoader(LoggedFirstStyle.class.getResource("/logged-first.fxml")).load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(LoggedFirstStyle.class.getResource("/logged-first.css").toString());
        scene.setFill(Color.TRANSPARENT);

        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
        root.setLayoutX((primaryScreenBounds.getMaxX() - root.getWidth())/2);
        root.setLayoutY((primaryScreenBounds.getMaxY() - root.getHeight()) / 2);
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
    }
}
