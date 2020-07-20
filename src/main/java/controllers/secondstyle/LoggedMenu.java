package controllers.secondstyle;

import animations.MoveStage;
import animations.TransitionResizeHeight;
import animations.TransitionResizeWidth;
import controllers.base.ControllerLogged;
import helpers.FontIndicator;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static application.RestaurantApplication.*;

public class LoggedMenu extends ControllerLogged {
    @FXML AnchorPane root, menu, menuButtonsContainer, profileView, profileButtonsContainer, menuContent;
    @FXML Button menuButton, notificationButton;
    @FXML Pane profileImageContainer;
    @FXML HBox notificationMenuIcon;
    @FXML StackPane profileImageClip;

    ObjectProperty<Button> currentContentButton = new SimpleObjectProperty<>();
    ObjectProperty<Button> currentMenuButton = new SimpleObjectProperty<>();

    private AnchorPane currentMenuView;

    private LoggedSecond contentController = (LoggedSecond) stageManager.secondLoggedController;
    private Stage stage = stageManager.secondLoggedMenuStage;

    private Timeline reverseDelay, reverseStageWidth, reverseStageHeight;
    private TransitionResizeWidth expandMenu;
    private TransitionResizeHeight expandMenuContent;

    private List<Node> userInfoContainers;
    private List<Node> profileButtons;
    private List<Node> menuButtons;
    private double fontPx = fontPxProperty.get();

    @FXML
    public void initialize(){
        userInfoContainers = new ArrayList<>(profileView.lookupAll("HBox"));
        profileButtons = profileButtonsContainer.getChildren();
        menuButtons = new ArrayList<>(menuButtonsContainer.lookupAll("Button"));

        scaleFontNodes();
        fontPxProperty.addListener((observable, oldValue, newValue) -> {
            fontPx = newValue.doubleValue();
            scaleFontNodes();
        });

        setNotificationBox(notificationIcon);
        setNotificationBox(notificationMenuIcon);
        bindNotificationIconSize();
        setClips();

        contentController.bindToMenu(this);

        notificationMenuIcon.opacityProperty().bind(notificationIcon.opacityProperty());
        notificationsList.setItems(notificationManager.notifications);

        setUserGraphicIndicator();
        setNotificationsListeners();
        setNotificationsFactories();
        setUserFields();
        setMenuTransitions();

        MoveStage.moveStage(menuButton, stage, contentRoot);

        editIndicator.maxHeightProperty().bind(editButton.heightProperty().subtract(15));
    }

    private void scaleFontNodes() {
        root.setStyle("-fx-font-size:" + fontPx + ";");
        updateMenuButtonsAnchors();
        updateInfoContainersAnchors();
        updateProfileButtonsAnchors();
    }

    private void updateProfileButtonsAnchors() {
        for (int i = 0; i < profileButtons.size(); i++) {
            AnchorPane.setTopAnchor(profileButtons.get(i), fontPx * 6.6 + i * fontPx * 2.75);
            AnchorPane.setBottomAnchor(profileButtons.get(i), fontPx * 0.5 + (profileButtons.size() - 1 - i) * fontPx * 2.75);
        }
    }

    private void updateInfoContainersAnchors() {
        AnchorPane.setTopAnchor(userInfoContainers.get(0), fontPx * 1.1);
        AnchorPane.setBottomAnchor(userInfoContainers.get(0), fontPx * 15.3);

        AnchorPane.setTopAnchor(userInfoContainers.get(1), fontPx * 5.5);
        AnchorPane.setBottomAnchor(userInfoContainers.get(1), fontPx * 11.0);
    }

    private void updateMenuButtonsAnchors() {
        double fontSize = fontPx;
        for (int i = 0; i < menuButtons.size(); i++) {
            Node container = menuButtons.get(i).getParent();
            AnchorPane.setLeftAnchor(container, fontSize * 3.2 + i * 7.6 * fontSize);
            AnchorPane.setRightAnchor(container, fontSize * 1.3 + (menuButtons.size() - 1 - i) * 7.6 * fontSize);
        }
    }

    private void setClips() {
        Rectangle profileViewClip = new Rectangle();
        profileViewClip.widthProperty().bind(profileView.widthProperty());
        profileViewClip.heightProperty().bind(profileView.heightProperty());
        profileView.setClip(profileViewClip);

        Circle clip = new Circle();
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
        menuClip.setX(FontIndicator.fontPx.get() * -1.25);
        menuClip.setY(FontIndicator.fontPx.get() * -1.65);
        menu.setClip(menuClip);
    }

