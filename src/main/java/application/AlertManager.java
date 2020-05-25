package application;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayDeque;
import java.util.Deque;

public class AlertManager {
    public Deque<String> loginAlerts = new ArrayDeque<>();
    public Deque<String> loggedAlerts = new ArrayDeque<>();
    public ObjectProperty<String> currentLoginAlert = new SimpleObjectProperty<>();
    public ObjectProperty<String> currentLoggedAlert = new SimpleObjectProperty<>();

    private AlertManager() {
    }

    static AlertManager initialize(){
        return new AlertManager();
    }

    public void addLoginAlert(String alert){
        loginAlerts.push(alert);
    }
    public void addLoggedAlert(String alert){
        loginAlerts.push(alert);
    }
    public void nextLoginAlert(){
        currentLoginAlert.set(loginAlerts.peekLast());
    }
    public void nextLoggedAlert(){
        currentLoggedAlert.set(loggedAlerts.peekLast());

    }
    public void resetAlerts(){
        loggedAlerts = new ArrayDeque<>();
        loginAlerts = new ArrayDeque<>();
        currentLoginAlert.set(null);
        currentLoggedAlert.set(null);
    }
}
