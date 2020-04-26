package sample;

import Animations.MoveRoot;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import sample.base.Controller;

import static Application.RestaurantApplication.stageManager;

public class ControllerLoggedSecondMenu implements Controller {
    @FXML AnchorPane menuRoot, menu, menuButtons, menuButtonsContainer, profileView, menuContent;
    @FXML Button menuButton, updateButton;
    @FXML Region notificationIcon;
    @FXML Pane profileImageContainer, profileImageClip;
    @FXML Region notificationRegion;

    ControllerLoggedSecondStyle contentController;

    @FXML
    public void initialize(){
        MoveRoot.move(menuButton, menuRoot);

        contentController = (ControllerLoggedSecondStyle) stageManager.secondLoggedController;
    }

    @Override
    public void resetStage() {
        menuContent.setDisable(true);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), profileImageContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.play();
    }

    @Override
    public void setStage() throws Exception {
        menuRoot.setLayoutX((primaryScreenBounds.getWidth() - menuRoot.getWidth()) / 2);
        menuRoot.setLayoutY(contentController.contentRoot.getLayoutY() - 60);
    }
}
