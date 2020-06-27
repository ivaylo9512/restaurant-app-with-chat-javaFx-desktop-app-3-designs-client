package helpers.listviews;

import models.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import controllers.firststyle.LoggedFirst;

import java.io.IOException;

import static application.RestaurantApplication.stageManager;

public class OrderListViewCell extends ListCell<Order> {
    @FXML
    private Label orderId;
    @FXML
    private HBox container;
    @FXML
    private Button button;
    @FXML
    private VBox orderPane;

    private FXMLLoader fxmlLoader;

    public Order order;

    @Override
    protected void updateItem(Order order, boolean empty) {
        super.updateItem(order, empty);
        this.order = order;

        if(empty || order == null) {

            setText(null);
            setGraphic(null);

        } else {

            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/cells/order-cell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            orderId.setText(String.valueOf(order.getId().get()));

            setText(null);
            setGraphic(container);

            LoggedFirst controller = ((LoggedFirst)stageManager.firstLoggedController);
            orderPane.setOpacity(1);
            if(controller.currentOrder.get() == order && controller.expandOrderPane.action.get()){
                controller.updateExpandOrder(orderPane, container, this);

                orderPane.setOpacity(0);
                if(order.getIndex() != getIndex()){
                    order.setIndex(getIndex());
                    controller.updateListScroll();
                }
            }
            order.setIndex(getIndex());
        }
    }
}