    private void setMenuTransitions() {
        expandMenu = new TransitionResizeWidth(Duration.millis(700), menu);
        expandMenu.fromWidthProperty().bind(menu.minWidthProperty());
        expandMenu.toWidthProperty().bind(menu.maxWidthProperty());

        expandMenuContent = new TransitionResizeHeight(Duration.millis(800), menuContent, menuContent.getMaxHeight());
        expandMenuContent.setFromHeight(0);
        expandMenuContent.toHeightProperty().bind(menuContent.maxHeightProperty());

        reverseDelay = new Timeline();
        reverseStageWidth = new Timeline(new KeyFrame(Duration.millis(700), event -> stage.setWidth(root.getMinWidth())));
        reverseStageHeight = new Timeline(new KeyFrame(Duration.millis(800), event -> stage.setHeight(root.getPrefHeight())));
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
    public void adjustStage(double height, double width) throws Exception {
        stage.setWidth(root.getMaxWidth());
        stage.setHeight(root.getPrefHeight());
        stage.setY(primaryScreenBounds.getHeight() * 0.05);
        stage.setX((primaryScreenBounds.getWidth() - root.getMaxWidth()) / 2);

        root.setLayoutX((root.getMaxWidth() - root.getPrefWidth()) / 2);
    }

    @FXML
    public void onNotificationClick(){
        notificationsList.getItems().remove(notificationsList.getFocusModel().getFocusedItem());
    }

    private void expandMenuContent(){
        reverseStageHeight.stop();
        stage.setHeight(root.getMaxHeight());
        expandMenuContent.setReverse(false);
        expandMenuContent.play();
    }
    private void reverseMenuContent(){
        expandMenuContent.setReverse(true);
        expandMenuContent.play();
        reverseStageHeight.play();
    }

    @FXML public void expandMenu(){
        if(currentMenuView == null && expandMenu.getReverse()) {
            reverseStageWidth.stop();
            stage.setWidth(root.getMaxWidth());

            notificationMenuIcon.setVisible(false);
            reverseDelay.stop();

            expandMenu.stop();
            expandMenu.setReverse(false);
            expandMenu.play();
        }
    }
    @FXML
    public void reverseMenu(){
        if(currentMenuView == null || expandMenuContent.getCurrentTime().lessThan(expandMenuContent.getCycleDuration()) && expandMenuContent.getReverse()) {
            Duration delay = expandMenuContent.getCurrentRate() == 1 ?
                    expandMenuContent.getCycleDuration().subtract(expandMenuContent.getCurrentTime()) : Duration.millis(1);

            reverseDelay = new Timeline(new KeyFrame(delay, event -> {
                notificationMenuIcon.setVisible(true);

                expandMenu.stop();
                expandMenu.setReverse(true);
                expandMenu.play();

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

            currentMenuView = (AnchorPane) notificationsView;
            currentMenuView.setOpacity(1);
            currentMenuView.setDisable(false);
            menuContent.setDisable(false);

            isNewNotificationChecked.set(true);
        }else if(!currentMenuView.equals(notificationsView) && menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            profileImageContainer.setOpacity(0);

            currentMenuView = (AnchorPane) notificationsView;
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
            button = (Button)root.lookup("#" + buttonId);
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


    private void setNotificationBox(Pane icon) {
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
        Region region = (Region)notificationIcon.getChildren().get(0);
        region.minWidthProperty().bind(notificationButton.widthProperty().subtract(FontIndicator.fontPx.multiply(5.67)));
        region.prefWidthProperty().bind(notificationButton.widthProperty().subtract(FontIndicator.fontPx.multiply(5.67)));
        region.minHeightProperty().bind(region.widthProperty());
        region.prefHeightProperty().bind(region.widthProperty());

        notificationIcon.prefWidthProperty().bind(notificationButton.widthProperty().subtract(FontIndicator.fontPx.multiply(4.75)));
        notificationIcon.prefHeightProperty().bind(notificationButton.widthProperty().subtract(FontIndicator.fontPx.multiply(4.75)));

        AnchorPane.setLeftAnchor(notificationIcon, AnchorPane.getLeftAnchor(notificationButton.getParent()) + fontPx * 5.875);
    }
}
