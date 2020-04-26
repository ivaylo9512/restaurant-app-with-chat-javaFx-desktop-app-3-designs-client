package sample.base;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public interface Controller {
    Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    void resetStage();

    void setStage() throws Exception;
}
