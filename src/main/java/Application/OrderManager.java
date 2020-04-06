package Application;

import Helpers.RequestEnum;
import Helpers.RequestService;
import Helpers.RequestTask;
import Helpers.ServiceErrorHandler;
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

import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.stageManager;
import static Application.ServerRequests.mapper;
import static Application.ServerRequests.tasks;

public class OrderManager {
    private static OrderService orderService;
    public Restaurant userRestaurant;
    public TreeMap<String, Menu> userMenu = new TreeMap<>();
    public ObservableList<Order> orders = FXCollections.observableArrayList();
    public LocalDateTime mostRecentOrderDate;
    public RequestService<Order> sendOrder = new RequestService<>(Order.class, null, RequestEnum.sendOrder);
    public ObservableList<Menu> newOrderList = FXCollections.observableArrayList();
    public Order newOrder;

    private OrderManager() {
        orderService = new OrderService();

        orderService.setOnFailed(new ServiceErrorHandler());
        orderService.setOnSucceeded(event -> {
            List<Order> newOrders = orderService.getValue();

            updateOrders(newOrders);
            orderService.restart();
        });

        sendOrder.setOnSucceeded(event -> {
            newOrderList.clear();
            sendOrder.reset();
        });
        sendOrder.setOnFailed(event -> sendOrder.reset());
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
        mostRecentOrderDate = null;
        newOrder = null;
        userMenu.clear();
        orders.clear();
    }

    public void sendOrder(){
        if (loginManager.loggedUser.getRole().get().equals("Chef")) {
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
            Dish newDish = (Dish) event.getSource().getValue();

            int orderIndex = orders.indexOf(new Order(newDish.getOrderId()));
            Order order = orders.get(orderIndex);
            order.setUpdated(newDish.getUpdated());
            orders.remove(orderIndex);
            orders.add(0, order);

            List<Dish> dishes = order.getDishes();
            int dishIndex = dishes.indexOf(dish);
            Dish oldDish = dishes.get(dishIndex);
            oldDish.setLoading(false);
            oldDish.setReady(true);

            dishes.set(dishIndex, oldDish);
        });
    }
}
