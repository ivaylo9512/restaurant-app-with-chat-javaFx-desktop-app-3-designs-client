package sample;

import Animations.MoveRoot;
import Animations.TransitionResizeHeight;
import Animations.TransitionResizeWidth;
import Helpers.ListViews.ChatsUsersListViewCellSecond;
import Helpers.Scrolls;
import Models.*;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import sample.base.ControllerLogged;


public class ControllerLoggedSecondStyle extends ControllerLogged {
    @FXML Label dishesCountLabel;

    @FXML AnchorPane menuRoot,menu, menuButtons, menuButtonsContainer, profileView, menuContent, orderInfo, orderView,
            chatView, userChatsClip, createView, dishesContainer, chatContainer;

    @FXML Button menuButton, updateButton;
    @FXML HBox notificationsInfo;
    @FXML Region notificationIcon;
    @FXML Pane profileImageContainer, profileImageClip, contentBar;
    @FXML TextArea chatTextArea;
    @FXML ScrollPane chatScroll;
    @FXML VBox chatBlock;
    @FXML Region notificationRegion;

    private AnchorPane currentView, currentMenuView;

    private ChatValue chatValue;

    @Override
    public void initialize() {
        super.initialize();

        setClips();
        setNotificationIcon();
        focusCurrentOrderOnListUpdate();

        Scrolls scrolls = new Scrolls(chatScroll, chatTextArea);
        scrolls.manageScrollsSecondStyle();


        MoveRoot.move(menuButton, menuRoot);
        MoveRoot.move(contentBar, contentRoot);

        chatBlock.prefWidthProperty().bind(chatScroll.widthProperty().subtract(25));
        editIndicator.maxHeightProperty().bind(editButton.heightProperty().subtract(15));
    }

    private void focusCurrentOrderOnListUpdate() {
        ordersList.getItems().addListener((ListChangeListener<Order>)c -> {
            if(currentOrder != null){
                ordersList.getSelectionModel().select(currentOrder);
            }
        });
    }

