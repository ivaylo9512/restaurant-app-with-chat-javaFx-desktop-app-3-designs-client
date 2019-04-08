package sample;

import Helpers.ListViews.DishListViewCell;
import Helpers.ListViews.OrderListViewCellSecond;
import Helpers.ServerRequests;
import Helpers.Services.LoginService;
import Helpers.Services.MessageService;
import Helpers.Services.OrderService;
import Helpers.Services.RegisterService;
import Models.Dish;
import Models.Order;
import Models.User;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.ConnectException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static Helpers.ServerRequests.httpClientLongPolling;
import static Helpers.ServerRequests.loggedUserProperty;
import static Helpers.Services.OrderService.mostRecentOrderDate;

public class ControllerLoggedThirdStyle {
    @FXML public ListView<Integer> ordersList;
    @FXML public ListView<Dish> dishesList;
    @FXML Label dishesCountLabel, orderIdLabel, updatedDateLabel, updatedTimeLabel,
            createdDateLabel, createdTimeLabel;
    @FXML AnchorPane orderInfo;

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private User loggedUser;

    private MessageService messageService;
    private OrderService orderService;

    @FXML
    public void initialize(){
        ordersList.setCellFactory(orders -> new OrderListViewCellSecond());
        dishesList.setCellFactory(dish -> new DishListViewCell());
    }

    public void displayUserInfo(){
        loggedUser = loggedUserProperty.getValue();
        ObservableList<Integer> ordersId = FXCollections.observableArrayList(loggedUser.getOrders().values().stream().map(Order::getId).collect(Collectors.toList()));
        ordersList.setItems(ordersId);

        waitForNewOrders();
    }
    public void resetStage(){

    }

    public void showOrder(int orderId){
        Order order = loggedUser.getOrders().get(orderId);

        orderIdLabel.setText(String.valueOf(order.getId()));
        dishesCountLabel.setText("Dishes " + order.getDishes().size());

        createdDateLabel.setText(dateFormatter.format(order.getCreated()));
        createdTimeLabel.setText(timeFormatter.format(order.getCreated()));
        updatedDateLabel.setText(dateFormatter.format(order.getUpdated()));
        updatedTimeLabel.setText(timeFormatter.format(order.getUpdated()));


        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), orderInfo);
        fadeIn.setFromValue(0.36);
        fadeIn.setToValue(1);
        fadeIn.play();

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

            orderService.restart();
        });

        orderService.setOnFailed(event -> serviceFailed(orderService));
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
        LoggedSecondStyle.alert.showAndWait();
    }
    private void showLoginStageAlert(String message) {
        DialogPane dialog = LoginThirdStyle.alert.getDialogPane();
        dialog.setContentText(message);
        LoginSecondStyle.alert.showAndWait();
    }
}
