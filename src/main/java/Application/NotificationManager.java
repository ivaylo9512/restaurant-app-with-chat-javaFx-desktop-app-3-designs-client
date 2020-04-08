package Application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotificationManager {
    ObservableList<String> notifications = FXCollections.observableArrayList();

    private NotificationManager(){

    }

    NotificationManager initialize(){
        return new NotificationManager();
    }

    public void addNotification(String notification){
        notifications.add(notification);
    }

    public void removeNotification(String notification){
        notifications.remove(notification);
    }
}
