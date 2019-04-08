package sample;

import Helpers.ListViews.DishListViewCell;
import Helpers.ListViews.OrderListViewCellSecond;
import Helpers.ServerRequests;
import Helpers.Services.LoginService;
import Helpers.Services.RegisterService;
import Models.Dish;
import Models.Order;
import Models.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.ConnectException;
import java.util.stream.Collectors;

import static Helpers.ServerRequests.loggedUserProperty;

public class ControllerLoggedThirdStyle {
    @FXML public ListView<Integer> ordersList;
    @FXML public ListView<Dish> dishesList;

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
}
