package application;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayDeque;
import java.util.Deque;

public class AlertManager {
    private Deque<String> loginAlerts = new ArrayDeque<>();
    private Deque<String> loggedAlerts = new ArrayDeque<>();
    ObjectProperty<String> currentLoginAlert = new SimpleObjectProperty<>();
    ObjectProperty<String> currentLoggedAlert = new SimpleObjectProperty<>();

    private AlertManager() {
    }

    static AlertManager initialize(){
        return new AlertManager();
    }

    public void addLoginAlert(String alert){
        if(currentLoginAlert.get() != null) {
            currentLoginAlert.set(alert);
        }else {
            loginAlerts.push(alert);
        }

    }
    public void addLoggedAlert(String alert){
        if(currentLoggedAlert.get() != null) {
            currentLoginAlert.set(alert);
        }else{
            loginAlerts.push(alert);
        }
    }
    public void nextLoginAlert(){
        currentLoginAlert.set(loginAlerts.peekLast());
    }
    public void nextLoggedAlert(){
        currentLoggedAlert.set(loggedAlerts.peekLast());

    }
    public void resetLoginAlerts(){
        loginAlerts = new ArrayDeque<>();
        currentLoginAlert.set(null);
    }
    public void resetLoggedAlerts(){
        loggedAlerts = new ArrayDeque<>();
        currentLoggedAlert.set(null);
    }
}
