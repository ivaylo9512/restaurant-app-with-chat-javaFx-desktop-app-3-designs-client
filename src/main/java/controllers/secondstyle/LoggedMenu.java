package controllers.secondstyle;

import animations.MoveRoot;
import animations.TransitionResizeHeight;
import animations.TransitionResizeWidth;
import application.RestaurantApplication;
import controllers.base.Controller;
import controllers.base.ControllerLogged;
import helpers.FontIndicator;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static application.RestaurantApplication.*;

public class LoggedMenu extends ControllerLogged implements Controller {
    @FXML AnchorPane menuRoot, menu, menuButtons, menuButtonsContainer, profileView, profileButtonsContainer, menuContent;
    @FXML Button menuButton, notificationButton;
    @FXML Pane profileImageContainer, profileImageClip;
    @FXML HBox notificationMenuIcon;

    ObjectProperty<Button> currentContentButton = new SimpleObjectProperty<>();
    ObjectProperty<Button> currentMenuButton = new SimpleObjectProperty<>();

    private HBox notificationBox;
    private AnchorPane currentMenuView;

    private FontIndicator fontIndicator = RestaurantApplication.fontIndicator;

    private LoggedSecond contentController = (LoggedSecond) stageManager.secondLoggedController;
    private Stage stage = stageManager.secondLoggedMenuStage;

    private Timeline reverseDelay, reverseStageWidth, reverseStageHeight;
    private TransitionResizeWidth reverseMenu, expandMenu;
    private TransitionResizeHeight reverseMenuContent, expandMenuContent;
    private List<Node> userInfoContainers;
    private List<Node> profileButtons;

    @FXML
    public void initialize(){
        notificationBox = (HBox)notificationIcon;
        userInfoContainers = new ArrayList<>(profileView.lookupAll("HBox"));
        profileButtons = profileButtonsContainer.getChildren();

        setNotificationBox(notificationBox);
        setNotificationBox(notificationMenuIcon);
        bindNotificationIconSize();
        setClips();

        contentController.bindToMenu(this);

        notificationMenuIcon.opacityProperty().bind(notificationBox.opacityProperty());
        notificationsList.setItems(notificationManager.notifications);

        menuRoot.setStyle("-fx-font-size:" + fontIndicator.getFontPx() + ";");
        updateMenuButtonsAnchors();
        updateInfoContainersAnchors();
        updateProfileButtonsAnchors();

        fontIndicator.getFontPxProperty().addListener((observable, oldValue, newValue) -> {
            menuRoot.setStyle("-fx-font-size:" + fontIndicator.getFontPx() + ";");
            updateMenuButtonsAnchors();
            updateInfoContainersAnchors();
            updateProfileButtonsAnchors();
        });

        setUserGraphicIndicator();
        setNotificationsListeners();
        setNotificationsFactories();
        setUserFields();
        setMenuTransitions();

        MoveRoot.moveStage(menuButton, stage);

        editIndicator.maxHeightProperty().bind(editButton.heightProperty().subtract(15));
    }

    private void updateProfileButtonsAnchors() {
        double fontSize = fontIndicator.getFontPx();
        for (int i = 0; i < profileButtons.size(); i++) {
            AnchorPane.setTopAnchor(profileButtons.get(i), fontSize * 6.8 + i * fontSize * 2.75);
            AnchorPane.setBottomAnchor(profileButtons.get(i), fontSize * 0.5 + (profileButtons.size() - 1 - i) * fontSize * 2.75);
        }
    }

    private void updateInfoContainersAnchors() {
        AnchorPane.setTopAnchor(userInfoContainers.get(0), fontIndicator.getFontPx() * 1.1);
        AnchorPane.setBottomAnchor(userInfoContainers.get(0), fontIndicator.getFontPx() * 15.3);

        AnchorPane.setTopAnchor(userInfoContainers.get(1), fontIndicator.getFontPx() * 5.5);
        AnchorPane.setBottomAnchor(userInfoContainers.get(1), fontIndicator.getFontPx() * 11.0);
    }

