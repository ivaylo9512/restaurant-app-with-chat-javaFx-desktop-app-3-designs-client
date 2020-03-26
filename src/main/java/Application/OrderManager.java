package Application;

import Models.Order;

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
    static OrderManager initialize(){
        return new OrderManager();
    }
}
