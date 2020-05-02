package controllers.firststyle;

import animations.*;
import controllers.base.Controller;
import helpers.listviews.ChatsUsersListViewCell;
import helpers.listviews.NotificationListViewCell;
import helpers.listviews.OrderListViewCell;
import helpers.Scrolls;
import javafx.event.Event;
import javafx.scene.input.*;
import models.*;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import controllers.base.ControllerLogged;

import static animations.ExpandOrderPane.*;
import static application.RestaurantApplication.*;

public class LoggedFirst extends ControllerLogged implements Controller{
    @FXML ScrollPane menuScroll, userInfoScroll, mainChatScroll;
    @FXML AnchorPane contentPane, mainChat, ordersPane, profileImageContainer, orderContainer, dishesAnchor, createdContainer, updatedContainer;
    @FXML Pane moveBar, profileRoot;
    @FXML TextArea mainChatTextArea;
    @FXML ImageView roleImage;
    @FXML Button expandButton;
    @FXML GridPane dates;

    private ScrollBar ordersScrollBar;

    private Image chefImage = new Image(getClass().getResourceAsStream("/images/chef-second.png"));
    private Image waiterImage = new Image(getClass().getResourceAsStream("/images/waiter-second.png"));

    @FXML
    public void initialize() {
        setClips();
        setListsItems();
        setNotificationsListeners();
        setNotificationsFactories();
        setListsFactories();
        setUserFields();
        setOrderPane();
        addOrdersListListeners();

        Scrolls scrolls = new Scrolls(menuScroll, userInfoScroll, chatUsersList,
                mainChatScroll, mainChatTextArea);
        scrolls.manageScrollsFirstStyle();


        menuSearch.textProperty().addListener((observable, oldValue, newValue) ->
                userMenu.setAll(searchMenu(newValue.toLowerCase()).values()));

        ResizeMainChat.addListeners(mainChat);
        ResizeRoot.addListeners(contentRoot);
        MoveRoot.move(moveBar, contentRoot);
    }

    private void setListsItems() {
        ordersList.setItems(orderManager.orders);
        newOrderList.setItems(orderManager.newOrderList);
        notificationsList.setItems(notificationManager.notifications);
        menuList.setItems(userMenu);
        chatUsersList.setItems(chatManager.chatsList);
    }

    @Override
    public void setListsFactories(){
        super.setListsFactories();

        chatUsersList.setCellFactory(chatList -> new ChatsUsersListViewCell());
        ordersList.setCellFactory(orderCell -> new OrderListViewCell());
        notificationsList.setCellFactory(menuCell -> new NotificationListViewCell());
    }

