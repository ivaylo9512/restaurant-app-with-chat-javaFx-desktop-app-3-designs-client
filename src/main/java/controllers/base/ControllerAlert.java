package controllers.base;

import javafx.animation.TranslateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

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
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(Font.getDefault().getSize());
        rectangle.setHeight(Font.getDefault().getSize() * 2);
        Circle circle = new Circle(Font.getDefault().getSize() / 2);
        VBox vBox = new VBox(rectangle, circle);

        alertIcon.getChildren().add(vBox);
    }
    public void bind(){
        content.widthProperty().addListener((observable, oldValue, newValue) -> {
            root.setPrefWidth(newValue.doubleValue());
            stage.sizeToScene();
            stage.setX((primaryScreenBounds.getWidth() - stage.getWidth()) / 2);
        });
        alertMessage.textProperty().bind(currentAlert);
        alertsCount.textProperty().bind(alerts.sizeProperty().asString());

        currentAlert.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                stage.setUserData("active");
                if(!stage.isShowing() && stage.getOwner().isShowing()){
                    stage.show();
                    stage.setY(0);
                    stage.setX((primaryScreenBounds.getWidth() - stage.getWidth()) / 2);
                    fadeInAlert();
                }
            }else{
                stage.setUserData("inactive");
                stage.close();
            }
        });
    }
    public void fadeInAlert(){
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(1000), content);
        translateTransition.setToY(0);
        translateTransition.play();
    }
    @FXML
    public void nextAlert(){
        String nextAlert = null;

        if(currentAlert.get() != null && alerts.size() > 0) alerts.remove(0);
        if(alerts.size() > 0) nextAlert = alerts.get(0);

        currentAlert.set(nextAlert);
    }
    @FXML
    public void closeAlerts(){
        currentAlert.set(null);
        alerts.clear();
    }
}
