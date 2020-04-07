package Animations;

import Helpers.ListViews.OrderListViewCell;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class ExpandOrderPane {

    private static double orderWidth;
    private static double orderHeight;
    private static double orderX;

    private static double maxOrderWidth;

    private static double buttonX;
    private static double buttonY;

    private static double mouseY;
    private static double mouseX;

    private static double xButtonRation;
    private static double initialOffsetX;
    private static double initialMouseX;


    private static double translatePaneX;

    public static GridPane dates;
    public static OrderListViewCell cell;
    public static Pane currentContainer;
    public static Pane currentPane;
    public static Button button;
    public static ListView orderList;
    public static ScrollBar scrollBar;
    public static Pane contentRoot;
    public static AnchorPane contentPane, orderPane;

    public static BooleanProperty isButtonExpanded = new SimpleBooleanProperty(false);
    public static BooleanProperty action = new SimpleBooleanProperty(false);
    private static FadeTransition showDates;

    private static TranslateTransition transitionPane;
    private static TranslateTransition transitionButton;
    private static TransitionResizeHeight heightPane;
    private static TransitionResizeWidth widthPane;

    public static void setCurrentOrder(MouseEvent event){
        action.setValue(true);

        orderList.setDisable(true);
        currentPane.setOpacity(0);

        mouseX = event.getScreenX();
        mouseY = event.getScreenY();

        buttonX = button.getLayoutX();
        buttonY = button.getLayoutY();

        orderX = currentPane.getLayoutX();
        orderWidth = currentPane.getWidth();
        orderHeight = currentPane.getHeight();

        double cellLayoutX = cell.getLayoutX();
        translatePaneX = cellLayoutX + orderX + currentContainer.getLayoutX() + 1;

        initialOffsetX = event.getX() - translatePaneX;
        initialMouseX = event.getScreenX();

        maxOrderWidth = orderWidth * 4;
        xButtonRation = currentPane.getWidth() / (button.getLayoutX() + button.getWidth() / 2);

        orderPane.setLayoutX(translatePaneX + contentPane.getLayoutX());
        orderPane.setLayoutY(currentPane.getLayoutY() + contentPane.getLayoutY() + 1);
    }

    public static void setListeners(){
        button.setOnMouseClicked(ExpandOrderPane::buttonPress);

        orderPane.setOnMouseEntered(event -> ResizeRoot.resize = false);
        orderPane.setOnMouseExited(event -> ResizeRoot.resize = true);
        orderPane.addEventFilter(MouseEvent.MOUSE_PRESSED, ExpandOrderPane::panePress);
        orderPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, ExpandOrderPane::moveOrder);

        orderList.setOnMouseReleased(ExpandOrderPane::paneReleased);
        orderList.setOnMouseDragged(ExpandOrderPane::listDrag);

        showDates = new FadeTransition(Duration.millis(500), dates);
        showDates.setFromValue(0);
        showDates.setToValue(1);

        transitionPane = new TranslateTransition(Duration.millis(750), orderPane);
        transitionPane.setToX(0);
        transitionPane.setToY(0);
        transitionButton = new TranslateTransition(Duration.millis(750), button);
        transitionButton.setToX(0);
        transitionButton.setToY(0);
        heightPane = new TransitionResizeHeight(orderPane);
        widthPane = new TransitionResizeWidth(orderPane);
    }

    private static void listDrag(MouseEvent event) {
        if(action.get()) paneDrag(event);
    }

    private static void buttonPress(MouseEvent event){
        if(isButtonExpanded.get()){
            reverseOrder();
        }else{
            expandOrderOnClick();
        }
    }

    private static void panePress(MouseEvent event){
        mouseX = event.getScreenX();
        mouseY = event.getScreenY();
    }

    private static void paneDrag(MouseEvent event) {
        if(!isButtonExpanded.getValue()){
            expandOrderOnDrag(event);
        }else {
            moveOrder(event);
        }
    }
    private static void paneReleased(MouseEvent event) {
        if(!isButtonExpanded.get()) {
            reverseOrder();
        }
    }

    private static void moveOrder(MouseEvent eventDrag){
        orderPane.setTranslateX((orderPane.getTranslateX()) + (eventDrag.getScreenX() - mouseX));
        orderPane.setTranslateY((orderPane.getTranslateY()) + (eventDrag.getScreenY() - mouseY));
        mouseX = eventDrag.getScreenX();
        mouseY = eventDrag.getScreenY();
    }

    public static void expandOrderOnClick(){
        isButtonExpanded.setValue(true);

        TransitionResizeHeight heightPane = new TransitionResizeHeight(Duration.millis(750), orderPane, maxOrderWidth);
        heightPane.play();
        TransitionResizeWidth widthPane = new TransitionResizeWidth(Duration.millis(750), orderPane, maxOrderWidth);
        widthPane.play();

        double expandButtonX = button.getPrefHeight() + (maxOrderWidth - orderWidth) / 15;
        double expandButtonY = button.getPrefWidth() + (maxOrderWidth - orderWidth) / 30;

        double translateButtonX = (maxOrderWidth - expandButtonX) / xButtonRation - buttonX;
        double translateButtonY = maxOrderWidth - expandButtonY - 10.5 - buttonY;

        TranslateTransition translateButton = new TranslateTransition(Duration.millis(750), button);
        translateButton.setToX(translateButtonX);
        translateButton.setToY(translateButtonY);
        translateButton.play();

        TranslateTransition translatePane = new TranslateTransition(Duration.millis(750),orderPane);
        translatePane.setToX(-(maxOrderWidth - orderPane.getWidth()) / 2);
        translatePane.setToY(0);
        translatePane.play();

        showDates.setDelay(Duration.millis(750));
        showDates.play();
    }

    private static void expandOrderOnDrag(MouseEvent eventDrag){

        double fasterExpand = 1.8;
        double expand = (initialMouseX - eventDrag.getScreenX()) * fasterExpand;
        if(initialOffsetX < orderWidth / 2){
            if(expand < 0 ){
                expand = 0;
            }else{
                expand *= -1;
                orderPane.setTranslateX(expand);
            }
        }
        if(expand >= 0){
            orderPane.setPrefWidth(orderWidth);
            orderPane.setPrefHeight(orderHeight);
            button.setTranslateY(0);
            button.setTranslateX(0);
            orderPane.setTranslateX(0);
        }

        if(orderWidth - expand > orderWidth) {

            orderPane.setPrefWidth(orderWidth - expand);
            orderPane.setPrefHeight(orderHeight - expand);

            if (orderPane.getPrefWidth() >= maxOrderWidth) {
                isButtonExpanded.setValue(true);
                orderPane.setPrefWidth(maxOrderWidth);
                orderPane.setPrefHeight(maxOrderWidth);

                mouseX = eventDrag.getScreenX();
                mouseY = eventDrag.getScreenY();

                showDates.setDelay(Duration.ZERO);
                showDates.play();
            }

            double translateButtonY = orderPane.getPrefHeight() - button.getPrefHeight() - 10.5 - buttonY;
            double translateButtonX = (orderPane.getPrefWidth() - button.getPrefWidth()) / xButtonRation - buttonX;

            button.setTranslateX(translateButtonX);
            button.setTranslateY(translateButtonY);

        }

    }
    public static void reverseOrder() {
        isButtonExpanded.setValue(false);
        showDates.stop();
        dates.setOpacity(0);

        int maxDelay = 750;
        double widthRatio = orderPane.getWidth() / maxOrderWidth;
        Duration delay = Duration.millis(widthRatio * maxDelay);


        transitionPane.setDuration(delay);
        transitionPane.play();

        transitionButton.setDuration(delay);
        transitionButton.play();

        heightPane.setAndPlay(delay, orderHeight);
        widthPane.setAndPlay(delay, orderWidth);

        Timeline reAppendOrderInFlow = new Timeline(new KeyFrame(delay, actionEvent -> {
            orderList.setDisable(false);
            currentPane.setOpacity(1);
            action.setValue(false);
        }));
        reAppendOrderInFlow.play();
    }
}
