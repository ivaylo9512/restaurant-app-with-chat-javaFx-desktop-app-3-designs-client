package helpers.listviews;

import animations.TransitionResizeHeight;
import models.Dish;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

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
    private GridPane grid;

    private FXMLLoader fxmlLoader;

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

            if(dish.isLoading()){
                ready.setText("...");
            } else if (dish.isReady()) {
                ready.setText("O");
            } else {
                ready.setText("X");
            }

            setText(null);
            setGraphic(grid);
        }

    }
    @FXML
    public void shrink(){
        TransitionResizeHeight resizeWidth2 = new TransitionResizeHeight(Duration.millis(250), grid, grid.getMaxHeight());
        resizeWidth2.play();
    }
    @FXML
    public void expand(){
        TransitionResizeHeight resizeWidth2 = new TransitionResizeHeight(Duration.millis(250), grid, grid.getMinHeight());
        resizeWidth2.play();
    }
    @FXML
    public void updateDishState() {
        if(loginManager.role.get().equals("Chef")) {

            Dish dish = getItem();
            if (!dish.isLoading() && !dish.isReady()) {
                ready.setText("...");
                dish.setLoading(true);
                orderManager.updateDishState(dish);
            }

        }else{
            stageManager.showAlert("You must be a chef to update the dish status.");
        }
    }
}
