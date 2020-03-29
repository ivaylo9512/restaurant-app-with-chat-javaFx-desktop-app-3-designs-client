package Application;

import Helpers.ServiceErrorHandler;
import Models.Dish;
import Models.Menu;
import Models.Order;
import Models.Restaurant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.TreeMap;

import static Application.OrderService.mostRecentOrderDate;
import static Application.RestaurantApplication.stageManager;
import static Application.LoginManager.userId;

public class OrderManager {
    private static OrderService orderService;
    public Restaurant userRestaurant;
    public TreeMap<String, Menu> userMenu = new TreeMap<>();
    public ObservableList<Order> orders = FXCollections.observableArrayList();

    private OrderManager() {
        orderService = new OrderService();

        orderService.setOnFailed(new ServiceErrorHandler());
        orderService.setOnSucceeded(event -> {
            List<Order> newOrders = orderService.getValue();

            updateOrders(newOrders);
            orderService.restart();
        });
    }

    private void updateOrders(List<Order> newOrders) {
//        newOrders.forEach(newOrder -> {
//            if(newOrder.getUpdated().get().isAfter(mostRecentOrderDate)) {
//                mostRecentOrderDate = newOrder.getUpdated().get();
//            }else if(newOrder.getCreated().get().isAfter(mostRecentOrderDate)){
//                mostRecentOrderDate = newOrder.getCreated().get();
//            }
//
//            int orderId = newOrder.getId().get();
//            int index = orders.indexOf(newOrder);
//            if(index == -1){
//                stageManager.currentController.createOrder(newOrder);
//
//                if(newOrder.getUserId() != userId.getValue()){
//                    addNotification("New order created " + orderId);
//                }
//            }else{
//                Order oldOrder = orders.get(index);
//                ObservableList<Dish> oldDishes = oldOrder.getDishes();
//                ObservableList<Dish> newDishes = newOrder.getDishes();
//
//                for (int i = 0; i < newDishes.size(); i++) {
//                    Dish newDish = newDishes.get(i);
//                    Dish oldDish = oldDishes.get(i);
//                    if(oldDish.getReady() && newDish.getReady()){
//                        stageManager.currentController.addNotification(newDish.getName() + " from order " + orderId + " is ready.");
//                    }
//                }
//                orders.remove(index);
//                orders.add(0, newOrder);
//
//                if (newOrder.isReady()) {
//                    stageManager.currentController.addNotification("Order " + orderId + " is ready.");
//                }
//
//            }
//        });
    }

    static OrderManager initialize(){
        return new OrderManager();
    }

    void setRestaurant(Restaurant restaurant) {
        userRestaurant = restaurant;
        restaurant.getMenu().forEach(menu ->
                userMenu.put(menu.getName().toLowerCase(), menu));
        orders.setAll(restaurant.getOrders());
    }

    void resetRestaurant(){
        userRestaurant = null;
        userMenu.clear();
        orders.clear();
    }
}
