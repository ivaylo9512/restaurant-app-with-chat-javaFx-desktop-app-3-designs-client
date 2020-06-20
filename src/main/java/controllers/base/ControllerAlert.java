package controllers.base;

import application.RestaurantApplication;
import helpers.FontIndicator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.util.Duration;

public class ControllerAlert extends Controller{
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

    public FontIndicator fontIndicator = RestaurantApplication.fontIndicator;

    public ObjectProperty<String> currentAlert;
    public SimpleListProperty<String> alerts;

    private Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    public void bind(){
        content.widthProperty().addListener((observable, oldValue, newValue) -> {
            root.setPrefWidth(newValue.doubleValue());
            stage.sizeToScene();
            stage.setX((primaryScreenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY(0);
        });
        alertMessage.textProperty().bind(currentAlert);
        alertsCount.textProperty().bind(alerts.sizeProperty().asString());
        root.setStyle("-fx-font-size: " + fontIndicator.getFontPx() + ";");

        root.paddingProperty().bind(Bindings.createObjectBinding(()->
                new Insets(fontIndicator.getFontPx() * 1.5),fontIndicator.getFontPxProperty()));
        fontIndicator.getFontPxProperty().addListener((observable, oldValue, newValue) -> {
            root.setStyle("-fx-font-size: " + newValue.doubleValue() + ";");
        });

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
                content.getStyleClass().add("translate-content");
                stage.setUserData("inactive");
                stage.close();
            }
        });
    }
    public void fadeInAlert(){
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(1000), content);
        translateTransition.setToY(0);
        translateTransition.play();
        Timeline removeStyle = new Timeline(new KeyFrame(new Duration(1000), event -> {
            content.getStyleClass().remove("translate-content");
        }));
        removeStyle.play();
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

    public FontIndicator getFontIndicator() {
        return fontIndicator;
    }
}
