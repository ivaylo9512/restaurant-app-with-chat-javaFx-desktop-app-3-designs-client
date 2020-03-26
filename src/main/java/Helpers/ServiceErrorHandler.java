package Helpers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import static Application.RestaurantApplication.loginManager;

public class ServiceErrorHandler implements EventHandler<WorkerStateEvent> {
    @Override
    public void handle(WorkerStateEvent event) {
        Service service = (Service) event.getSource();
        Throwable exception = service.getException();
        if(exception != null) {
            if (exception.getMessage().equals("Jwt token has expired.")) {
                loginManager.logout();
                showLoginStageAlert("Session has expired.");
            } else if(service.getException().getMessage().equals("Socket closed")) {
                service.restart();
            }else{
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(3000), eventT -> service.restart()));
                timeline.play();
            }
        }
    }
}
