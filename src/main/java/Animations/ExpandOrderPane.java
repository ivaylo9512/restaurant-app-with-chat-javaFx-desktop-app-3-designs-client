package Animations;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
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


    private static Pane orderContainer;
    private static Pane currentOrder;
    private static AnchorPane dishesAnchor;
    private static GridPane dates;

    public static Button button;
    public static ListView orderList;
    public static ScrollBar scrollBar;
    public static Pane contentRoot;
    public static AnchorPane contentPane, orderPane;
    public static boolean action = false;

    private static BooleanProperty buttonExpanded = new SimpleBooleanProperty(false,"buttonExpanded");
    public static BooleanProperty buttonExpandedProperty(){
        return buttonExpanded;
    }

    public static void setCurrentOrder(MouseEvent event){
        orderContainer = (Pane) event.getPickResult().getIntersectedNode();
        currentOrder = (Pane) orderContainer.getChildren().get(0);
        action = true;

        mouseX = event.getScreenX();
        mouseY = event.getScreenY();

        buttonX = button.getLayoutX();
        buttonY = button.getLayoutY();

        orderX = currentOrder.getLayoutX();
        orderWidth = currentOrder.getWidth();
        orderHeight = currentOrder.getHeight();

        Node cell = orderContainer.getParent();
        double cellLayoutX = cell.getLayoutX();
        double translatePaneX = cellLayoutX + currentOrder.getLayoutX() + orderContainer.getLayoutX() + 1;

        maxOrderWidth = orderWidth * 4;
        xButtonRation = currentOrder.getWidth() / (button.getLayoutX() + button.getWidth() / 2);

        initialOffsetX = event.getX() - translatePaneX;
        initialMouseX = event.getScreenX();

        orderList.setDisable(true);

        orderPane.setLayoutX(translatePaneX + contentPane.getLayoutX());
        orderPane.setLayoutY(currentOrder.getLayoutY() + contentPane.getLayoutY() + 1);

    }

    public static void setListeners(){
        button.setOnMouseClicked(ExpandOrderPane::buttonPress);

        orderPane.setOnMouseEntered(event -> ResizeRoot.resize = false);
        orderPane.setOnMouseExited(event -> ResizeRoot.resize = true);
        orderPane.setOnMousePressed(ExpandOrderPane::panePress);
        orderPane.setOnDragDetected(ExpandOrderPane::paneDrag);
        orderPane.setOnMouseReleased(ExpandOrderPane::paneReleased);
    }

    private static void buttonPress(MouseEvent event){
        if(buttonExpanded.getValue()){
            reverseOrder();
        }else{
            expandOrderOnClick();
        }
    };

    private static void panePress(MouseEvent event){

        if(initial){
            action = true;
            initial = false;

            initialOffsetX = event.getX() - translatePaneX;
            initialMouseX = event.getScreenX();

            orderList.setDisable(true);
            currentOrder.setOpacity(0);

        }
        mouseX = event.getScreenX();
        mouseY = event.getScreenY();
    };

    private static void paneDrag(MouseEvent event) {
        if(!buttonExpanded.getValue()){
            expandOrderOnDrag(event);
        }else {
            moveOrder(event);
        }
    };
    private static void paneReleased(MouseEvent event) {
        if(!buttonExpanded.getValue()) {
            reverseOrder();
        }
    };

    private static void moveOrder(MouseEvent eventDrag){
        orderPane.setTranslateX((orderPane.getTranslateX()) + (eventDrag.getScreenX() - mouseX));
        orderPane.setTranslateY((orderPane.getTranslateY()) + (eventDrag.getScreenY() - mouseY));
        mouseX = eventDrag.getScreenX();
        mouseY = eventDrag.getScreenY();
    }

    private static void expandOrderOnClick(){
        buttonExpanded.setValue(true);
        action = true;
        orderList.setDisable(true);
        dishesAnchor.setDisable(false);
        dishesAnchor.setOpacity(1);

        TransitionResizeHeight heightPane = new TransitionResizeHeight(Duration.millis(750),currentOrder, maxOrderWidth);
        heightPane.play();
        TransitionResizeWidth widthPane = new TransitionResizeWidth(Duration.millis(750),currentOrder, maxOrderWidth);
        widthPane.play();

        double expandButtonX = button.getPrefHeight() + (maxOrderWidth - orderWidth) / 15;
        double expandButtonY = button.getPrefWidth() + (maxOrderWidth - orderWidth) / 30;

        double translateButtonX = (maxOrderWidth - expandButtonX) / xButtonRation - buttonX;
        double translateButtonY = maxOrderWidth - expandButtonY - 10.5 - buttonY;

        TranslateTransition translateButton = new TranslateTransition(Duration.millis(750), button);
        translateButton.setToX(translateButtonX);
        translateButton.setToY(translateButtonY);
        translateButton.play();

        TranslateTransition translatePane = new TranslateTransition(Duration.millis(750),currentOrder);
        translatePane.setToX(-(maxOrderWidth - currentOrder.getWidth()) / 2);
        translatePane.setToY(0);
        translatePane.play();

        FadeTransition showDates = new FadeTransition(Duration.millis(500), dates);
        showDates.setFromValue(0);
        showDates.setToValue(1);
        showDates.setDelay(Duration.millis(750));
        showDates.play();

//        dates.setDisable(false);

    }

    private static void expandOrderOnDrag(MouseEvent eventDrag){
//        dishesAnchor.setDisable(false);
//        dishesAnchor.setOpacity(1);

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
                orderPane.setPrefWidth(maxOrderWidth);
                orderPane.setPrefHeight(maxOrderWidth);

                buttonExpanded.setValue(true);
                mouseX = eventDrag.getScreenX();
                mouseY = eventDrag.getScreenY();

                FadeTransition showDates = new FadeTransition(Duration.millis(500), dates);
                showDates.setFromValue(0);
                showDates.setToValue(1);
                showDates.setDelay(Duration.millis(750));
                showDates.play();

//                dates.setDisable(false);
            }

            double translateButtonY = orderPane.getPrefHeight() - button.getPrefHeight() - 10.5 - buttonY;
            double translateButtonX = (orderPane.getPrefWidth() - button.getPrefWidth()) / xButtonRation - buttonX;

            button.setTranslateX(translateButtonX);
            button.setTranslateY(translateButtonY);

        }

    }
    public static void reverseOrder() {
        Timeline delay = new Timeline(new KeyFrame(Duration.millis(20), actionEvent1 -> {
            dates.setDisable(true);
            dates.setOpacity(0);

            buttonExpanded.setValue(false);

            TranslateTransition transitionPane = new TranslateTransition(Duration.millis(750), orderPane);
            transitionPane.setToX(0);
            transitionPane.setToY(0);
            transitionPane.play();

            TranslateTransition transitionButton = new TranslateTransition(Duration.millis(750), button);
            transitionButton.setToX(0);
            transitionButton.setToY(0);
            transitionButton.play();

            TransitionResizeHeight heightPane = new TransitionResizeHeight(Duration.millis(750), orderPane, orderHeight);
            heightPane.play();
            TransitionResizeWidth widthPane = new TransitionResizeWidth(Duration.millis(750), orderPane, orderWidth);
            widthPane.play();

            Timeline reAppendOrderInFlow = new Timeline(new KeyFrame(Duration.millis(750), actionEvent -> {
                orderList.setDisable(false);
                currentOrder.setOpacity(1);

                dishesAnchor.setDisable(true);
                dishesAnchor.setOpacity(0);
                action = false;
            }));
            reAppendOrderInFlow.play();
        }));
        delay.play();
    }
}
