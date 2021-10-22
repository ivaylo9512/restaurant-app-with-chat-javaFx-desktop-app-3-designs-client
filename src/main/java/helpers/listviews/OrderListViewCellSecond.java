package helpers.listviews;

import javafx.scene.text.Text;
import models.Order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.util.stream.Collectors;

public class OrderListViewCellSecond extends ListCell<Order> {
    @FXML
    private StackPane container;
    @FXML
    private HBox orderId;

    private FXMLLoader fxmlLoader;

    @Override
    protected void updateItem(Order order, boolean empty) {
        super.updateItem(order, empty);

        if(empty || order == null) {

            setText(null);
            setGraphic(null);

        } else {

            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/cells/order-cell-second.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            orderId.getChildren().setAll(String.valueOf(order.getId().get())
                    .chars()
                    .mapToObj(c -> new Text(String.valueOf((char)c)))
                    .collect(Collectors.toList()));

            setText(null);
            setGraphic(container);
        }
    }
}
