package helpers.listviews;

import models.Order;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class OrderListViewCellThird extends ListCell<Order> {
    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @FXML
    private AnchorPane container, infoContainer;

    @FXML
    private Pane createdContainer, updatedContainer;

    @FXML
    private Label orderId, createdDate, createdTime, updatedDate, updatedTime;

    private FXMLLoader fxmlLoader;

    @Override
    protected void updateItem(Order order, boolean empty) {
        super.updateItem(order, empty);

        if(empty || order == null) {

            setText(null);
            setGraphic(null);

        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/cells/order-cell-third.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            LocalDateTime createdAt = order.getCreatedAt().get();
            LocalDateTime updatedAt = order.getUpdatedAt().get();

            createdDate.setText(dateFormatter.format(createdAt));
            createdTime.setText(timeFormatter.format(createdAt));
            updatedDate.setText(dateFormatter.format(updatedAt));
            updatedTime.setText(timeFormatter.format(updatedAt));
            orderId.setText(String.valueOf(order.getId()));

            setText(null);
            setGraphic(container);
        }
    }

    @FXML
    public void translateCreated(){
        createdDate.setText("created");
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), createdDate);
        translate.setToX(30);
        translate.play();
    }

    @FXML
    public void reverseCreated(){
        createdDate.setText(dateFormatter.format(getItem().getCreatedAt().get()));
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), createdDate);
        translate.setToX(0);
        translate.play();
    }

    @FXML
    public void reverseUpdated() {
        updatedDate.setText("updated");
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), updatedDate);
        translate.setToX(30);
        translate.play();
    }

    @FXML
    public void translateUpdated() {
        updatedDate.setText(dateFormatter.format(getItem().getUpdatedAt().get()));
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), updatedDate);
        translate.setToX(0);
        translate.play();
    }
}
