package application;

import models.Notification;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static application.RestaurantApplication.notificationSound;

public class NotificationManager {
    public ObservableList<Notification> notifications = FXCollections.observableArrayList();

    private NotificationManager(){
    }

    static NotificationManager initialize(){
        return new NotificationManager();
    }

    public void addNotification(String text){
        Notification notification = new Notification(text);

        notifications.add(notification);
        notificationSound.play();
    }

    public void removeNotification(Notification notification){
        notifications.remove(notification);
    }
}
