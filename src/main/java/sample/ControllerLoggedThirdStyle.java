package sample;

import Helpers.ListViews.DishListViewCell;
import Helpers.ListViews.OrderListViewCellSecond;
import Helpers.ServerRequests;
import Helpers.Services.LoginService;
import Helpers.Services.RegisterService;
import Models.Dish;
import Models.Order;
import Models.User;
import javafx.animation.FadeTransition;
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

import java.net.ConnectException;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import static Helpers.ServerRequests.loggedUserProperty;

public class ControllerLoggedThirdStyle {
    @FXML public ListView<Integer> ordersList;
    @FXML public ListView<Dish> dishesList;
    @FXML Label dishesCountLabel, orderIdLabel, updatedDateLabel, updatedTimeLabel,
            createdDateLabel, createdTimeLabel;
    @FXML AnchorPane orderInfo;

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private User loggedUser;
    @FXML
    public void initialize(){
        ordersList.setCellFactory(orders -> new OrderListViewCellSecond());
        dishesList.setCellFactory(dish -> new DishListViewCell());
    }

    public void displayUserInfo(){
        loggedUser = loggedUserProperty.getValue();
        ObservableList<Integer> ordersId = FXCollections.observableArrayList(loggedUser.getOrders().values().stream().map(Order::getId).collect(Collectors.toList()));
        ordersList.setItems(ordersId);
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
}
