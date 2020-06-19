package controllers.firststyle;

import animations.*;
import controllers.base.ChatSession;
import controllers.base.Controller;
import controllers.base.ControllerAdjustable;
import helpers.listviews.ChatsUsersListViewCell;
import helpers.listviews.NotificationListViewCell;
import helpers.listviews.OrderListViewCell;
import helpers.Scrolls;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.input.*;
import javafx.stage.Stage;
import models.*;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import controllers.base.ControllerLogged;

import static application.RestaurantApplication.*;

public class LoggedFirst extends ControllerLogged implements ControllerAdjustable {
    @FXML ScrollPane menuScroll, userInfoScroll;
    @FXML AnchorPane contentPane, ordersPane, profileImageContainer, orderContainer, dishesAnchor, createdContainer, updatedContainer;
    @FXML Pane moveBar, profileRoot;
    @FXML ImageView roleImage;
    @FXML Button expandButton;
    @FXML GridPane dates;

    private ScrollBar ordersScrollBar;

    private Image chefImage = new Image(getClass().getResourceAsStream("/images/chef-second.png"));
    private Image waiterImage = new Image(getClass().getResourceAsStream("/images/waiter-second.png"));

    private ObjectProperty<ChatsUsersListViewCell> mainUserChatCell = new SimpleObjectProperty<>();
    private ObjectProperty<ChatsUsersListViewCell> secondUserChatCell = new SimpleObjectProperty<>();

    private ChatSession mainChatSession, secondChatSession;
    private ExpandOrderPane expandOrderPane = new ExpandOrderPane();
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

        currentOrder = ordersList.getSelectionModel().selectedItemProperty();

        mainChatSession = new ChatSession(mainChat, mainChatValue, mainChatBlock, mainChatInfo, mainChatTextArea);
        secondChatSession = new ChatSession(secondChat, secondChatValue, secondChatBlock, secondChatInfo, secondChatTextArea);

        mainChatSession.init();
        secondChatSession.init();

        chatUsersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Scrolls scrolls = new Scrolls(menuScroll, userInfoScroll, chatUsersList,
                mainChatScroll, mainChatTextArea, secondChatScroll, secondChatTextArea);
        scrolls.manageScrollsFirstStyle();

        menuSearch.textProperty().addListener((observable, oldValue, newValue) ->
                userMenu.setAll(searchMenu(newValue.toLowerCase()).values()));

        ResizeMainChat.addListeners(mainChat);
        ResizeMainChat.addListeners(secondChat);

        ResizeRoot.addListeners(root);
        MoveRoot.move(moveBar, root);
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
                expandOrderPane.isOrderListScrolling = true;
            }
        });
        ordersList.addEventHandler(TouchEvent.TOUCH_RELEASED, event -> expandOrderPane.isOrderListScrolling = false);
        ordersList.addEventFilter(MouseEvent.MOUSE_PRESSED, expandOrderPane::expandOrder);
        ordersList.skinProperty().addListener((observable, oldValue, newValue) -> {
            for (Node node: ordersList.lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar) {
                    ScrollBar bar = (ScrollBar) node;
                    if(bar.getOrientation().equals(Orientation.HORIZONTAL)) {
                        ordersScrollBar = (ScrollBar) node;
                        setExpandOrderPane();
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
        if(currentOrder.get() != null && ordersList.isDisabled()) {
            ordersList.scrollTo(currentOrder.get());
            double zeroIndexScroll = (currentOrder.get().getIndex() * (expandOrderPane.cellWidth + 0.40)) / (ordersList.getItems().size() * expandOrderPane.cellWidth + 1);
            double scrollPosition = zeroIndexScroll - (expandOrderPane.cellLayoutX / (ordersList.getItems().size() * expandOrderPane.cellWidth + 1));
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
    public void adjustStage() throws Exception{
        userMenu.setAll(orderManager.userMenu.values());

        mainChatSession.bindChat();
        secondChatSession.bindChat();

        resetStageDimensions();
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
        resetOrderFields();

        menuList.getItems().clear();
        if(notificationsList.getItems().size() > 0) notificationsList.scrollTo(0);
        if(ordersList.getItems().size() > 0) ordersList.scrollTo(0);
        ordersList.getSelectionModel().clearSelection();

        notificationsView.setOpacity(0);
        notificationsView.setDisable(true);

        mainChatSession.unBindChat();
        secondChatSession.unBindChat();

        mainChat.setLayoutX(217);
        mainChat.setLayoutY(231);
        mainChat.setPrefHeight(189);

        secondChat.setLayoutX(217);
        secondChat.setLayoutY(231);
        secondChat.setPrefHeight(189);

        userInfoScroll.setVvalue(0);
        menuScroll.setVvalue(0);

        userInfoScroll.setDisable(false);
        ordersList.setDisable(false);

        profileRoot.setOpacity(0);
        profileRoot.setDisable(true);

        ordersPane.setDisable(false);
        ordersPane.setOpacity(1);

        ResizeRoot.resize = true;

        if(expandOrderPane.isButtonExpanded.get()){
            expandOrderPane.reverseOrder();
        }
    }

    @Override
    public void setStage(Stage stage){
        this.stage = stage;
    }

    public void setChatValue(ChatValue chat, ChatsUsersListViewCell cell){
        ObjectProperty<ChatValue> valueProperty = mainChatValue;
        ObjectProperty<ChatsUsersListViewCell> userChatCell = mainUserChatCell;
        if((chat == secondChatValue.get()) || mainChatValue.get() != null && mainChatValue.get() != chat){
            valueProperty = secondChatValue;
            userChatCell = secondUserChatCell;
            if(secondChatValue.get() != null && chat != secondChatValue.get())
                secondUserChatCell.get().getStyleClass().remove("selected");
        }

        if(valueProperty.get() == chat){
            userChatCell.get().getStyleClass().remove("selected");
            userChatCell.set(null);
            valueProperty.set(null);
        }else{
            cell.getStyleClass().add("selected");
            userChatCell.set(cell);
            valueProperty.set(chat);
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

        orderContainer.disableProperty().bind(expandOrderPane.isButtonExpanded.not());
        orderContainer.opacityProperty().bind(Bindings.createIntegerBinding(()->{
            if(expandOrderPane.action.get()){
                return 1;
            }
            return 0;
        },expandOrderPane.action));
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

    private void setExpandOrderPane() {
        expandOrderPane.setControllerFields(this, contentRoot, orderContainer,
                expandButton, contentPane, ordersList, dates);

        expandOrderPane.setListeners();
    }

    public void setOrder(Order order) {
        bindOrderProperties(order);
    }

    public void updateExpandOrder(AnchorPane orderPane, Pane container, OrderListViewCell orderCell) {
        expandOrderPane.currentPane = orderPane;
        expandOrderPane.currentContainer = container;
        expandOrderPane.cell = orderCell;

    }
}