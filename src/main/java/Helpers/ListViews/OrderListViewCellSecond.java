package Helpers.ListViews;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import sample.LoggedThirdStyle;

import java.io.IOException;


public class OrderListViewCellSecond extends ListCell<Integer> {

    @FXML
    private AnchorPane container;

    @FXML
    private Label orderId;

    private FXMLLoader mLLoader;

    @Override
    protected void updateItem(Integer id, boolean empty) {
        super.updateItem(id, empty);

        if(empty || id == null) {

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
            orderId.setText(String.valueOf(id));
            setText(null);
            setGraphic(container);
        }
    }
}
