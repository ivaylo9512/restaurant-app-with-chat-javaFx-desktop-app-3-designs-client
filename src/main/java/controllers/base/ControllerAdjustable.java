package controllers.base;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public abstract class ControllerAdjustable implements Controller{
    @FXML
    public AnchorPane root;
    public Stage stage;

    protected static Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    abstract void resetStage();

    void adjustStage(double height, double width){
        root.setPrefWidth(root.getMinWidth());
        root.setPrefHeight(root.getMinHeight());

        stage.setY((primaryScreenBounds.getHeight() - height) / 2);
        stage.setX((primaryScreenBounds.getWidth() - width) / 2);

        stage.sizeToScene();
    }

    public void setStage(Stage stage){
        this.stage = stage;
    }
}
