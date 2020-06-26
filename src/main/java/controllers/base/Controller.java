package controllers.base;

import helpers.FontIndicator;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public abstract class Controller {
    protected static DoubleProperty fontPxProperty = FontIndicator.fontPx;
    protected static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    protected static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    protected static DateTimeFormatter dateFormatterSimple = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    protected static Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    public Stage stage;
    public void setStage(Stage stage){
        this.stage = stage;
    }
}