    @Override
    public void setListsFactories() {
        super.setListsFactories();

        chatUsersList.setCellFactory(chatUsersList -> new ChatsUsersListViewCellSecond());
        ordersList.setCellFactory(param -> new ListCell<Order>(){
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Order" + item.getId().get());
                }
            }
        });
    }

    private void setClips() {
        Rectangle profileViewClip = new Rectangle();
        profileViewClip.widthProperty().bind(profileView.widthProperty());
        profileViewClip.heightProperty().bind(profileView.heightProperty());
        profileView.setClip(profileViewClip);

        Circle clip = new Circle(30.8, 30.8, 30.8);
        profileImageClip.setClip(clip);

        Rectangle chatsClip = new Rectangle(211, 421);
        userChatsClip.setClip(chatsClip);

        Rectangle notificationClip = new Rectangle();
        notificationClip.setArcHeight(33);
        notificationClip.setArcWidth(33);
        notificationClip.heightProperty().bind(notificationsList.heightProperty());
        notificationClip.widthProperty().bind(notificationsList.widthProperty());
        notificationsList.setClip(notificationClip);
    }

    private void setNotificationIcon() {
        SVGPath usb3 = new SVGPath();
        usb3.setContent("m434.753906 360.8125c-32.257812-27.265625-50.753906-67.117188-50.753906-109.335938v-59.476562c0-75.070312-55.765625-137.214844-128-147.625v-23.042969c0-11.796875-9.558594-21.332031-21.332031-21.332031-11.777344 0-21.335938 9.535156-21.335938 21.332031v23.042969c-72.253906 10.410156-128 72.554688-128 147.625v59.476562c0 42.21875-18.496093 82.070313-50.941406 109.503907-8.300781 7.105469-13.058594 17.429687-13.058594 28.351562 0 20.589844 16.746094 37.335938 37.335938 37.335938h352c20.585937 0 37.332031-16.746094 37.332031-37.335938 0-10.921875-4.757812-21.246093-13.246094-28.519531zm0 0");

        SVGPath usb4 = new SVGPath();
        usb4.setContent("m234.667969 512c38.632812 0 70.953125-27.542969 78.378906-64h-156.757813c7.421876 36.457031 39.742188 64 78.378907 64zm0 0");
        Shape s = Shape.union(usb3,usb4);
        s.setFill(Paint.valueOf("FC3903"));

        HBox notificationIcon = (HBox)this.notificationIcon;

        notificationRegion.setShape(s);
        notificationRegion.minWidthProperty().bind(updateButton.widthProperty().subtract(68));
        notificationRegion.prefWidthProperty().bind(updateButton.widthProperty().subtract(68));
        notificationRegion.setMaxSize(14, 14);
        notificationRegion.minHeightProperty().bind(notificationRegion.widthProperty());
        notificationRegion.prefHeightProperty().bind(notificationRegion.widthProperty());
        notificationRegion.setStyle("-fx-background-color: #FC3903");
        notificationRegion.getStyleClass().add("shadow");

        notificationIcon.setAlignment(Pos.CENTER);
        notificationIcon.prefWidthProperty().bind(updateButton.widthProperty().subtract(57));
        notificationIcon.prefHeightProperty().bind(updateButton.widthProperty().subtract(57));
        notificationIcon.setMaxSize(25, 25);
        notificationIcon.setTranslateX(40);
        notificationIcon.setTranslateY(-8);
        notificationIcon.setStyle("-fx-background-radius: 5em;" + "-fx-background-color: #1a1a1a;" + "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,1) , 6.5, 0.4 , 0 , 0 )");

        RotateTransition tt = new RotateTransition(Duration.millis(200), notificationIcon);
        tt.setByAngle(16);
        RotateTransition tl = new RotateTransition(Duration.millis(200), notificationIcon);
        tl.setByAngle(-32);
        PauseTransition pauseTransition = new PauseTransition(Duration.millis(500));

        SequentialTransition sequentialTransition = new SequentialTransition(pauseTransition, tt, tl);
        sequentialTransition.setCycleCount(Timeline.INDEFINITE);
        sequentialTransition.setAutoReverse(true);
        sequentialTransition.play();

    }

    @FXML
    public void showChatView(){
        displayView(chatView);
    }
    @FXML
    public void showOrderView(){
        displayView(orderView);
    }
    @FXML
    public void showCreateView(){
        displayView(createView);
    }

    private void displayView(AnchorPane requestedView){
        if(requestedView.equals(currentView)){
            contentRoot.setOpacity(0);
            contentRoot.setDisable(true);
            requestedView.setOpacity(0);
            requestedView.setDisable(true);

            currentView = null;
        }else if(currentView == null) {
            requestedView.setDisable(false);
            requestedView.setOpacity(1);

            contentRoot.setOpacity(1);
            contentRoot.setDisable(false);
            currentView = requestedView;
        }else{
            requestedView.setDisable(false);
            requestedView.setOpacity(1);

            currentView.setDisable(true);
            currentView.setOpacity(0);
            currentView = requestedView;
        }
    }

    @FXML
    public void onNotificationClick(){
        notificationsList.getItems().remove(notificationsList.getFocusModel().getFocusedItem());
    }

    @FXML public void expandMenu(){
        if(menuButtonsContainer.getChildren().size() == 1){
            menuButtonsContainer.getChildren().add(0, menuButtons);
        }
        TransitionResizeWidth expand = new TransitionResizeWidth(Duration.millis(700), menu, 518);
        expand.play();
    }
    @FXML
    public void reverseMenu(){
        if(currentMenuView == null) {
            TransitionResizeWidth reverse = new TransitionResizeWidth(Duration.millis(700), menu, 38.5);
            reverse.play();
            menuButtonsContainer.getChildren().remove(menuButtons);
        }
    }
    private void expandMenuContent(){
        TransitionResizeHeight expand = new TransitionResizeHeight(Duration.millis(800), menuContent, menuContent.getMaxHeight());
        expand.play();
    }
    private void reverseMenuContent(){
        TransitionResizeHeight reverse = new TransitionResizeHeight(Duration.millis(800), menuContent, 0);
        reverse.play();
    }
    @Override
    public void setStage() throws Exception{
        super.setStage();

//        ObservableList<Chat> chats = FXCollections.observableArrayList(getChats());
//        setChatValues(chats);
//        userChats.setItems(chats);

        menuRoot.setLayoutX((primaryScreenBounds.getWidth() - menuRoot.getWidth()) / 2);
        menuRoot.setLayoutY(contentRoot.getLayoutY() - 60);
    }

    @Override
    public void resetStage(){
        super.resetStage();

        orderView.getStyleClass().add("inactive");

        chatValue = null;
        chatTextArea.setText(null);
        menuSearch.setText("");

        chatBlock.getChildren().remove(1,chatBlock.getChildren().size());

        chatContainer.setDisable(true);
        chatContainer.setOpacity(0);

        contentRoot.setOpacity(0);
        contentRoot.setDisable(true);

        menuContent.setDisable(true);

        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);
        }

        currentMenuView = null;
        currentView = null;

        expandMenu();
        reverseMenuContent();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), profileImageContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.play();
    }

    @FXML
    public void showProfile(){
        if(menuContent.isDisabled()) {
            expandMenuContent();
            currentMenuView = profileView;

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
                reverseMenu();
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
                reverseMenu();
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
    public void focus(MouseEvent event){
        Button button = (Button) event.getSource();
        AnchorPane.setTopAnchor(button, -5.5);
        AnchorPane.setBottomAnchor(button, -4.0);
    }
    @FXML
    public void unFocus(MouseEvent event){
        Button button = (Button) event.getSource();
        AnchorPane.setTopAnchor(button, -1.0);
        AnchorPane.setBottomAnchor(button, 0.0);
    }

    @FXML
    private void showOrder(){
        Order order = ordersList.getSelectionModel().getSelectedItem();
        if(currentOrder == null){
            orderView.getStyleClass().remove("inactive");
        }
        bindOrderProperties(order);

        if(!currentView.equals(orderView)){
            currentView.setOpacity(0);
            currentView.setDisable(true);

            orderView.setOpacity(1);
            orderView.setDisable(false);
            currentView = orderView;
        }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), orderInfo);
        fadeIn.setFromValue(0.36);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    @Override
    public void bindOrderProperties(Order currentOrder) {
        super.bindOrderProperties(currentOrder);

        dishesCountLabel.textProperty().unbind();
        dishesCountLabel.textProperty().bind(Bindings.concat("Dishes ")
                .concat(Bindings.size(currentDishList.getItems()).asString()));
    }
}
