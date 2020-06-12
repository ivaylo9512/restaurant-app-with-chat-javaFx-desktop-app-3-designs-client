package application;

import controllers.base.ControllerLogin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

import static application.RestaurantApplication.stageManager;

public class AlertManager {

    SimpleListProperty<String> loginAlerts = new SimpleListProperty<>(FXCollections.observableArrayList());
    SimpleListProperty<String> loggedAlerts = new SimpleListProperty<>(FXCollections.observableArrayList());

    ObjectProperty<String> currentLoginAlert = new SimpleObjectProperty<>();
    ObjectProperty<String> currentLoggedAlert = new SimpleObjectProperty<>();

    AlertManager() {
    }

    public void addAlert(String alert){
        if(stageManager.currentController instanceof ControllerLogin){
            addLoginAlert(alert);
        }else{
            addLoggedAlert(alert);
        }
    }
    public void addLoginAlert(String alert){
        if(currentLoginAlert.get() == null) {
            currentLoginAlert.set(alert);
        }
        loginAlerts.add(alert);

    }
    public void addLoggedAlert(String alert){
        if(currentLoggedAlert.get() == null) {
            currentLoggedAlert.set(alert);
        }
        loggedAlerts.add(alert);
    }
    public void resetLoginAlerts(){
        loginAlerts.clear();
        currentLoginAlert.set(null);
    }
    public void resetLoggedAlerts(){
        loggedAlerts.clear();
        currentLoggedAlert.set(null);
    }
}
