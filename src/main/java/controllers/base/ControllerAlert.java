package controllers.base;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ControllerAlert {
    @FXML
    Button closeButton, nextButton;
    @FXML
    Label alertsCount, alertMessage;
    @FXML
    AnchorPane root;
    @FXML
    AnchorPane content;
    @FXML
    StackPane alertIcon;

    public Stage stage;
    public ObjectProperty<String> currentAlert;
    public SimpleListProperty<String> alerts;

    private Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    public void initialize(){
        SVGPath path1 = new SVGPath();
        path1.setContent("M244.709,389.496c18.736,0,34.332-14.355,35.91-33.026l24.359-290.927c1.418-16.873-4.303-33.553-15.756-46.011C277.783,7.09,261.629,0,244.709,0s-33.074,7.09-44.514,19.532C188.74,31.99,183.022,48.67,184.44,65.543l24.359,290.927C210.377,375.141,225.973,389.496,244.709,389.496z");

        SVGPath path2 = new SVGPath();
        path2.setContent("M244.709,410.908c-21.684,0-39.256,17.571-39.256,39.256c0,21.683,17.572,39.254,39.256,39.254s39.256-17.571,39.256-39.254C283.965,428.479,266.393,410.908,244.709,410.908z");
        Shape svg = Shape.union(path1, path2);

        Region region = new Region();
        region.setShape(svg);
        alertIcon.getChildren().add(region);
    }
    public void bind(){
        content.widthProperty().addListener((observable, oldValue, newValue) -> {
            root.setPrefWidth(newValue.doubleValue());
            stage.sizeToScene();
            stage.setX((primaryScreenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((primaryScreenBounds.getHeight() - stage.getHeight()) / 2);

        });
        alertMessage.textProperty().bind(currentAlert);
        alertsCount.textProperty().bind(alerts.sizeProperty().asString());
    }
    @FXML
    public void nextAlert(){
        String nextAlert = null;

        if(alerts.size() > 1){
            if(currentAlert.get() != null) alerts.remove(0);

            nextAlert = alerts.get(0);
        }
        currentAlert.set(nextAlert);
    }
    @FXML
    public void closeAlerts(){
        currentAlert.set(null);
        alerts.clear();
    }
}
