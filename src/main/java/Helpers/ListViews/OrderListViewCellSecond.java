package Helpers.ListViews;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;


public class OrderListViewCellSecond extends ListCell<Integer> {

    @Override
    protected void updateItem(Integer orderId, boolean empty) {
        super.updateItem(orderId, empty);

        if(empty || orderId == null) {

            setText(null);
            setGraphic(null);

        } else {
            Button button = new Button();
            button.setText(String.valueOf(orderId));
            HBox container = new HBox(button);
            container.setAlignment(Pos.CENTER);
            setText(null);
            setGraphic(container);
        }
    }
}
