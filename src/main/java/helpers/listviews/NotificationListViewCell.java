package helpers.listviews;

import animations.TransitionResizeHeight;
import helpers.FontIndicator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import models.Notification;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;

import static application.RestaurantApplication.notificationManager;

public class NotificationListViewCell extends ListCell<Notification> {

    @FXML
    private HBox hBox;
    @FXML
    private Pane container;
    @FXML
    private Text text;

    private FXMLLoader fxmlLoader;
    private TransitionResizeHeight resizeHeight = new TransitionResizeHeight(Duration.millis(100));


    @Override
    protected void updateItem(Notification notification, boolean empty) {
        super.updateItem(notification, empty);

        if(empty || notification == null) {

            setText(null);
            setGraphic(null);

        }else{

            if(fxmlLoader == null){
                fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/cells/notification-cell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            text.setText(notification.getName());
            resizeHeight.fromHeightProperty().bind(container.minHeightProperty());
            resizeHeight.toHeightProperty().bind(container.maxHeightProperty());
            resizeHeight.setRegion(container);

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
    public void translateRemove(){
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), hBox);
        translateTransition.setToY(-FontIndicator.fontPx.get() * 0.25);
        translateTransition.play();
        Timeline remove = new Timeline(new KeyFrame(Duration.millis(200), e -> notificationManager.removeNotification(getItem())));
        remove.play();

    }
}
