package sample;

import Animations.MoveRoot;
import Animations.ResizeRoot;
import Helpers.Scrolls;
import Models.*;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sample.base.ControllerLogged;
import java.util.*;

import static Application.RestaurantApplication.*;

public class ControllerLoggedThirdStyle extends ControllerLogged {
    @FXML public ListView<Order> ordersList;
    @FXML public ListView<TextField> notificationsList;
    @FXML public ListView<Chat> chatsList;
    @FXML public AnchorPane profileView, ordersView, chatsView, ordersMenu, chatsMenu, createRoot, menuBar, mainChatContainer, secondChatContainer;
    @FXML public Pane profileImageContainer, profileImageClip;
    @FXML VBox chatsContainer, mainChatBlock, secondChatBlock;
    @FXML ScrollPane mainChatScroll, secondChatScroll;
    @FXML TextArea mainChatTextArea, secondChatTextArea;
    @FXML HBox notificationsInfo;
    @FXML Text chatsBtn;

    private Map<Integer, ChatValue> chatsMap = new HashMap<>();

    private AnchorPane currentView, currentMenu;
    private Text currentText;
    private Order currentOrder;

    private ChatValue mainChat;
    private ChatValue secondChat;

    @Override
    public void initialize(){
        super.initialize();

        Scrolls scrolls = new Scrolls(mainChatScroll, secondChatScroll, mainChatTextArea, secondChatTextArea);
        scrolls.manageScrollsThirdStyle();

        Rectangle clip = new Rectangle();
        clip.heightProperty().bind(createRoot.heightProperty());
        clip.widthProperty().bind(createRoot.widthProperty());
        createRoot.setClip(clip);

        Circle profileClip = new Circle(40.5, 40.5, 40.5);
        profileImageClip.setClip(profileClip);

        chatsContainer.getChildren().remove(mainChatContainer);
        chatsContainer.getChildren().remove(secondChatContainer);

        mainChatBlock.prefWidthProperty().bind(mainChatScroll.widthProperty().subtract(16));
        secondChatBlock.prefWidthProperty().bind(secondChatScroll.widthProperty().subtract(16));

        MoveRoot.move(menuBar, contentRoot);
        ResizeRoot.addListeners(contentRoot);
    }

    @Override
    public void setStage() throws Exception{
        super.setStage();

        loginAnimation();
    }

    @Override
    public void resetStage(){
        super.resetStage();

        mainChat = null;
        secondChat = null;
        mainChatTextArea.setText(null);
        secondChatTextArea.setText(null);
        menuSearch.setText("");

        menuBar.setOpacity(0);

        mainChatBlock.getChildren().remove(1, mainChatBlock.getChildren().size());
        secondChatBlock.getChildren().remove(1, secondChatBlock.getChildren().size());

        chatsList.getItems().clear();
        ordersList.getItems().clear();
        notificationsList.getItems().clear();

        if(currentText != null){
            currentText.getStyleClass().remove("strikethrough");
        }
        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);
        }
        if(currentMenu != null){
            currentMenu.setOpacity(0);
            currentMenu.setDisable(true);
        }

        currentView = null;
        currentMenu= null;
    }

    private void loginAnimation() {
        menuBar.setTranslateX(contentRoot.getPrefWidth() / 2 - menuBar.getPrefWidth() / 2);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(700), menuBar);
        fadeIn.setDelay(Duration.millis(1000));
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    @FXML
    public void displayOrdersView(MouseEvent event){
        Text clickedText = (Text)event.getSource();
        setStrikeThrough(clickedText);
        displayView(ordersView, ordersMenu);
    }
    @FXML
    public void displayProfileView(MouseEvent event){
        Text clickedText = (Text)event.getSource();
        setStrikeThrough(clickedText);
        displayView(profileView, chatsMenu);
    }
    @FXML
    public void displayChatsView(MouseEvent event){
        Text clickedText = (Text)event.getSource();
        setStrikeThrough(clickedText);
        displayView(chatsView, chatsMenu);
    }

    private void setStrikeThrough(Text clickedText) {
        clickedText.getStyleClass().add("strikethrough");
        if(currentText != null) {
            currentText.getStyleClass().remove("strikethrough");
        }
        currentText = clickedText;
    }

    private void displayView(AnchorPane requestedView, AnchorPane requestedMenu){
        Timeline delayView = new Timeline(new KeyFrame(Duration.millis(1), event -> {
            if (currentMenu != null) {
                currentMenu.setOpacity(0);
                currentMenu.setDisable(true);
            }

            requestedMenu.setOpacity(1);
            requestedMenu.setDisable(false);
            requestedView.setOpacity(1);
            requestedView.setDisable(false);

            currentMenu = requestedMenu;
            currentView = requestedView;
        }));
        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);

            delayView.play();
        }else{
            animateMenuBar();

            delayView.setDelay(Duration.millis(1000));
            delayView.play();
        }
    }

    private void animateMenuBar() {
        TranslateTransition animateBar = new TranslateTransition(Duration.millis(1000), menuBar);
        animateBar.setToX(0);
        animateBar.play();
    }

    public void showOrder(){
        Order selectedOrder = ordersList.getSelectionModel().getSelectedItem();
        if(selectedOrder != null){
            currentOrder = loggedUser.getOrders().get(selectedOrder.getId());
            currentOrder.getDishes().forEach(dish -> dish.setOrderId(currentOrder.getId()));

            dishesList.setItems(FXCollections.observableArrayList(currentOrder.getDishes()));

        }
    }

    private void addNotification(String notification) {
        notificationsInfo.setOpacity(0);
        notificationsInfo.setDisable(true);

        TextField textField = new TextField(notification);
        textField.setEditable(false);
        textField.setDisable(true);

        notificationsList.getItems().add(0, textField);
        notificationSound.play();
    }
    @FXML
    public void removeNotification(){
        notificationsList.getItems().remove(notificationsList.getFocusModel().getFocusedItem());
        if(notificationsList.getItems().size() == 0){
            notificationsInfo.setOpacity(1);
            notificationsInfo.setDisable(false);
        }
    }
}
