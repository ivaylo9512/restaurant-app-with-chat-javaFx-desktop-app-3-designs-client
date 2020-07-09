package controllers.secondstyle;

import animations.ResizeStage;
import controllers.base.ChatSession;
import helpers.listviews.ChatsUsersListViewCellSecond;
import helpers.Scrolls;
import helpers.listviews.OrderListViewCellSecond;
import javafx.stage.Stage;
import models.*;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import controllers.base.ControllerLogged;

import static application.RestaurantApplication.*;


public class LoggedSecond extends ControllerLogged {
    @FXML Label dishesCountLabel;
    @FXML Pane orderInfo, orderContainer, orderView, createView, chatView, chatListView;

    private Stage stage = stageManager.secondLoggedStage;
    private Pane currentView;

    private ChatSession mainChatSession;

    private LoggedMenu menuController;

    @FXML
    public void initialize() {
        setListsFactories();
        setCreateGraphicIndicators();
        setListsItems();
        focusCurrentOrderOnListUpdate();

        currentOrder = ordersList.getSelectionModel().selectedItemProperty();
        orderContainer.disableProperty().bind(currentOrder.isNull());

        mainChatSession = new ChatSession(mainChat, mainChatValue, mainChatBlock, mainChatInfo, mainChatTextArea);
        mainChatSession.init();

        Scrolls scrolls = new Scrolls(mainChatScroll, mainChatTextArea);
        scrolls.manageScrollsSecondStyle();

        menuSearch.textProperty().addListener((observable, oldValue, newValue) ->
                userMenu.setAll(searchMenu(newValue.toLowerCase()).values()));

        mainChatBlock.prefWidthProperty().bind(mainChatScroll.widthProperty().subtract(25));

        chatListView.disableProperty().bind(chatView.disabledProperty());
    }

    private void focusCurrentOrderOnListUpdate() {
        ordersList.getItems().addListener((ListChangeListener<Order>)c -> {
            if(currentOrder.get() != null){
                ordersList.getSelectionModel().select(currentOrder.get());
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
        ordersList.setCellFactory(orderListCell -> new OrderListViewCellSecond());
    }

    public void setChatValue(ChatValue chat){
        mainChatValue.set(chat);
    }

    void displayView(Pane requestedView){
        if(requestedView.equals(currentView)){
            requestedView.setDisable(true);
            currentView = null;
        }else if(currentView == null) {
            requestedView.setDisable(false);
            currentView = requestedView;

            stage.toFront();
        }else{
            requestedView.setDisable(false);
            currentView.setDisable(true);
            currentView = requestedView;

            stage.toFront();
        }
    }

    void resetOrder(){
        if(currentOrder.get() != null) {
            unbindOrderProperties();
            resetOrderFields();
            ordersList.getSelectionModel().clearSelection();
        }
    }

    void bindToMenu(LoggedMenu menuController){
        this.menuController = menuController;
        menuController.currentContentButton.addListener((observable, oldValue, newValue) -> {
            if(newValue == null){
                stage.close();
            }else{
                stage.show();
            }
        });
    }

    @Override
    public void adjustStage(double height, double width) throws Exception{
        super.adjustStage(height, width);

        userMenu.setAll(orderManager.userMenu.values());
        mainChatSession.bindChat();
    }

    @Override
    public void resetStage(){
        unbindOrderProperties();
        resetOrderFields();

        if(currentView != null){
            currentView.setDisable(true);
            currentView = null;
        }

        mainChatSession.unBindChat();

        menuList.getItems().clear();
        if(ordersList.getItems().size() > 0) {
            ordersList.scrollTo(0);
        }
        ordersList.getSelectionModel().clearSelection();
        chatUsersList.getSelectionModel().clearSelection();

        menuSearch.setText("");
    }

    @Override
    public void setStage(Stage stage){
        super.setStage(stage);

        resizeStage = new ResizeStage(root, contentRoot, stage);
        resizeStage.addListeners();

        root.prefWidthProperty().bind(stage.widthProperty());
        root.prefHeightProperty().bind(stage.heightProperty());
    }

    @FXML
    private void showOrder(){
        Order order = ordersList.getSelectionModel().getSelectedItem();

        if(!currentView.equals(orderView)){
            menuController.setView("orderButton", null);
        }

        bindOrderProperties(order);

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
