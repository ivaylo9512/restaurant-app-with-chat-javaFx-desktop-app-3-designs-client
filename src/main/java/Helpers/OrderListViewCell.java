package Helpers;

import Models.Order;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import sample.LoggedFirstStyle;

import java.io.IOException;
import java.util.Objects;

public class OrderListViewCell extends ListCell<Order> {
    @FXML
    private Label orderId;

    @FXML
    private AnchorPane orderPane;

    @FXML
    private Pane container;

    @FXML
    private AnchorPane dishesAnchor;

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
                mLLoader = new FXMLLoader(getClass().getResource("/FXML/order-cell.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            Scrolls.fixBlurriness(dishesScroll);

            orderPane.setId(String.valueOf(order.getId()));

            orderId.setText(String.valueOf(order.getId()));

            button.prefWidthProperty().bind(((orderPane.prefWidthProperty()
                    .subtract(81.6))
                    .divide(15))
                    .add(28));
            button.prefHeightProperty().bind(((orderPane.prefHeightProperty()
                    .subtract(81.6))
                    .divide(30))
                    .add(28));

            button.setOnMouseClicked(event -> LoggedFirstStyle.controller.expandOrder(event));

            dishesScroll.skinProperty().addListener((observable, oldValue, newValue) -> {
                ScrollBar bar = Scrolls.findVerticalScrollBar(dishesScroll);
                Objects.requireNonNull(bar).addEventFilter(MouseEvent.MOUSE_DRAGGED, Event::consume);
                Objects.requireNonNull(bar).addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
            });

            dishesAnchor.prefHeightProperty().bind(orderPane.prefHeightProperty().subtract(99));
            dishesAnchor.setDisable(true);
            dishesAnchor.setOpacity(0);


            dishesBox.prefWidthProperty().bind(dishesScroll.widthProperty().subtract(14.65));
            dishesBox.getChildren().clear();
            order.getDishes().forEach(dish -> {
                Label amount = new Label("3");
                amount.getStyleClass().add("amount");

                Label ready;
                if (dish.getReady()) {
                    ready = new Label("O");
                } else {
                    ready = new Label("X");
                }
                ready.setId("dish" + dish.getId());
                ready.getStyleClass().add("ready");
                ready.setOnMouseClicked(event -> {
                    if(ready.getText().equals("X")) {
                        LoggedFirstStyle.controller.updateDishStatus(order.getId(), dish.getId());
                    }
                });

                TextField name = new TextField(dish.getName());
                name.getStyleClass().add("name");
                name.setDisable(true);

                HBox dishBox = new HBox(amount, name, ready);
                dishBox.getStyleClass().add("dish");

                amount.setViewOrder(1);
                name.setViewOrder(3);
                HBox.setHgrow(name, Priority.ALWAYS);
                dishesBox.getChildren().add(dishBox);
            });

            setText(null);
            setGraphic(container);
        }

    }
}
