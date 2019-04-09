package Helpers.ListViews;

import Models.Order;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import sample.LoggedThirdStyle;

import java.io.IOException;
import java.time.format.DateTimeFormatter;


public class OrderListViewCellSecond extends ListCell<Order> {
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @FXML
    private AnchorPane container;

    @FXML
    private Label orderId, createdDate, createdTime, updatedDate, updatedTime;

    @FXML
    private AnchorPane infoContainer;

    private FXMLLoader mLLoader;

    @Override
    protected void updateItem(Order order, boolean empty) {
        super.updateItem(order, empty);

        if(empty || order == null) {

            setText(null);
            setGraphic(null);

        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("/FXML/cells/order-cell-second.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            container.setOnMouseClicked(event -> LoggedThirdStyle.controller.showOrder(order.getId()));

            createdDate.setText(dateFormatter.format(order.getCreated()));
            createdTime.setText(timeFormatter.format(order.getCreated()));
            updatedDate.setText(dateFormatter.format(order.getUpdated()));
            updatedTime.setText(timeFormatter.format(order.getUpdated()));
            orderId.setText(String.valueOf(order.getId()));
            setText(null);
            setGraphic(container);
        }
    }
}
