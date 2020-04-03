package Helpers.ListViews;

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

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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

    @Override
    protected void updateItem(Order order, boolean empty) {
        super.updateItem(order, empty);

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

            orderPane.setId(String.valueOf(order.getId()));

            orderId.setText(String.valueOf(order.getId()));

            container.setClip(new Rectangle(container.getPrefWidth(), container.getPrefHeight()));

            setText(null);
            setGraphic(container);
        }

    }
}
