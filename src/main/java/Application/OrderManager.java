package Application;

import Helpers.ServiceErrorHandler;
import Models.Menu;
import Models.Order;
import Models.Restaurant;

import java.util.List;
import java.util.TreeMap;

public class OrderManager {
    private static OrderService orderService;
    public Restaurant userRestaurant;
    public TreeMap<String, Menu> userMenu = new TreeMap<>();

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

    void setRestaurant(Restaurant restaurant) {
        userRestaurant = restaurant;
        restaurant.getMenu().forEach(menu ->
                userMenu.put(menu.getName().toLowerCase(), menu));
    }
}
