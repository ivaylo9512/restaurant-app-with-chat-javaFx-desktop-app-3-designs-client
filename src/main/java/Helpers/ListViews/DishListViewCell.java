package Helpers.ListViews;

import Models.Dish;
import Models.Menu;
import Models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import sample.LoggedSecondStyle;

import java.io.IOException;

import static Helpers.ServerRequests.loggedUserProperty;

public class DishListViewCell extends ListCell<Dish> {
    @FXML
    private Label price;
    @FXML
    private Label name;
    @FXML
    private Label ready;

    @FXML
    private GridPane grid;

    private FXMLLoader mLLoader;

    @Override
    protected void updateItem(Dish dish, boolean empty) {
        super.updateItem(dish, empty);

        if(empty || dish == null) {

            setText(null);
            setGraphic(null);

        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("/FXML/dish-cell.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            price.setText(String.valueOf(dish.getId()));
            name.setText(dish.getName());
            if (dish.getReady()) {
                ready.setText("O");
                ready.setUserData("ready");
            } else {
                ready.setText("X");
                ready.setUserData("not ready");
            }
            ready.setId("dish" + dish.getId());
            grid.setOnMouseClicked(event -> {
                if(ready.getText().equals("X")) {
                    LoggedSecondStyle.controller.updateDishStatus(dish, ready);
                }
            });
            grid.prefWidthProperty().bind(widthProperty().subtract(13));
            setText(null);
            setGraphic(grid);
        }

    }
}
