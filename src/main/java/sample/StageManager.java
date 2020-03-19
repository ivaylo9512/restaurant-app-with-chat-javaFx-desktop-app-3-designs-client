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

import java.io.IOException;

public class StageManager extends Application {
    static Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    @Override
    public void start(Stage primaryStage) throws Exception {
        initializeFirstStyle(primaryStage).show();

    }
    static Alert firstAlert;
    static Stage firstStage;
    private Stage initializeFirstStyle(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-first.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-first.css").toString());
        scene.setFill(Color.TRANSPARENT);
        ControllerLoginFirstStyle controller = loader.getController();
        stage.showingProperty().addListener((observable, oldValue, isShowing) -> {
            if(!isShowing){
                controller.resetStage();
            }else{
                controller.setStage();
            }
        });
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);

        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
    }

}