    private void updateMenuButtonsAnchors() {
        double fontSize = fontIndicator.getFontPx();
        int anchorCount = 0;
        for (Node node : menuButtons.getChildren()) {
            if (node instanceof AnchorPane) {
                AnchorPane.setLeftAnchor(node, fontSize * 3.2 + anchorCount * 7.6 * fontSize);
                AnchorPane.setRightAnchor(node, fontSize * 1.3 + (menuButtons.getChildren().size() - 2 - anchorCount) * 7.6 * fontSize);
                anchorCount++;
            }
        }
    }

    private void setClips() {
        Rectangle profileViewClip = new Rectangle();
        profileViewClip.widthProperty().bind(profileView.widthProperty());
        profileViewClip.heightProperty().bind(profileView.heightProperty());
        profileView.setClip(profileViewClip);

        Circle clip = new Circle(30.8, 30.8, 30.8);
        clip.centerXProperty().bind(profileImageClip.widthProperty().divide(2));
        clip.centerYProperty().bind(profileImageClip.widthProperty().divide(2));
        clip.radiusProperty().bind(profileImageClip.widthProperty().divide(2));
        profileImageClip.setClip(clip);

        Rectangle notificationClip = new Rectangle();
        notificationClip.setArcHeight(33);
        notificationClip.setArcWidth(33);
        notificationClip.heightProperty().bind(notificationsList.heightProperty());
        notificationClip.widthProperty().bind(notificationsList.widthProperty());
        notificationsList.setClip(notificationClip);

        Rectangle menuClip = new Rectangle();
        menuClip.heightProperty().bind(menu.heightProperty().add(45));
        menuClip.widthProperty().bind(menu.widthProperty().add(30));
        menuClip.setX(-15);
        menuClip.setY(-20);

        menu.setClip(menuClip);
    }

    private void setMenuTransitions() {
        expandMenu = new TransitionResizeWidth(Duration.millis(700), menu, menu.getMaxWidth());
        reverseMenu = new TransitionResizeWidth(Duration.millis(700), menu, menu.getMinWidth());
        reverseMenuContent = new TransitionResizeHeight(Duration.millis(800), menuContent, 0);
        expandMenuContent = new TransitionResizeHeight(Duration.millis(800), menuContent, menuContent.getMaxHeight());

        reverseDelay = new Timeline();
        reverseStageWidth = new Timeline(new KeyFrame(Duration.millis(700), event -> stage.setWidth(menuRoot.getMinWidth())));
        reverseStageHeight = new Timeline(new KeyFrame(Duration.millis(800), event -> stage.setHeight(menuRoot.getPrefHeight())));
    }

    @Override
    public void resetStage() {
        if(notificationsList.getItems().size() > 0) notificationsList.scrollTo(0);

        if(currentMenuView != null){
            currentMenuView.setDisable(true);
            currentMenuView.setOpacity(0);
            currentMenuView = null;
        }
        expandMenu();
        reverseMenuContent();

        if(currentMenuButton.get() != null){
            currentMenuButton.get().getParent().getStyleClass().add("shadow");
            currentMenuButton.set(null);
        }

        if(currentContentButton.get() != null){
            currentContentButton.get().getParent().getStyleClass().add("shadow");
            currentContentButton.set(null);
        }

        profileImageContainer.setOpacity(0);
        menuContent.setDisable(true);
        notificationsView.setOpacity(0);
        notificationsView.setDisable(true);
    }

    @Override
    public void setStage() throws Exception {
        stage.setWidth(menuRoot.getMaxWidth());
        stage.setHeight(menuRoot.getPrefHeight());
        stage.setY(primaryScreenBounds.getHeight() * 0.05);
        stage.setX((primaryScreenBounds.getWidth() - menuRoot.getMaxWidth()) / 2);

        menuRoot.setLayoutX((menuRoot.getMaxWidth() - menuRoot.getPrefWidth()) / 2);
        expandMenu();
    }

    @FXML
    public void onNotificationClick(){
        notificationsList.getItems().remove(notificationsList.getFocusModel().getFocusedItem());
    }

    private void expandMenuContent(){
        reverseStageHeight.stop();
        reverseMenuContent.stop();

        stage.setHeight(menuRoot.getMaxHeight());

        expandMenuContent = new TransitionResizeHeight(Duration.millis(800), menuContent, menuContent.getMaxHeight());
        expandMenuContent.play();
    }
    private void reverseMenuContent(){
        expandMenuContent.stop();

        reverseMenuContent = new TransitionResizeHeight(Duration.millis(800), menuContent, 0);
        reverseMenuContent.play();
        reverseStageHeight.play();
    }

