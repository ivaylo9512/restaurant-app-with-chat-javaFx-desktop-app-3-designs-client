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
    public static AnchorPane contentPane;
    public static boolean action = false;

    private static BooleanProperty buttonExpanded = new SimpleBooleanProperty(false,"buttonExpanded");
    public static BooleanProperty buttonExpandedProperty(){
        return buttonExpanded;
    }

    public static void setCurrentOrder(MouseEvent event){
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        if(intersectedNode instanceof  Button){
            button = (Button) intersectedNode;
            currentOrder = (Pane) button.getParent();
        }else{
            currentOrder = (Pane) intersectedNode;
            button = (Button) currentOrder.getChildren().get(2);
        }
        action = true;

        dishesAnchor = (AnchorPane) currentOrder.getChildren().get(0);
        dates = (GridPane) currentOrder.getChildren().get(1);
        orderContainer = (Pane) currentOrder.getParent();


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

        currentOrder.setLayoutX(translatePaneX + contentPane.getLayoutX());
        currentOrder.setLayoutY(currentOrder.getLayoutY() + contentPane.getLayoutY() + 1);
        currentOrder.setStyle("-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,1.6) , 4, 0.0 , 0 , 0 )");
        contentRoot.getChildren().add(currentOrder);

        if(intersectedNode instanceof Button){
            expandOrderOnClick();
        }

        currentOrder.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseEntered -> ResizeRoot.resize = false);
        currentOrder.addEventFilter(MouseEvent.MOUSE_EXITED, mouseExited -> ResizeRoot.resize = true);
        currentOrder.addEventFilter(MouseEvent.MOUSE_PRESSED, panePress);
        currentOrder.addEventFilter(MouseEvent.MOUSE_DRAGGED, paneDrag);
        currentOrder.addEventFilter(MouseEvent.MOUSE_RELEASED, paneReleased);
    }

    private static EventHandler<MouseEvent> panePress = eventPress -> {
        mouseX = eventPress.getScreenX();
        mouseY = eventPress.getScreenY();
    };

    private static EventHandler<MouseEvent> paneDrag = eventDrag -> {
        if(!buttonExpanded.getValue()){
            expandOrderOnDrag(eventDrag);
        }else {
            moveOrder(eventDrag);
        }
    };
    private static EventHandler<MouseEvent> paneReleased = event1 -> {
        if(!buttonExpanded.getValue()) {
            reverseOrder();
        }
    };

    private static void moveOrder(MouseEvent eventDrag){
        currentOrder.setTranslateX((currentOrder.getTranslateX()) + (eventDrag.getScreenX() - mouseX));
        currentOrder.setTranslateY((currentOrder.getTranslateY()) + (eventDrag.getScreenY() - mouseY));
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

        dates.setDisable(false);

    }

    private static void expandOrderOnDrag(MouseEvent eventDrag){
        dishesAnchor.setDisable(false);
        dishesAnchor.setOpacity(1);

        double fasterExpand = 1.8;
        double expand = (initialMouseX - eventDrag.getScreenX()) * fasterExpand;
        if(initialOffsetX < orderWidth / 2){
            if(expand < 0 ){
                expand = 0;
            }else{
                expand *= -1;
                currentOrder.setTranslateX(expand);
            }
        }
        if(expand >= 0){
            currentOrder.setPrefWidth(orderWidth);
            currentOrder.setPrefHeight(orderHeight);
            button.setTranslateY(0);
            button.setTranslateX(0);
            currentOrder.setTranslateX(0);
        }

        if(orderWidth - expand > orderWidth) {

            currentOrder.setPrefWidth(orderWidth - expand);
            currentOrder.setPrefHeight(orderHeight - expand);

            if (currentOrder.getPrefWidth() >= maxOrderWidth) {
                currentOrder.setPrefWidth(maxOrderWidth);
                currentOrder.setPrefHeight(maxOrderWidth);

                buttonExpanded.setValue(true);
                mouseX = eventDrag.getScreenX();
                mouseY = eventDrag.getScreenY();

                FadeTransition showDates = new FadeTransition(Duration.millis(500), dates);
                showDates.setFromValue(0);
                showDates.setToValue(1);
                showDates.setDelay(Duration.millis(750));
                showDates.play();

                dates.setDisable(false);
            }

            double translateButtonY = currentOrder.getPrefHeight() - button.getPrefHeight() - 10.5 - buttonY;
            double translateButtonX = (currentOrder.getPrefWidth() - button.getPrefWidth()) / xButtonRation - buttonX;

            button.setTranslateX(translateButtonX);
            button.setTranslateY(translateButtonY);

        }

    }
    public static void reverseOrder() {
        Timeline delay = new Timeline(new KeyFrame(Duration.millis(20), actionEvent1 -> {
            dates.setDisable(true);
            dates.setOpacity(0);

            buttonExpanded.setValue(false);

            currentOrder.removeEventFilter(MouseEvent.MOUSE_CLICKED, panePress);
            currentOrder.removeEventFilter(MouseEvent.MOUSE_DRAGGED, paneDrag);
            currentOrder.removeEventFilter(MouseEvent.MOUSE_RELEASED, paneReleased);

            TranslateTransition transitionPane = new TranslateTransition(Duration.millis(750), currentOrder);
            transitionPane.setToX(0);
            transitionPane.setToY(0);
            transitionPane.play();

            TranslateTransition transitionButton = new TranslateTransition(Duration.millis(750), button);
            transitionButton.setToX(0);
            transitionButton.setToY(0);
            transitionButton.play();

            TransitionResizeHeight heightPane = new TransitionResizeHeight(Duration.millis(750), currentOrder, orderHeight);
            heightPane.play();
            TransitionResizeWidth widthPane = new TransitionResizeWidth(Duration.millis(750), currentOrder, orderWidth);
            widthPane.play();

            Timeline reAppendOrderInFlow = new Timeline(new KeyFrame(Duration.millis(750), actionEvent -> {
                currentOrder.setLayoutX(orderX);
                currentOrder.setLayoutY(currentOrder.getLayoutY() - contentPane.getLayoutY() - 1);
                orderContainer.getChildren().add(currentOrder);
                currentOrder.setStyle("");
                orderList.setDisable(false);
                dishesAnchor.setDisable(true);
                dishesAnchor.setOpacity(0);
                action = false;
            }));
            reAppendOrderInFlow.play();
        }));
        delay.play();
    }
}
