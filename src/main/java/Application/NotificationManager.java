package Application;

import Models.Notification;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static Application.RestaurantApplication.notificationSound;

public class NotificationManager {
    public ObservableList<Notification> notifications = FXCollections.observableArrayList();

    private NotificationManager(){
        addNotification("add");
        addNotification("hey");
        addNotification("add");
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
