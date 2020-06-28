package controllers.base;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public abstract class ControllerAdjustable extends Controller{
    @FXML
    public Pane root;

    public abstract void resetStage();

    public void adjustStage(double height, double width) throws Exception{
        if(root.getMinHeight() > 0 ){
            height = root.getMinHeight();
            width = root.getMinWidth();

            stage.setMinHeight(height);
            stage.setMinWidth(width);
        }
        stage.setHeight(height);
        stage.setWidth(width);

        stage.setY((primaryScreenBounds.getHeight() - height) / 2);
        stage.setX((primaryScreenBounds.getWidth() - width) / 2);
    }

    @FXML
    public void minimize(){
        stage.setIconified(true);
    }

    @FXML
    public void close(){
        stage.close();
    }
}
