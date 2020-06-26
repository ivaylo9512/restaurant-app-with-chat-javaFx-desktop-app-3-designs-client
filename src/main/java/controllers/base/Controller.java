package controllers.base;

import helpers.FontIndicator;
import javafx.beans.property.DoubleProperty;
import javafx.stage.Stage;

public abstract class Controller {
    public Stage stage;
    public DoubleProperty fontPxProperty = FontIndicator.fontPx;

    public void setStage(Stage stage){
        this.stage = stage;
    }
}
