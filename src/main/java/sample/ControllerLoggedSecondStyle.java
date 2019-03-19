package sample;

import Animations.TransitionResizeWidth;
import Models.Order;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static Helpers.ServerRequests.*;

public class ControllerLoggedSecondStyle {
    @FXML VBox orderContainer, dishesContainer;
    @FXML Label dishesCount, orderId, updatedDate, updatedTime, createdDate, createdTime;
    @FXML AnchorPane orderInfo, menu;

    private Button displayedOrder;
    private HashMap<Integer, Order> ordersMap = new HashMap<>();
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @FXML
    public void initialize() {
        loggedUser.getRestaurant().getOrders().forEach(this::appendOrder);
        menu.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            TransitionResizeWidth expand = new TransitionResizeWidth(Duration.millis(700), menu, 400);
            expand.play();
        });
        menu.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            TransitionResizeWidth reverse = new TransitionResizeWidth(Duration.millis(700), menu, 38.5);
            reverse.play();
        });
    }

    @FXML
    public void focus(MouseEvent event){
        Button button = (Button) event.getSource();
        AnchorPane.setTopAnchor(button, -5.5);
        AnchorPane.setBottomAnchor(button, -4.0);
    }
    @FXML
    public void unFocus(MouseEvent event){
        Button button = (Button) event.getSource();
        AnchorPane.setTopAnchor(button, -1.0);
        AnchorPane.setBottomAnchor(button, -1.0);
    }
    private void appendOrder(Order order){
        int orderId = order.getId();
        ordersMap.put(orderId, order);

        Button orderButton = new Button("Order " + orderId);
        orderButton.setId(String.valueOf(orderId));
        orderButton.setOnMouseClicked(e -> showOrder(order, orderButton));

        orderContainer.getChildren().add(0, orderButton);

    }
    private void showOrder(Order order, Button orderButton){
        if(displayedOrder != null){
            displayedOrder.getStyleClass().remove("focused");
        }
        displayedOrder = orderButton;
        displayedOrder.getStyleClass().add("focused");

        orderId.setText(String.valueOf(order.getId()));
        dishesCount.setText(null);
        dishesCount.setText(String.valueOf(order.getDishes().size()));

        createdDate.setText(dateFormatter.format(order.getCreated()));
        createdTime.setText(timeFormatter.format(order.getCreated()));
        updatedDate.setText(dateFormatter.format(order.getUpdated()));
        updatedTime.setText(timeFormatter.format(order.getUpdated()));


        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), orderInfo);
        fadeIn.setFromValue(0.36);
        fadeIn.setToValue(1);
        fadeIn.play();


        dishesContainer.getChildren().clear();
        order.getDishes().forEach(dish -> {
            Label price = new Label(String.valueOf(dish.getId()));
            Label name = new Label(dish.getName());
            Label ready = new Label();

            if (dish.getReady()) {
                ready.setText("O");
            } else {
                ready.setText("O");
            }
            ready.setId("dish" + dish.getId());

            price.getStyleClass().add("price");
            ready.getStyleClass().add("ready");
            name.getStyleClass().add("name");
            HBox.setHgrow(name, Priority.ALWAYS);
            HBox dishContainer = new HBox(price, name, ready);

            dishesContainer.getChildren().add(dishContainer);
        });
    }
}
