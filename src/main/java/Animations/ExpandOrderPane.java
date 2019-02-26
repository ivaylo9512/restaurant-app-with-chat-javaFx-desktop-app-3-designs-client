package Animations;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class ExpandOrderPane {
    private static Pane currentOrder;
    private static Button button;
    private static Label label;

    private static double orderWidth;
    private static double orderHeight;
    private static double buttonWidth;
    private static double buttonHeight;
    private static double labelWidth;
    private static double maxOrderWidth;

    private static double buttonX;
    private static double buttonY;

    private static double mouseY;
    private static double mouseX;

    private static double xButtonRation;
    private static double initialOffsetX;
    private static double initialMouseX;

    public static boolean action = false;
    public static Boolean buttonExpanded = false;

    public static void expandPane(Pane order, Pane orderContainer, double scrolledAmount, MouseEvent event,
                                  double translatePaneX, double translatePaneY, Node intersectedNode, ScrollPane scrollPane){
        currentOrder = order;
        currentOrder.setLayoutX(translatePaneX - scrolledAmount);

        button = (Button) currentOrder.getChildren().get(0);
        label = (Label) currentOrder.getChildren().get(1);

        mouseX = event.getScreenX();
        mouseY = event.getScreenY();
        buttonX = button.getLayoutX();
        buttonY = button.getLayoutY();
        initialOffsetX = event.getX() - translatePaneX + scrolledAmount;
        initialMouseX = event.getScreenX();

        orderWidth = currentOrder.getWidth();
        orderHeight = currentOrder.getHeight();
        buttonWidth = button.getWidth();
        buttonHeight = button.getHeight();
        labelWidth = label.getWidth();
        maxOrderWidth = orderWidth * 4;

        xButtonRation = currentOrder.getWidth() / (button.getLayoutX() + buttonWidth / 2);

        if(intersectedNode.getTypeSelector().equals("Button")){
            buttonExpanded = true;
            action = true;
            scrollPane.setDisable(true);
            button = (Button) intersectedNode;

            ResizeHeight heightPane = new ResizeHeight(Duration.millis(750),order, maxOrderWidth);
            heightPane.play();
            ResizeWidth widthPane = new ResizeWidth(Duration.millis(750),order, maxOrderWidth);
            widthPane.play();

            double expandButton = buttonWidth + (maxOrderWidth - orderWidth) / 15;
            ResizeHeight heightButton = new ResizeHeight(Duration.millis(750),button, expandButton);
            heightButton.play();
            ResizeWidth widthButton = new ResizeWidth(Duration.millis(750),button, expandButton);
            widthButton.play();

            double expandLabel = maxOrderWidth - (orderWidth - labelWidth);
            ResizeWidth widthLabel = new ResizeWidth(Duration.millis(750),label, expandLabel);
            widthLabel.play();

            double translateButtonY = maxOrderWidth - expandButton - 10.5 - buttonY;
            double translateButtonX = (maxOrderWidth - expandButton) / xButtonRation - buttonX;
            TranslateTransition translateButton = new TranslateTransition(Duration.millis(750), button);
            translateButton.setToX(translateButtonX);
            translateButton.setToY(translateButtonY);
            translateButton.play();

            TranslateTransition translatePane = new TranslateTransition(Duration.millis(750),order);
            translatePane.setToX(-(maxOrderWidth - order.getWidth()) / 2);
            translatePane.setToY(0);
            translatePane.play();

        }

        EventHandler panePress = (EventHandler<MouseEvent>) eventPress -> {
            mouseX = eventPress.getScreenX();
            mouseY = eventPress.getScreenY();
        };
        currentOrder.addEventFilter(MouseEvent.MOUSE_PRESSED, panePress);

        EventHandler paneDrag = (EventHandler<MouseEvent>) eventDrag -> {
            scrollPane.setDisable(true);
            if(!buttonExpanded){
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
                    label.setPrefWidth(labelWidth);
                    currentOrder.setPrefWidth(orderWidth);
                    currentOrder.setPrefHeight(orderHeight);
                    button.setPrefWidth(buttonWidth);
                    button.setPrefHeight(buttonHeight);
                    button.setTranslateY(0);
                    button.setTranslateX(0);
                    currentOrder.setTranslateX(0);
                }
                if(orderWidth - expand > orderWidth) {
                    button.setPrefWidth(buttonWidth - expand / 15);
                    button.setPrefHeight(buttonWidth - expand / 15);
                    currentOrder.setPrefWidth(orderWidth - expand);
                    currentOrder.setPrefHeight(orderHeight - expand);

                    double translateButtonY = currentOrder.getPrefHeight() - button.getPrefHeight() - 10.5 - buttonY;
                    double translateButtonX = (currentOrder.getPrefWidth() - button.getWidth()) / xButtonRation - buttonX;

                    button.setTranslateX(translateButtonX);
                    button.setTranslateY(translateButtonY);
                    label.setPrefWidth(labelWidth - expand);

                    if (currentOrder.getPrefWidth() >= maxOrderWidth) {
                        buttonExpanded = true;
                        mouseX = eventDrag.getScreenX();
                        mouseY = eventDrag.getScreenY();
                    }
                }
            }else {
                currentOrder.setTranslateX((currentOrder.getTranslateX()) + (eventDrag.getScreenX() - mouseX));
                currentOrder.setTranslateY((currentOrder.getTranslateY()) + (eventDrag.getScreenY() - mouseY));
                mouseX = eventDrag.getScreenX();
                mouseY = eventDrag.getScreenY();
            }
        };
        currentOrder.addEventFilter(MouseEvent.MOUSE_DRAGGED, paneDrag);

        EventHandler paneReleased = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!buttonExpanded){
                    currentOrder.removeEventFilter(MouseEvent.MOUSE_CLICKED, panePress);
                    currentOrder.removeEventFilter(MouseEvent.MOUSE_DRAGGED, paneDrag);
                    currentOrder.removeEventFilter(MouseEvent.MOUSE_RELEASED, this);
                    TranslateTransition transitionPane = new TranslateTransition(Duration.millis(750),currentOrder);
                    transitionPane.setToX(0);
                    transitionPane.setToY(0);
                    transitionPane.play();

                    TranslateTransition transitionButton = new TranslateTransition(Duration.millis(750),button);
                    transitionButton.setToX(0);
                    transitionButton.setToY(0);
                    transitionButton.play();

                    ResizeHeight heightPane = new ResizeHeight(Duration.millis(750),currentOrder,orderHeight);
                    heightPane.play();
                    ResizeWidth widthPane = new ResizeWidth(Duration.millis(750),currentOrder,orderWidth);
                    widthPane.play();

                    ResizeHeight heightButton = new ResizeHeight(Duration.millis(750),button,buttonHeight);
                    heightButton.play();
                    ResizeWidth widthButton = new ResizeWidth(Duration.millis(750),button,buttonWidth);
                    widthButton.play();

                    ResizeWidth widthLabel = new ResizeWidth(Duration.millis(750),label,labelWidth);
                    widthLabel.play();

                    Timeline reAppendOrderInFlow = new Timeline(new KeyFrame(Duration.millis(750), actionEvent -> {
                        currentOrder.setLayoutX(translatePaneX - orderContainer.getLayoutX());
                        orderContainer.getChildren().add(currentOrder);
                        currentOrder.setStyle("");
                        scrollPane.setDisable(false);
                        action = false;
                    }));
                    reAppendOrderInFlow.play();
                }
            }
        };


        currentOrder.addEventFilter(MouseEvent.MOUSE_RELEASED, paneReleased);
    }
}
