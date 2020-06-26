package helpers.listviews;

import helpers.FontIndicator;
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
    private Label orderId, updatedDate, createdDate, createdTime, updatedTime;
    @FXML
    private AnchorPane orderPane;
    @FXML
    private Pane updatedContainer;
    @FXML
    private AnchorPane dishesAnchor, createdContainer;
    @FXML
    private ScrollPane dishesScroll;
    @FXML
    private VBox dishesBox;
    @FXML
    private HBox container;
    @FXML
    private Button button;

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

            orderPane.setId(String.valueOf(order.getId().get()));
            orderId.setText(String.valueOf(order.getId().get()));

            double fontPx = FontIndicator.fontPx.get();
            AnchorPane.setLeftAnchor(orderId, fontPx * 2.3);
            AnchorPane.setRightAnchor(orderId, fontPx * 2.3);
            AnchorPane.setLeftAnchor(button, fontPx * 2.3);
            AnchorPane.setTopAnchor(button, fontPx * 4);

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
