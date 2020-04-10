package Helpers.ListViews;

import Animations.TransitionResizeHeight;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static Application.RestaurantApplication.notificationManager;

public class NotificationListViewCell extends ListCell<String> {

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if(empty || item == null) {

            setText(null);
            setGraphic(null);

        }else{
            AnchorPane pane = new AnchorPane();
            pane.getStyleClass().add("notification");
            pane.setPrefHeight(48);

            Text notification = new Text(item);
            HBox hBox = new HBox(notification);
            hBox.setLayoutY(10);

            pane.getChildren().add(hBox);


            AnchorPane.setLeftAnchor(hBox, 10.0);
            AnchorPane.setRightAnchor(hBox, 10.0);
            AnchorPane.setBottomAnchor(hBox, 3.0);
            AnchorPane.setTopAnchor(hBox, 3.0);

            pane.setOnMouseEntered(event -> {
                TransitionResizeHeight resize = new TransitionResizeHeight(Duration.millis(100), pane, 58);
                resize.play();
            });
            pane.setOnMouseExited(event -> {
                TransitionResizeHeight resize = new TransitionResizeHeight(Duration.millis(100), pane, 48);
                resize.play();
            });
            pane.setOnMouseClicked(event -> {
                TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), hBox);
                translateTransition.setToY(-10);
                translateTransition.setFromY(0);
                translateTransition.play();
            });
            this.setOnMouseClicked(event -> {
                TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), hBox);
                translateTransition.setToY(-3);
                translateTransition.setFromY(0);
                translateTransition.play();

                Timeline remove = new Timeline(new KeyFrame(Duration.millis(200), e -> {
                    notificationManager.removeNotification(item);
                }));
                remove.play();
            });

            setText(null);
            setGraphic(pane);

        }
    }
}
