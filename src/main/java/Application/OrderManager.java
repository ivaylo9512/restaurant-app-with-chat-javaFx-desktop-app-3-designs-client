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
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
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

    public RequestService<Order> sendOrder = new RequestService<>(Order.class, null, RequestEnum.sendOrder);

    private OrderManager() {
        sendOrder.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onSendOrderSuccess);
        sendOrder.setOnFailed(event -> sendOrder.reset());
    }

    private EventHandler<WorkerStateEvent> onSendOrderSuccess = event -> {
        orders.add(0, (Order)event.getSource().getValue());
        newOrderList.clear();
        sendOrder.reset();
    };

    static OrderManager initialize(){
        return new OrderManager();
    }

    void setRestaurant(Restaurant restaurant) {
        sendOrder.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onSendOrderSuccess);

        userRestaurant = restaurant;
        restaurant.getMenu().forEach(menu ->
                userMenu.put(menu.getName().toLowerCase(), menu));
        orders.setAll(restaurant.getOrders());

        Order order = orders.get(0);
        mostRecentOrderDate = order.getCreated().get().isAfter(order.getUpdated().get())
                ? order.getCreated().get() : order.getUpdated().get();
    }

    void resetRestaurant(){
        sendOrder.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onSendOrderSuccess);

        userRestaurant = null;
        mostRecentOrderDate = null;
        newOrder = null;

        newOrderList.clear();
        userMenu.clear();
        orders.clear();
        notificationManager.notifications.clear();

        if(sendOrder.isRunning()) sendOrder.cancel();
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

    public void addOrder(Order order){
        notificationManager.addNotification("New order created " + order.getId().get());
        orders.add(0, order);
    }

    public void updateDish(Dish dish){
        int orderId = dish.getOrderId();
        int orderIndex = orders.indexOf(new Order(orderId));
        if(orderIndex >= 0) {
            Order order = orders.get(orderIndex);
            order.setUpdated(dish.getUpdated());

            if (dish.isOrderReady()) {
                order.setReady(true);
                notificationManager.addNotification("Order " + orderId + " is ready.");
            }

            orders.remove(orderIndex);
            orders.add(0, order);

            List<Dish> dishes = order.getDishes();
            int dishIndex = dishes.indexOf(dish);
            dishes.set(dishIndex, dish);

            if(dish.getUpdatedById() != loginManager.userId.get()){
                notificationManager.addNotification(dish.getName() + " from order " + orderId + " is ready.");
            }
        }
    }

    private JavaType type = mapper.constructType(Dish.class);
    public void updateDishState(Dish dish){
        HttpRequestBase request = ServerRequests.updateDishState(dish.getOrderId(), dish.getId());
        RequestTask<Dish> task = new RequestTask<>(type, request);
        tasks.execute(task);

        task.setOnSucceeded(event -> {
            Dish newDish = task.getValue();
            updateDish(newDish);
        });

        task.setOnFailed(event -> {
            int orderIndex = orders.indexOf(new Order(dish.getOrderId()));
            if(orderIndex >= 0) {

                Order order = orders.get(orderIndex);

                List<Dish> dishes = order.getDishes();
                int dishIndex = dishes.indexOf(dish);
                dish.setLoading(false);

                dishes.set(dishIndex, dish);
            }
        });
    }
}