    private void addOrdersListListeners() {
        ordersList.addEventHandler(TouchEvent.TOUCH_PRESSED, event -> {
            if(event.getTarget() instanceof AnchorPane){
                Event.fireEvent(event.getTarget(), new MouseEvent(MouseEvent.MOUSE_PRESSED,
                        event.getTouchPoint().getSceneX(), event.getTouchPoint().getSceneY(), event.getTouchPoint().getScreenX(), event.getTouchPoint().getScreenY(), MouseButton.PRIMARY, 1,
                        true, true, true, true, true, true, true, true, true, true, null));
                Event.fireEvent(ordersPane, new MouseEvent(MouseEvent.MOUSE_PRESSED,
                        event.getTouchPoint().getSceneX(), event.getTouchPoint().getSceneY(), event.getTouchPoint().getScreenX(), event.getTouchPoint().getScreenY(), MouseButton.PRIMARY, 1,
                        true, true, true, true, true, true, true, true, true, true, null));
                ordersList.setDisable(true);
            }else{
                isOrderListScrolling = true;
            }
        });
        ordersList.addEventHandler(TouchEvent.TOUCH_RELEASED, event -> isOrderListScrolling = false);
        ordersList.addEventFilter(MouseEvent.MOUSE_PRESSED, this::expandOrder);
        ordersList.skinProperty().addListener((observable, oldValue, newValue) -> {
            for (Node node: ordersList.lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar) {
                    ScrollBar bar = (ScrollBar) node;
                    if(bar.getOrientation().equals(Orientation.HORIZONTAL)) {
                        ordersScrollBar = (ScrollBar) node;
                        ExpandOrderListeners();
                    }
                }
            }
        });
    }

    private void setClips() {
        Circle clip = new Circle(0, 0, 30);
        clip.setLayoutX(30);
        clip.setLayoutY(30);
        profileImageContainer.setClip(clip);
    }

    public void updateListScroll() {
        if(currentOrder != null && ordersList.isDisabled()) {
            ordersList.scrollTo(currentOrder);
            double zeroIndexScroll = (currentOrder.getIndex() * (cellWidth + 0.40)) / (ordersList.getItems().size() * cellWidth + 1);
            double scrollPosition = zeroIndexScroll - (cellLayoutX / (ordersList.getItems().size() * cellWidth + 1));
            ordersScrollBar.setValue(scrollPosition);
        }
    }

    @FXML
    private void showNotifications() {
        notificationsView.setDisable(false);
        notificationsView.setOpacity(1);

        isNewNotificationChecked.set(true);

        ordersPane.setDisable(true);
        ordersPane.setOpacity(0);
    }

    @FXML
    private void showOrders() {
        notificationsView.setDisable(true);
        notificationsView.setOpacity(0);

        ordersPane.setDisable(false);
        ordersPane.setOpacity(1);
    }

    @FXML
    private void scrollToChats() {
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(1000), new KeyValue(
                        menuScroll.vvalueProperty(), 1)));
        animation.play();
        chatUsersList.setDisable(false);
        userInfoScroll.setDisable(true);
    }

    @FXML
    private void showProfile() {
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(1000), new KeyValue(
                        menuScroll.vvalueProperty(), 0)));
        animation.play();
        profileRoot.setOpacity(1);
        profileRoot.setDisable(false);
        userInfoScroll.setDisable(false);
        chatUsersList.setDisable(true);
    }

    @FXML
    public void showMenu(){
        profileRoot.setOpacity(0);
        profileRoot.setDisable(true);
    }

    @Override
    public void setStage() throws Exception{
        userMenu.setAll(orderManager.userMenu.values());

        setContentRoot();
        if(ordersList.getItems().size() > 0) ordersList.scrollTo(0);
        if(notificationsList.getItems().size() > 0) notificationsList.scrollTo(0);

        if (roleField.getText().equals("Chef")) {
            roleImage.setImage(chefImage);
        } else {
            roleImage.setImage(waiterImage);
        }
    }

    @Override
    public void resetStage(){
        unbindOrderProperties();

        menuList.getItems().clear();
        if(notificationsList.getItems().size() > 0) notificationsList.scrollTo(0);
        if(ordersList.getItems().size() > 0) ordersList.scrollTo(0);
        ordersList.getSelectionModel().clearSelection();

        notificationsView.setOpacity(0);
        notificationsView.setDisable(true);

        mainChatBlock.getChildren().remove(1,mainChatBlock.getChildren().size());

        mainChat.setDisable(true);
        mainChat.setOpacity(0);
        mainChat.setLayoutX(217);
        mainChat.setLayoutY(231);
        mainChat.setPrefHeight(189);

        mainChatTextArea.setText(null);
        mainChatValue = null;

        userInfoScroll.setVvalue(0);
        menuScroll.setVvalue(0);

        userInfoScroll.setDisable(false);
        ordersList.setDisable(false);

        profileRoot.setOpacity(0);
        profileRoot.setDisable(true);

        ordersPane.setDisable(false);
        ordersPane.setOpacity(1);

        ResizeRoot.resize = true;

        if(isButtonExpanded.get()){
            ExpandOrderPane.reverseOrder();
        }
    }

    @Override
    public void setMainChat(ChatValue chat){
        if(chat == mainChatValue){
            mainChat.setOpacity(0);
            mainChat.setDisable(true);
            mainChatValue = null;
            chatUsersList.getSelectionModel().clearSelection();
        }else{
            mainChat.setDisable(false);
            mainChat.setOpacity(0);

            Timeline opacity = new Timeline(new KeyFrame(Duration.millis(200), event -> mainChat.setOpacity(1)));
            opacity.play();

            super.setMainChat(chat);
        }
    }

    public void setOrderPane(){
        Rectangle rect = new Rectangle(orderContainer.getWidth(), orderContainer.getHeight());
        rect.heightProperty().bind(orderContainer.prefHeightProperty());
        rect.widthProperty().bind(orderContainer.prefWidthProperty());
        rect.setArcWidth(20);
        rect.setArcHeight(20);
        rect.getStyleClass().add("shadow");
        orderPane.setClip(rect);

        expandButton.prefWidthProperty().bind(((orderContainer.prefWidthProperty()
                .subtract(81.6))
                .divide(15))
                .add(28));
        expandButton.prefHeightProperty().bind(((orderContainer.prefHeightProperty()
                .subtract(81.6))
                .divide(30))
                .add(28));
        Rectangle dishesClip = new Rectangle();
        dishesClip.widthProperty().bind(dishesAnchor.widthProperty());
        dishesClip.heightProperty().bind(dishesAnchor.heightProperty());
        currentDishList.setClip(dishesClip);

        orderContainer.disableProperty().bind(isButtonExpanded.not());
        orderContainer.opacityProperty().bind(Bindings.createIntegerBinding(()->{
            if(action.get()){
                return 1;
            }
            return 0;
        },action));
    }

    @FXML
    public void showCreated(){
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), createdDate);
        translate.setToX(30);
        translate.play();
    }

    @FXML
    public void hideCreated(){
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), createdDate);
        translate.setToX(0);
        translate.play();
    }

    @FXML
    public void showUpdated(){
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), updatedDate);
        translate.setToX(-30);
        translate.play();
    }

    @FXML
    public void hideUpdated(){
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), updatedDate);
        translate.setToX(0);
        translate.play();
    }

    private void ExpandOrderListeners() {
        ExpandOrderPane.contentRoot = contentRoot;
        ExpandOrderPane.orderPane = orderContainer;
        ExpandOrderPane.button = expandButton;
        ExpandOrderPane.contentPane = contentPane;
        ExpandOrderPane.orderList = ordersList;
        ExpandOrderPane.dates = dates;

        ExpandOrderPane.setListeners();
    }

    private void expandOrder(MouseEvent event) {
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        intersectedNode = intersectedNode == null ? (Node)event.getTarget() : intersectedNode;

        if(!isOrderListScrolling && !ExpandOrderPane.action.get() && (intersectedNode instanceof Button || intersectedNode instanceof AnchorPane)){

            currentPane = intersectedNode instanceof AnchorPane ? (AnchorPane) intersectedNode
                    : (AnchorPane) intersectedNode.getParent();
            currentContainer = (Pane)currentPane.getParent();
            cell =  (OrderListViewCell) currentContainer.getParent();

            ExpandOrderPane.setCurrentOrder(event);
            currentOrder = cell.order;
            bindOrderProperties(currentOrder);

            if(intersectedNode instanceof Button)
                expandOrderOnClick();

        }
    }
}