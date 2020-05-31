package controllers.base;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ControllerAlert {
    @FXML
    Button closeButton, nextButton;
    @FXML
    Label alertsCount, alertMessage;
    @FXML
    AnchorPane root;
    @FXML
    GridPane content;

    public Stage stage;
    public ObjectProperty<String> currentAlert;
    public SimpleListProperty<String> alerts;

    public void bind(){

        content.widthProperty().addListener((observable, oldValue, newValue) -> {
            root.setPrefWidth(newValue.doubleValue());
            stage.sizeToScene();
        });
        alertMessage.textProperty().bind(currentAlert);
        alertsCount.textProperty().bind(alerts.sizeProperty().asString());
    }
    @FXML
    public void nextAlert(){
        String nextAlert = null;

        if(alerts.size() > 0){
            if(currentAlert.get() != null) alerts.remove(0);

            nextAlert = alerts.get(0);
            System.out.println(nextAlert);
        }
        currentAlert.set(nextAlert);
    }
    @FXML
    public void closeAlerts(){
        currentAlert.set(null);
        alerts.clear();
    }
}
