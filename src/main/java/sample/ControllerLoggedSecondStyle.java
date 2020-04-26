package sample;

import Animations.MoveRoot;
import Helpers.ListViews.ChatsUsersListViewCellSecond;
import Helpers.Scrolls;
import Models.*;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import sample.base.ControllerLogged;


public class ControllerLoggedSecondStyle extends ControllerLogged {
    @FXML Label dishesCountLabel;

    @FXML AnchorPane  orderInfo, orderView,
            chatView, userChatsClip, createView, dishesContainer, chatContainer;
    @FXML Pane contentBar;
    @FXML TextArea chatTextArea;
    @FXML ScrollPane chatScroll;
    @FXML VBox chatBlock;

    private AnchorPane currentView, currentMenuView;

    private ChatValue chatValue;

    @Override
    public void initialize() {
        super.initialize();

        setClips();
        focusCurrentOrderOnListUpdate();

        Scrolls scrolls = new Scrolls(chatScroll, chatTextArea);
        scrolls.manageScrollsSecondStyle();

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
        Rectangle chatsClip = new Rectangle(211, 421);
        userChatsClip.setClip(chatsClip);

        Rectangle notificationClip = new Rectangle();
        notificationClip.setArcHeight(33);
        notificationClip.setArcWidth(33);
        notificationClip.heightProperty().bind(notificationsList.heightProperty());
        notificationClip.widthProperty().bind(notificationsList.widthProperty());
        notificationsList.setClip(notificationClip);
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
        }else{
            requestedView.setDisable(false);
            requestedView.setOpacity(1);

            currentView.setDisable(true);
            currentView.setOpacity(0);
            currentView = requestedView;
        }
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


        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);
        }

        currentMenuView = null;
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

        dishesCountLabel.textProperty().unbind();
        dishesCountLabel.textProperty().bind(Bindings.concat("Dishes ")
                .concat(Bindings.size(currentDishList.getItems()).asString()));
    }
}
