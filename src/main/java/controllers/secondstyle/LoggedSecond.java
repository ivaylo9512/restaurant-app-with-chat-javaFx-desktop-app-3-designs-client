package controllers.secondstyle;

import animations.MoveRoot;
import animations.ResizeRoot;
import controllers.base.ChatSession;
import controllers.base.Controller;
import helpers.listviews.ChatsUsersListViewCellSecond;
import helpers.Scrolls;
import javafx.stage.Stage;
import models.*;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import controllers.base.ControllerLogged;

import static application.RestaurantApplication.*;


public class LoggedSecond extends ControllerLogged implements Controller {
    @FXML Label dishesCountLabel;

    @FXML AnchorPane  orderInfo, orderView,
            chatView, userChatsClip, createView, dishesContainer;
    @FXML Pane contentBar;

    private Stage stage = stageManager.secondLoggedStage;
    private AnchorPane currentView;


    @FXML
    public void initialize() {
        setClips();
        setListsFactories();
        setCreateGraphicIndicators();
        setListsItems();
        focusCurrentOrderOnListUpdate();

        ChatSession mainChatSession = new ChatSession(mainChat, mainChatValue, mainChatBlock, mainChatInfo, mainChatTextArea);

        Scrolls scrolls = new Scrolls(mainChatScroll, mainChatTextArea);
        scrolls.manageScrollsSecondStyle();

        menuSearch.textProperty().addListener((observable, oldValue, newValue) ->
                userMenu.setAll(searchMenu(newValue.toLowerCase()).values()));

        MoveRoot.move(contentBar, contentRoot);
        ResizeRoot.addListeners(contentRoot);

        mainChatBlock.prefWidthProperty().bind(mainChatScroll.widthProperty().subtract(25));
    }

    private void focusCurrentOrderOnListUpdate() {
        ordersList.getItems().addListener((ListChangeListener<Order>)c -> {
            if(currentOrder != null){
                ordersList.getSelectionModel().select(currentOrder);
            }
        });
    }

    private void setListsItems() {
        ordersList.setItems(orderManager.orders);
        newOrderList.setItems(orderManager.newOrderList);
        menuList.setItems(userMenu);
        chatUsersList.setItems(chatManager.chatsList);
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
        Rectangle chatsClip = new Rectangle(211, 421);
        userChatsClip.setClip(chatsClip);
    }

    public void setChatValue(ChatValue chat){
        mainChatValue.set(chat);
    }

    void displayView(AnchorPane requestedView){
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

            stage.toFront();
        }else{
            requestedView.setDisable(false);
            requestedView.setOpacity(1);

            currentView.setDisable(true);
            currentView.setOpacity(0);
            currentView = requestedView;

            stage.toFront();
        }
    }

    @Override
    public void setStage() {
        userMenu.setAll(orderManager.userMenu.values());

        setContentRoot();
    }

    @Override
    public void resetStage(){
        if(currentOrder != null) orderView.getStyleClass().add("inactive");
        unbindOrderProperties();

        menuList.getItems().clear();
        if(ordersList.getItems().size() > 0) ordersList.scrollTo(0);
        ordersList.getSelectionModel().clearSelection();


        mainChatValue.set(null);

        mainChatTextArea.setText(null);
        menuSearch.setText("");

        mainChatBlock.getChildren().remove(1,mainChatBlock.getChildren().size());

        mainChat.setDisable(true);
        mainChat.setOpacity(0);

        contentRoot.setOpacity(0);
        contentRoot.setDisable(true);

        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);
        }
        currentView = null;
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

        dishesCountLabel.textProperty().bind(Bindings.concat("Dishes ")
                .concat(Bindings.size(currentDishList.getItems()).asString()));
    }
}
