package helpers.listviews;

import animations.TransitionResizeHeight;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import models.Notification;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;

import static application.RestaurantApplication.notificationManager;

public class NotificationListViewCell extends ListCell<Notification> {

    @FXML
    private AnchorPane container;
    @FXML
    private HBox hBox;
    @FXML
    private Text text;

    private FXMLLoader fxmlLoader;

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

            setText(null);
            setGraphic(container);
        }
    }

    @FXML
    public void expandHeight(){
        TransitionResizeHeight resize = new TransitionResizeHeight(Duration.millis(100), container, 58);
        resize.play();
    }
    @FXML
    public void reverseHeight(){
        TransitionResizeHeight resize = new TransitionResizeHeight(Duration.millis(100), container, 48);
        resize.play();
    }
    @FXML
    public void translateRemove(){
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), hBox);
        translateTransition.setToY(-3);
        translateTransition.setFromY(0);
        translateTransition.play();
    }

    {
        this.setOnMouseClicked(event -> {
            if(!this.isEmpty()){
                translateRemove();
                Timeline remove = new Timeline(new KeyFrame(Duration.millis(200), e -> notificationManager.removeNotification(getItem())));
                remove.play();
            }
        });

        this.setOnMouseEntered(event -> {
            if(!this.isEmpty()) {
                expandHeight();
            }
        });
        this.setOnMouseExited(event -> {
            if(!this.isEmpty()) {
                reverseHeight();
            }
        });
    }
}
