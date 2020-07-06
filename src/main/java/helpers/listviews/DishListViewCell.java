package helpers.listviews;

import animations.TransitionResizeHeight;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import models.Dish;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import java.io.IOException;

import static application.RestaurantApplication.*;

public class DishListViewCell extends ListCell<Dish> {
    @FXML
    private Label price;
    @FXML
    private Label name;
    @FXML
    private Label ready;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private AnchorPane container;

    private FXMLLoader fxmlLoader;
    private TransitionResizeHeight resizeHeight = new TransitionResizeHeight(Duration.millis(250));

    @Override
    protected void updateItem(Dish dish, boolean empty) {
        super.updateItem(dish, empty);

        if(empty || dish == null) {

            setText(null);
            setGraphic(null);

        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/cells/dish-cell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            price.setText(String.valueOf(dish.getId()));
            name.setText(dish.getName());
            resizeHeight.fromHeightProperty().bind(container.minHeightProperty());
            resizeHeight.fromHeightProperty().bind(container.maxHeightProperty());
            resizeHeight.setRegion(container);

            if(dish.isLoading()){
                progressIndicator.setOpacity(1);
                ready.setOpacity(0);
            } else if (dish.isReady()) {
                progressIndicator.setOpacity(0);
                ready.setOpacity(1);
                ready.setText("O");
            } else {
                progressIndicator.setOpacity(0);
                ready.setOpacity(1);
                ready.setText("X");
            }

            setText(null);
            setGraphic(container);
        }

    }
    @FXML
    public void resizeHeight(){
        resizeHeight.play();
        resizeHeight.setReverse(!resizeHeight.getReverse());
    }
    @FXML
    public void updateDishState() {
        if(loginManager.role.get().equals("Chef")) {

            Dish dish = getItem();
            if (!dish.isLoading() && !dish.isReady()) {
                progressIndicator.setOpacity(1);
                ready.setOpacity(0);

                dish.setLoading(true);
                orderManager.updateDishState(dish);
            }

        }else{
            alertManager.addLoggedAlert("You must be a chef to update the dish status.");
        }
    }
}
