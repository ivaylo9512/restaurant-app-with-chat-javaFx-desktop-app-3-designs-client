package Application;

import Helpers.ServiceErrorHandler;
import Models.Order;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.util.List;

public class OrderManager {
    private static OrderService orderService;

    private OrderManager() {
        orderService = new OrderService();
        orderService.setOnSucceeded(event -> {
            List<Order> newOrders = orderService.getValue();

            updateNewOrders(newOrders);
            orderService.restart();
        });

        orderService.setOnFailed(new ServiceErrorHandler());

    }

    static OrderManager initialize(){
        return new OrderManager();
    }
}
