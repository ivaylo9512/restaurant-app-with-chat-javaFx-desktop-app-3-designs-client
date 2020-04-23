package sample;

import Animations.*;
import Helpers.ListViews.ChatsUsersListViewCell;
import Helpers.ListViews.NotificationListViewCell;
import Helpers.ListViews.OrderListViewCell;
import Helpers.Scrolls;
import Models.*;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import sample.base.ControllerLogged;

import static Animations.ExpandOrderPane.*;

public class ControllerLoggedFirstStyle extends ControllerLogged {
    @FXML ScrollPane menuScroll, userInfoScroll, mainChatScroll;
    @FXML VBox mainChatBlock;
    @FXML FlowPane chatInfo;
    @FXML AnchorPane contentPane, mainChat, ordersPane, profileImageContainer, orderContainer, dishesAnchor, createdContainer, updatedContainer;
    @FXML Pane moveBar, profileRoot;
    @FXML TextArea mainChatTextArea;
    @FXML ImageView roleImage;
    @FXML Button expandButton;
    @FXML GridPane dates;

    private ScrollBar ordersScrollBar;
    private ChatValue mainChatValue;

    private Image chefImage;
    private Image waiterImage;

    @FXML
    public void initialize() {
        super.initialize();

        addListeners();
        setClips();
        setOrderPane();

        Scrolls scrolls = new Scrolls(menuScroll, userInfoScroll, chatUsersList,
                mainChatScroll, mainChatTextArea);
        scrolls.manageScrollsFirstStyle();

        chefImage = new Image(getClass().getResourceAsStream("/images/chef-second.png"));
        waiterImage = new Image(getClass().getResourceAsStream("/images/waiter-second.png"));

        ResizeMainChat.addListeners(mainChat);

        contentRoot.setCursor(Cursor.DEFAULT);
        MoveRoot.move(moveBar, contentRoot);
    }

    @Override
    public void setListsFactories(){
        super.setListsFactories();

        chatUsersList.setCellFactory(chatList -> new ChatsUsersListViewCell());
        ordersList.setCellFactory(orderCell -> new OrderListViewCell());
        notificationsList.setCellFactory(menuCell -> new NotificationListViewCell());
    }

    private void addListeners() {
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
            scrollBar.setValue(scrollPosition);
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
        super.setStage();

        if (roleField.getText().equals("Chef")) {
            roleImage.setImage(chefImage);
        } else {
            roleImage.setImage(waiterImage);
        }

//        List<Chat> chats = getChats();
//        appendChats(chats);
    }

    @Override
    public void resetStage(){
        super.resetStage();

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
        ExpandOrderPane.scrollBar = ordersScrollBar;
        ExpandOrderPane.orderList = ordersList;
        ExpandOrderPane.dates = dates;

        ExpandOrderPane.setListeners();
    }

    private void expandOrder(MouseEvent event) {
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        String type = intersectedNode.getTypeSelector();
        if(type.equals("Button") || (!ExpandOrderPane.action.get() && type.equals("AnchorPane"))){

            currentPane = type.equals("AnchorPane") ? (AnchorPane) intersectedNode
                    : (AnchorPane) intersectedNode.getParent();
            currentContainer = (Pane)currentPane.getParent();
            cell =  (OrderListViewCell) currentContainer.getParent();

            ExpandOrderPane.setCurrentOrder(event);
            currentOrder = cell.order;
            bindOrderProperties(currentOrder);

            if(intersectedNode.getTypeSelector().equals("Button"))
                expandOrderOnClick();

        }
    }
}