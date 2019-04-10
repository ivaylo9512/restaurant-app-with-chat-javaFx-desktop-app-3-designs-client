package sample;

import Helpers.ListViews.DishListViewCell;
import Helpers.ListViews.MenuListViewCell;
import Helpers.ListViews.OrderListViewCellSecond;
import Helpers.Services.MessageService;
import Helpers.Services.OrderService;
import Models.Dish;
import Models.Menu;
import Models.Order;
import Models.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static Helpers.ServerRequests.*;
import static Helpers.Services.OrderService.mostRecentOrderDate;

public class ControllerLoggedThirdStyle {
    @FXML public ListView<Order> ordersList;
    @FXML public ListView<Dish> dishesList;
    @FXML public ListView<Menu> menuList,newOrderList;
    @FXML public TextField menuSearch;
    @FXML public AnchorPane profileView, ordersView, chatsView, ordersMenu, chatsMenu, createRoot;
    @FXML public Pane profileImageClip;

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private TreeMap<String, Menu> menuMap = new TreeMap<>();

    private User loggedUser;

    private MessageService messageService;
    private OrderService orderService;
    private AnchorPane currentView, currentMenu;

    @FXML
    public void initialize(){
        ordersList.setCellFactory(orders -> new OrderListViewCellSecond());
        dishesList.setCellFactory(dish -> new DishListViewCell());
        menuList.setCellFactory(menu -> new MenuListViewCell());
        newOrderList.setCellFactory(menu -> new MenuListViewCell());

        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Menu> observableList = FXCollections.observableArrayList();
            searchMenu(newValue.toLowerCase()).forEach((s, menu) -> observableList.add(menu));
            menuList.setItems(observableList);
        });
        Rectangle clip = new Rectangle();
        clip.heightProperty().bind(createRoot.prefHeightProperty());
        clip.widthProperty().bind(createRoot.prefWidthProperty());
        createRoot.setClip(clip);

        Circle profileClip = new Circle(30.8, 30.8, 30.8);
        profileImageClip.setClip(profileClip);
    }

    public void displayUserInfo(){
        loggedUser = loggedUserProperty.getValue();
        loggedUser.getRestaurant().getMenu().forEach(menu -> menuMap.put(menu.getName().toLowerCase(), menu));

        ObservableList<Order> orders = FXCollections.observableArrayList(loggedUser.getOrders().values());
        FXCollections.reverse(orders);

        menuList.setItems(FXCollections.observableArrayList(loggedUser.getRestaurant().getMenu()));

        ordersList.setItems(orders);

        waitForNewOrders();
    }
    public void resetStage(){
        menuMap.clear();
    }
    @FXML
    public void displayOrdersView(){
        displayView(ordersView, ordersMenu);
    }
    @FXML
    public void displayProfileView(){
        displayView(profileView, chatsMenu);
    }
    @FXML
    public void displayChatsView(){
        displayView(chatsView, chatsMenu);
    }

    private void displayView(AnchorPane requestedView, AnchorPane requestedMenu){
        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);
        }

        if (currentMenu != null) {
            currentMenu.setOpacity(0);
            currentMenu.setDisable(true);
        }
        requestedMenu.setOpacity(1);
        requestedMenu.setDisable(false);
        requestedView.setOpacity(1);
        requestedView.setDisable(false);

        currentMenu = requestedMenu;
        currentView = requestedView;
    }
    public void showOrder(int orderId){
        Order order = loggedUser.getOrders().get(orderId);


        order.getDishes().forEach(dish -> dish.setOrderId(order.getId()));
        dishesList.setItems(FXCollections.observableArrayList(order.getDishes()));
    }

    private void waitForNewOrders() {
        orderService = new OrderService();
        orderService.setOnSucceeded(event -> {
            List<Order> newOrders = (List<Order>) orderService.getValue();

            if (newOrders.size() > 0) {
                Order mostRecentNewOrder = newOrders.get(0);
                if (mostRecentNewOrder.getCreated().isAfter(mostRecentNewOrder.getUpdated())) {
                    mostRecentOrderDate = mostRecentNewOrder.getCreated();
                } else {
                    mostRecentOrderDate = mostRecentNewOrder.getUpdated();
                }
            }

            updateNewOrders(newOrders);
            orderService.restart();
        });

        orderService.setOnFailed(event -> serviceFailed(orderService));
    }

    private void updateNewOrders(List<Order> newOrders) {
        newOrders.forEach(order -> {
            int orderId = order.getId();
            Order orderValue = loggedUser.getOrders().get(orderId);

            if (orderValue != null) {
                order.getDishes().forEach(dish -> {
//
//                    if(orderIdLabel.getText().equals(String.valueOf(orderId))) {
//                        Label ready = (Label) dishesList.lookup("#dish" + dish.getId());
//
//                        if (ready != null && ready.getText().equals("X") && dish.getReady()) {
//                            ready.setText("O");
//                            ready.setUserData("ready");
//                        }
//                    }
                });
            } else {
                ordersList.getItems().add(0, order);
                if(order.getUserId() != loggedUser.getId()){
                }
            }
            loggedUser.getOrders().put(orderId, order);
        });
    }

    private void serviceFailed(Service service){

        if(service.getException() != null) {
            if (service.getException().getMessage().equals("Jwt token has expired.")) {
                logOut();
                messageService.reset();
                orderService.reset();
                showLoginStageAlert("Session has expired.");

            } else if(service.getException().getMessage().equals("Socket closed")) {
                service.reset();

            }else{
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(3000), event -> service.restart()));
                timeline.play();
            }
        }
    }
    @FXML
    public void createNewOrder() {
        List<Dish> dishes = new ArrayList<>();
        newOrderList.getItems().forEach(menuItem -> dishes.add(new Dish(menuItem.getName())));

        if (loggedUser.getRole().equals("Server")) {
            if (dishes.size() > 0) {
                try {
                    sendOrder(new Order(dishes));
                    newOrderList.getItems().clear();
                } catch (Exception e) {
                    showLoggedStageAlert(e.getMessage());
                }
            } else {
                showLoggedStageAlert("Order must have at least one dish.");
            }
        } else {
            showLoggedStageAlert("You must be a server to create orders.");
        }
    }
    private SortedMap<String, Menu> searchMenu(String prefix) {
        return menuMap.subMap(prefix, prefix + Character.MAX_VALUE);
    }
    @FXML
    public void addMenuItem(){
        Menu menuItem = menuList.getSelectionModel().getSelectedItem();
        newOrderList.getItems().add(0, menuItem);
    }
    @FXML
    public void removeMenuItem(){
        Menu menuItem = newOrderList.getSelectionModel().getSelectedItem();
        newOrderList.getItems().remove(menuItem);
    }

    @FXML
    public void logOut(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        LoggedSecondStyle.stage.close();
        if(LoginSecondStyle.stage != null) {
            LoginSecondStyle.stage.show();
        }else{
            try{
                LoginSecondStyle.displayLoginScene();
            } catch (Exception e) {
                LoginFirstStyle.stage.show();
                DialogPane dialogPane = LoginFirstStyle.alert.getDialogPane();
                dialogPane.setContentText(e.getMessage());
                LoginFirstStyle.alert.showAndWait();

            }
        }
    }
    private void showLoggedStageAlert(String message) {
        DialogPane dialog = LoggedThirdStyle.alert.getDialogPane();
        dialog.setContentText(message);
        LoggedThirdStyle.alert.showAndWait();
    }
    private void showLoginStageAlert(String message) {
        DialogPane dialog = LoginThirdStyle.alert.getDialogPane();
        dialog.setContentText(message);
        LoginSecondStyle.alert.showAndWait();
    }
}
