package sample;

import Animations.ResizeRoot;
import Helpers.ServerRequests;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoggedFirstStyle extends LoginFirstStyle {

    static Stage stage;

    public static void displayLoggedScene() throws IOException {

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        Pane root = new FXMLLoader(LoggedFirstStyle.class.getResource("/FXML/logged-first.fxml")).load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(LoggedFirstStyle.class.getResource("/css/logged-first.css").toString());
        scene.setFill(Color.TRANSPARENT);

        stage = new Stage();
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

        ResizeRoot.addListeners(contentRoot);
    }
}
