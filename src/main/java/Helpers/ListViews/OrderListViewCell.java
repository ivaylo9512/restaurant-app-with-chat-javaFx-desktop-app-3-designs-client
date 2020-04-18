package Helpers.ListViews;

import Animations.ExpandOrderPane;
import Application.StageManager;
import Helpers.Scrolls;
import Models.Order;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import sample.ControllerLoggedFirstStyle;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static Application.RestaurantApplication.stageManager;

public class OrderListViewCell extends ListCell<Order> {
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @FXML
    private Label orderId, updatedDate, createdDate, createdTime, updatedTime;

    @FXML
    private AnchorPane orderPane;

    @FXML
    private Pane container, updatedContainer;

    @FXML
    private AnchorPane dishesAnchor, createdContainer;

    @FXML
    private ScrollPane dishesScroll;

    @FXML
    private VBox dishesBox;
    @FXML
    private Button button;

    private FXMLLoader mLLoader;

    public Order order;

    @Override
    protected void updateItem(Order order, boolean empty) {
        super.updateItem(order, empty);
        this.order = order;

        if(empty || order == null) {

            setText(null);
            setGraphic(null);

        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("/FXML/cells/order-cell.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            orderPane.setId(String.valueOf(order.getId().get()));

            orderId.setText(String.valueOf(order.getId().get()));

            container.setClip(new Rectangle(container.getPrefWidth(), container.getPrefHeight()));

            setText(null);
            setGraphic(container);

            orderPane.setOpacity(1);
            if(ExpandOrderPane.currentOrder == order){
                ExpandOrderPane.currentPane = orderPane;
                ExpandOrderPane.currentContainer = container;
                ExpandOrderPane.cell = this;
                ExpandOrderPane.setOrderDimension();

                orderPane.setOpacity(0);
                if(order.getIndex() != getIndex()){
                    ((ControllerLoggedFirstStyle)stageManager.currentController).updateListScroll();
                }
            }
            order.setIndex(getIndex());
        }

    }


}
