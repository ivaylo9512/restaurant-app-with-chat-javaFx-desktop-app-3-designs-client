package Application;

import Helpers.RequestEnum;
import Helpers.RequestService;
import Helpers.RequestTask;
import Models.Dish;
import Models.Menu;
import Models.Order;
import Models.Restaurant;
import com.fasterxml.jackson.databind.JavaType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.http.client.methods.HttpRequestBase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static Application.RestaurantApplication.*;
import static Application.ServerRequests.mapper;
import static Application.ServerRequests.tasks;

public class OrderManager {
    public Restaurant userRestaurant;
    public TreeMap<String, Menu> userMenu = new TreeMap<>();
    public ObservableList<Order> orders = FXCollections.observableArrayList();
    public LocalDateTime mostRecentOrderDate;
    public ObservableList<Menu> newOrderList = FXCollections.observableArrayList();
    public Order newOrder;

    private RequestService<List<Order>> orderService = new RequestService<>(Order.class, List.class, RequestEnum.waitOrders);
    private RequestService<Order> sendOrder = new RequestService<>(Order.class, null, RequestEnum.sendOrder);

    private OrderManager() {
        orderService.setOnSucceeded(event -> {
            List<Order> newOrders = orderService.getValue();
            if(newOrders != null) {
                updateOrders(newOrders);
                orderService.restart();
            }
        });

        sendOrder.setOnSucceeded(event -> {
            orders.add(0, (Order)event.getSource().getValue());
            newOrderList.clear();
            sendOrder.reset();
        });
        sendOrder.setOnFailed(event -> sendOrder.reset());
    }

    private void updateOrders(List<Order> newOrders) {
        newOrders.forEach(newOrder -> {
            if(newOrder.getUpdated().get().isAfter(mostRecentOrderDate)) {
                mostRecentOrderDate = newOrder.getUpdated().get();
            }else if(newOrder.getCreated().get().isAfter(mostRecentOrderDate)){
                mostRecentOrderDate = newOrder.getCreated().get();
            }

            int orderId = newOrder.getId().get();
            int index = orders.indexOf(newOrder);
            if(index == -1){
                notificationManager.addNotification("New order created " + orderId);
                orders.add(0, newOrder);
            }else{
                Order oldOrder = orders.get(index);
                ObservableList<Dish> oldDishes = oldOrder.getDishes();
                ObservableList<Dish> newDishes = newOrder.getDishes();

                for (int i = 0; i < newDishes.size(); i++) {
                    Dish newDish = newDishes.get(i);
                    Dish oldDish = oldDishes.get(i);
                    if(!oldDish.isReady() && newDish.isReady()){
                        notificationManager.addNotification(newDish.getName() + " from order " + orderId + " is ready.");
                        oldDishes.set(i, newDish);
                    }
                }

                if (newOrder.isReady()) {
                    oldOrder.setReady(true);
                    notificationManager.addNotification("Order " + orderId + " is ready.");
                }

                orders.remove(index);
                oldOrder.setUpdated(newOrder.getUpdated().get());
                orders.add(0, oldOrder);
            }
        });
    }

    static OrderManager initialize(){
        return new OrderManager();
    }

    void setRestaurant(Restaurant restaurant) {
        userRestaurant = restaurant;
        restaurant.getMenu().forEach(menu ->
                userMenu.put(menu.getName().toLowerCase(), menu));
        orders.setAll(restaurant.getOrders());

        Order order = orders.get(0);
        mostRecentOrderDate = order.getCreated().get().isAfter(order.getUpdated().get())
                ? order.getCreated().get() : order.getUpdated().get();

        orderService.start();
    }

    void resetRestaurant(){
        userRestaurant = null;
        mostRecentOrderDate = null;
        newOrder = null;
        userMenu.clear();
        orders.clear();

        if(orderService.isRunning())orderService.cancel();
        if(sendOrder.isRunning()) sendOrder.cancel();

        orderService.reset();
        sendOrder.reset();
    }

    public void sendOrder(){
        if (loginManager.loggedUser.getRole().get().equals("Server")) {
            List<Dish> dishes = newOrderList.stream().map(menu -> new Dish(menu.getName())).collect(Collectors.toList());
            if(dishes.size() > 0) {

                newOrder = new Order(dishes);
                sendOrder.start();

            }else{
                stageManager.showAlert("Order must have at least one dish.");
            }
        } else {
            stageManager.showAlert("You must be a server to create orders.");
        }
    }

    private JavaType type = mapper.constructType(Dish.class);
    public void updateDishState(Dish dish){
        HttpRequestBase request = ServerRequests.updateDishState(dish.getOrderId(), dish.getId());
        RequestTask<Dish> task = new RequestTask<>(type, request);
        tasks.execute(task);
        task.setOnSucceeded(event -> {
            Dish newDish = task.getValue();

            int orderIndex = orders.indexOf(new Order(newDish.getOrderId()));
            Order order = orders.get(orderIndex);
            order.setUpdated(newDish.getUpdated());

            orders.remove(orderIndex);
            orders.add(0, order);

            List<Dish> dishes = order.getDishes();
            int dishIndex = dishes.indexOf(dish);

            dishes.set(dishIndex, newDish);
        });
    }
}
