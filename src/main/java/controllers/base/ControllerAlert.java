package controllers.base;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class ControllerAlert {
    @FXML
    Button closeButton, nextButton;
    @FXML
    Label alertsCount, alertMessage;
    @FXML
    AnchorPane root;

    public ObjectProperty<String> currentAlert;
    public SimpleListProperty<String> alerts;

    public void bind(){
        alertMessage.textProperty().bind(currentAlert);
        alertsCount.textProperty().bind(alerts.sizeProperty().asString());
    }
    @FXML
    public void nextAlert(){
        String nextAlert = null;
        if(currentAlert.get() != null) alerts.remove(alerts.size() - 1);

        if(alerts.size() > 0){
            nextAlert = alerts.remove(alerts.size() - 1);
        }
        currentAlert.set(nextAlert);
    }
    @FXML
    public void closeAlerts(){
        currentAlert.set(null);
        alerts.clear();
    }
}
