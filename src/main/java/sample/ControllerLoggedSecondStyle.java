package sample;

import Models.Order;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.HashMap;

import static Helpers.ServerRequests.*;

public class ControllerLoggedSecondStyle {
    @FXML VBox orderContainer;

    private HashMap<Integer, Order> ordersMap = new HashMap<>();
    @FXML
    public void initialize() {
        loggedUser.getRestaurant().getOrders().forEach(this::appendOrder);
    }

    private void appendOrder(Order order){
        ordersMap.put(order.getId(), order);

        Button orderButton = new Button("Order " + order.getId());
        orderButton.setId(String.valueOf(order.getId()));

//        orderContainer.getChildren().add(orderButton);
    }
}