    @FXML public void expandMenu(){
        if(stage.getHeight() != menuRoot.getPrefWidth()) {
            reverseStageWidth.stop();
            stage.setWidth(menuRoot.getMaxWidth());

            notificationMenuIcon.setVisible(false);
            reverseDelay.stop();
            reverseMenu.stop();

            expandMenu = new TransitionResizeWidth(Duration.millis(700), menu, menu.getMaxWidth());
            expandMenu.play();
        }
    }
    @FXML
    public void reverseMenu(){
        if(currentMenuView == null || reverseMenuContent.getCurrentRate() != 0 ) {
            Duration delay = reverseMenuContent.getCurrentRate() == 1 ?
                    reverseMenuContent.getCycleDuration().subtract(reverseMenuContent.getCurrentTime()) : Duration.millis(1);

            reverseDelay = new Timeline(new KeyFrame(delay, event -> {
                notificationMenuIcon.setVisible(true);

                expandMenu.stop();

                reverseMenu = new TransitionResizeWidth(Duration.millis(700), menu, menu.getMinWidth());
                reverseMenu.play();

                reverseStageWidth.play();
            }));
            reverseDelay.play();
        }
    }

    @FXML
    public void showProfile(){
        if(menuContent.isDisabled()) {
            expandMenuContent();
            currentMenuView = profileView;
            currentMenuView.setDisable(false);
            currentMenuView.setOpacity(1);

            menuContent.setDisable(false);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), profileImageContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(300));
            fadeIn.play();

        }else if(!currentMenuView.equals(profileView) && menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            currentMenuView.setOpacity(0);
            currentMenuView.setDisable(true);

            currentMenuView = profileView;
            currentMenuView.setDisable(false);

            profileImageContainer.setOpacity(1);
        }else if(menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            reverseMenuContent();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(600), profileImageContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
            Timeline removeView = new Timeline(new KeyFrame(Duration.millis(800), event -> {
                menuContent.setDisable(true);
                currentMenuView = null;
            }));
            removeView.play();
        }
    }
    @FXML
    public void showNotifications(){
        if(menuContent.isDisabled()) {
            expandMenuContent();

            currentMenuView = notificationsView;
            currentMenuView.setOpacity(1);
            currentMenuView.setDisable(false);
            menuContent.setDisable(false);

            isNewNotificationChecked.set(true);
        }else if(!currentMenuView.equals(notificationsView) && menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            profileImageContainer.setOpacity(0);

            currentMenuView = notificationsView;
            currentMenuView.setDisable(false);
            currentMenuView.setOpacity(1);

            isNewNotificationChecked.set(true);
        }else if(menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            reverseMenuContent();

            Timeline removeView = new Timeline(new KeyFrame(Duration.millis(800), event -> {
                menuContent.setDisable(true);
                currentMenuView.setDisable(true);
                currentMenuView.setOpacity(0);
                currentMenuView = null;
            }));
            removeView.play();
        }
    }
    @FXML
    public void profileButtonHoverOver(MouseEvent event){
        AnchorPane shadowContainer = (AnchorPane) ((Button)event.getSource()).getParent();
        shadowContainer.getStyleClass().add("profile-button-hovered");
    }
    @FXML
    public void profileButtonHoverOut(MouseEvent event){
        AnchorPane shadowContainer = (AnchorPane) ((Button)event.getSource()).getParent();
        shadowContainer.getStyleClass().remove("profile-button-hovered");
    }
    @FXML
    public void onMenuAction(MouseEvent event){
        Button button = (Button) event.getSource();
        setView(button.getId(), button);
    }

    void setView(String buttonId, Button button) {
        if(button == null){
            button = (Button)menuRoot.lookup("#" + buttonId);
        }

        switch (buttonId){
            case "profileButton":
                showProfile();
                if(menuContent.getPrefHeight() == 0 || menuContent.getPrefHeight() == menuContent.getMaxHeight())
                    changeButtonStyle(button, currentMenuButton);
                break;
            case "notificationButton":
                if(menuContent.getPrefHeight() == 0 || menuContent.getPrefHeight() == menuContent.getMaxHeight())
                    changeButtonStyle(button, currentMenuButton);
                showNotifications();
                break;
            case "orderButton":
                contentController.displayView(contentController.orderView);
                changeButtonStyle(button, currentContentButton);
                break;
            case "chatButton":
                contentController.resetOrder();
                contentController.displayView(contentController.chatView);
                changeButtonStyle(button, currentContentButton);
                break;
            case "createButton":
                contentController.resetOrder();
                contentController.displayView(contentController.createView);
                changeButtonStyle(button, currentContentButton);
                break;
        }
    }

    private void changeButtonStyle(Button newButton, ObjectProperty<Button> currentButtonProperty){
        Button currentButton = currentButtonProperty.get();
        if(currentButton != null){
            currentButton.getParent().getStyleClass().add("shadow");
        }
        if(newButton != currentButton){
            Node container = newButton.getParent();
            container.getStyleClass().remove("shadow");

            AnchorPane.setTopAnchor(container, 0.0);
            AnchorPane.setBottomAnchor(container, 0.0);

            currentButtonProperty.set(newButton);
        }else{
            currentButtonProperty.set(null);
        }
    }
    @FXML
    public void menuButtonFocus(MouseEvent event){
        Node buttonContainer = ((Button) event.getSource()).getParent();

        if(buttonContainer.getStyleClass().contains("shadow")) {
            AnchorPane.setTopAnchor(buttonContainer, -4.5);
            AnchorPane.setBottomAnchor(buttonContainer, -4.0);
        }
    }
    @FXML
    public void menuButtonUnFocus(MouseEvent event){
        Node buttonContainer = ((Button) event.getSource()).getParent();

        if(buttonContainer.getStyleClass().contains("shadow")) {
            AnchorPane.setTopAnchor(buttonContainer, 0.0);
            AnchorPane.setBottomAnchor(buttonContainer, 0.0);
        }
    }


    private void setNotificationBox(HBox icon) {
        SVGPath usb3 = new SVGPath();
        usb3.setContent("m434.753906 360.8125c-32.257812-27.265625-50.753906-67.117188-50.753906-109.335938v-59.476562c0-75.070312-55.765625-137.214844-128-147.625v-23.042969c0-11.796875-9.558594-21.332031-21.332031-21.332031-11.777344 0-21.335938 9.535156-21.335938 21.332031v23.042969c-72.253906 10.410156-128 72.554688-128 147.625v59.476562c0 42.21875-18.496093 82.070313-50.941406 109.503907-8.300781 7.105469-13.058594 17.429687-13.058594 28.351562 0 20.589844 16.746094 37.335938 37.335938 37.335938h352c20.585937 0 37.332031-16.746094 37.332031-37.335938 0-10.921875-4.757812-21.246093-13.246094-28.519531zm0 0");

        SVGPath usb4 = new SVGPath();
        usb4.setContent("m234.667969 512c38.632812 0 70.953125-27.542969 78.378906-64h-156.757813c7.421876 36.457031 39.742188 64 78.378907 64zm0 0");
        Shape s = Shape.union(usb3,usb4);

        Region region = new Region();
        region.setShape(s);
        icon.getChildren().add(region);

        RotateTransition tt = new RotateTransition(Duration.millis(200), icon);
        tt.setByAngle(16);
        RotateTransition tl = new RotateTransition(Duration.millis(200), icon);
        tl.setByAngle(-32);
        PauseTransition pauseTransition = new PauseTransition(Duration.millis(500));

        SequentialTransition sequentialTransition = new SequentialTransition(pauseTransition, tt, tl);
        sequentialTransition.setCycleCount(Timeline.INDEFINITE);
        sequentialTransition.setAutoReverse(true);
        sequentialTransition.play();

    }
    void bindNotificationIconSize(){
        Region region = (Region)notificationBox.getChildren().get(0);
        region.minWidthProperty().bind(notificationButton.widthProperty().subtract(68));
        region.prefWidthProperty().bind(notificationButton.widthProperty().subtract(68));
        region.minHeightProperty().bind(region.widthProperty());
        region.prefHeightProperty().bind(region.widthProperty());

        notificationBox.prefWidthProperty().bind(notificationButton.widthProperty().subtract(57));
        notificationBox.prefHeightProperty().bind(notificationButton.widthProperty().subtract(57));
    }

    public FontIndicator getFontIndicator() {
        return fontIndicator;
    }
}
