package Application;

import Models.Order;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Service;
import javafx.util.Duration;

import java.util.List;

public class OrderManager {
    private static OrderService orderService;

    private OrderManager(){
        orderService = new OrderService();
        orderService.setOnSucceeded(event -> {
            List<Order> newOrders = (List<Order>) orderService.getValue();

            updateNewOrders(newOrders);
            orderService.restart();
        });

        orderService.setOnFailed(event -> serviceFailed(orderService));
    }
    private void serviceFailed(Service service){
        if(service.getException() != null && service.isRunning()) {
            if (service.getException().getMessage().equals("Jwt token has expired.")) {
                logout();
                showLoginStageAlert("Session has expired.");

            } else if(service.getException().getMessage().equals("Socket closed")) {
                service.reset();

            }else{
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(3000), event -> service.restart()));
                timeline.play();
            }
        }
    }
    static OrderManager initialize(){
        return new OrderManager();
    }
}
