package animations;

import controllers.firststyle.LoggedFirst;
import helpers.listviews.OrderListViewCell;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import models.Order;

public class ExpandOrderPane {

    private double mouseY, mouseX, initialOffsetX, initialMouseX;
    private DoubleProperty orderWidth, orderHeight;
    private double maxOrderWidthRatio = 4;

    public Pane currentContainer, currentPane, orderPane;
    public StackPane contentPane;

    public GridPane dates;
    public OrderListViewCell cell;
    public Button button;
    public ListView orderList;

    public double cellLayoutX, cellWidth;

    public BooleanProperty isButtonExpanded = new SimpleBooleanProperty(false);
    public BooleanProperty action = new SimpleBooleanProperty(false);
    public boolean isOrderListScrolling;

    private LoggedFirst controller;
    private FadeTransition showDates;
    private TranslateTransition transitionPane;
    private TransitionResizeHeight heightPane;
    private TransitionResizeWidth widthPane;

    private Timeline expandedDelay = new Timeline();

    public void setControllerFields(LoggedFirst controller, Pane orderContainer, Button expandButton, StackPane contentPane, ListView<Order> ordersList, GridPane dates) {
        this.controller = controller;
        this.orderPane = orderContainer;
        this.button = expandButton;
        this.contentPane = contentPane;
        this.orderList = ordersList;
        this.dates = dates;
    }

    public void setCurrentOrder(MouseEvent event){
        action.setValue(true);
        expandedDelay.stop();

        orderList.setDisable(true);
        orderList.setOpacity(0.4);
        currentPane.setOpacity(0);

        mouseX = event.getScreenX();
        mouseY = event.getScreenY();

        setOrderDimension(event);

        initialMouseX = event.getScreenX();
    }

    public void setOrderDimension(MouseEvent event) {
        orderWidth = currentPane.minWidthProperty();
        orderHeight = currentPane.minWidthProperty();

        cellLayoutX = cell.getLayoutX();
        cellWidth = cell.getWidth();

        initialOffsetX = event.getX() - cellLayoutX - currentPane.getLayoutX();
        double initialOffsetY = event.getY() - currentPane.getLayoutY();

        orderPane.setLayoutX(event.getSceneX() - initialOffsetX);
        orderPane.setLayoutY(event.getSceneY() - initialOffsetY);
    }

    public void setListeners(){
        button.setOnMouseClicked(this::buttonPress);

        orderPane.setOnMouseEntered(event -> ResizeRoot.resize = false);
        orderPane.setOnMouseExited(event -> ResizeRoot.resize = true);
        orderPane.addEventFilter(MouseEvent.MOUSE_PRESSED, this::panePress);
        orderPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::paneDrag);

        orderList.setOnMouseReleased(this::paneReleased);
        orderList.setOnMouseDragged(this::listDrag);
        orderList.getParent().setOnMouseReleased(this::paneReleased);
        orderList.getParent().setOnMouseDragged(this::listDrag);

        showDates = new FadeTransition(Duration.millis(500), dates);
        showDates.setFromValue(0);
        showDates.setToValue(1);

        transitionPane = new TranslateTransition(Duration.millis(750), orderPane);
        transitionPane.setToX(0);
        transitionPane.setToY(0);
        heightPane = new TransitionResizeHeight(orderPane);
        widthPane = new TransitionResizeWidth(orderPane);
    }

    public void expandOrder(MouseEvent event) {
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        intersectedNode = intersectedNode == null ? (Node)event.getTarget() : intersectedNode;

        if(!isOrderListScrolling && !action.get() && (intersectedNode instanceof Button || intersectedNode instanceof VBox)){

            currentPane = intersectedNode instanceof VBox ? (VBox) intersectedNode
                    : (VBox) intersectedNode.getParent();
            currentContainer = (Pane)currentPane.getParent();
            cell =  (OrderListViewCell) currentContainer.getParent();

            setCurrentOrder(event);
            controller.setOrder(cell.order);

            if(intersectedNode instanceof Button)
                expandOrderOnClick();

        }
    }

    private void listDrag(MouseEvent event) {
        if(action.get() && !isOrderListScrolling) paneDrag(event);
    }

    private void buttonPress(MouseEvent event){
        if(!action.get() || isButtonExpanded.get()) {
            if (isButtonExpanded.get()) {
                if(orderList.getOpacity() == 0){
                    resetOrder();
                }else {
                    reverseOrder();
                }
            } else {
                expandOrderOnClick();
            }
        }
    }

    private void panePress(MouseEvent event){
        mouseX = event.getScreenX();
        mouseY = event.getScreenY();
    }

    private void paneDrag(MouseEvent event) {
        if(!isButtonExpanded.getValue()){
            expandOrderOnDrag(event);
        }else {
            moveOrder(event);
        }
    }
    private void paneReleased(MouseEvent event) {
        if(!isButtonExpanded.get() && action.get()) {
            if(orderList.getOpacity() == 0){
                resetOrder();
            }else{
                reverseOrder();
            }
        }
    }

    private void moveOrder(MouseEvent eventDrag){
        orderPane.setTranslateX((orderPane.getTranslateX()) + (eventDrag.getScreenX() - mouseX));
        orderPane.setTranslateY((orderPane.getTranslateY()) + (eventDrag.getScreenY() - mouseY));
        mouseX = eventDrag.getScreenX();
        mouseY = eventDrag.getScreenY();
    }

    public void expandOrderOnClick(){
        expandedDelay = new Timeline(new KeyFrame(Duration.millis(750), event -> isButtonExpanded.setValue(true)));
        expandedDelay.play();

        TransitionResizeHeight heightPane = new TransitionResizeHeight(Duration.millis(750), orderPane, orderWidth.get() * maxOrderWidthRatio);
        heightPane.play();
        TransitionResizeWidth widthPane = new TransitionResizeWidth(Duration.millis(750), orderPane, orderWidth.get() * maxOrderWidthRatio);
        widthPane.play();

        TranslateTransition translatePane = new TranslateTransition(Duration.millis(750),orderPane);
        translatePane.setToX(-(orderWidth.get() * maxOrderWidthRatio - orderPane.getWidth()) / 2);
        translatePane.setToY(0);
        translatePane.play();

        showDates.setDelay(Duration.millis(750));
        showDates.play();
    }

    private void expandOrderOnDrag(MouseEvent eventDrag){
        double fasterExpand = 1.8;
        double expand = (initialMouseX - eventDrag.getScreenX()) * fasterExpand;
        if(initialOffsetX < orderWidth.get() / 2){
            if(expand < 0 ){
                expand = 0;
            }else{
                expand *= -1;
                orderPane.setTranslateX(expand);
            }
        }
        if(expand >= 0){
            orderPane.setPrefSize(orderWidth.get(), orderHeight.get());
            orderPane.setTranslateX(0);
        }

        if(orderWidth.get() - expand > orderWidth.get()) {

            orderPane.setPrefSize(orderWidth.get() - expand, orderHeight.get() - expand);

            if (orderPane.getPrefWidth() >= orderWidth.get() * maxOrderWidthRatio) {
                isButtonExpanded.setValue(true);
                orderPane.setPrefSize(orderWidth.get() * maxOrderWidthRatio, orderWidth.get() * maxOrderWidthRatio);

                mouseX = eventDrag.getScreenX();
                mouseY = eventDrag.getScreenY();

                showDates.setDelay(Duration.ZERO);
                showDates.play();
            }
        }

    }
    public void reverseOrder() {
        isButtonExpanded.setValue(false);
        showDates.stop();
        dates.setOpacity(0);

        int maxDelay = 750;
        double widthRatio = orderPane.getWidth() / (orderWidth.get() * maxOrderWidthRatio);
        Duration delay = Duration.millis(widthRatio * maxDelay);

        System.out.println(orderPane.getWidth());

        transitionPane.setDuration(delay);
        transitionPane.play();

        heightPane.setToHeight(orderHeight.get());
        heightPane.setDuration(delay);
        heightPane.play();
        widthPane.setToWidth(orderWidth.get());
        widthPane.setDuration(delay);
        widthPane.play();

        Timeline reAppendOrderInFlow = new Timeline(new KeyFrame(delay, actionEvent -> {
            orderList.getSelectionModel().clearSelection();
            orderList.setDisable(false);
            orderList.setOpacity(1);
            currentPane.setOpacity(1);
            action.setValue(false);
        }));
        reAppendOrderInFlow.play();
    }

    public void resetOrder(){
        isButtonExpanded.setValue(false);
        showDates.stop();
        dates.setOpacity(0);

        orderPane.setTranslateX(0);
        orderPane.setTranslateY(0);

        orderPane.setPrefSize(orderWidth.get(), orderHeight.get());

        orderList.getSelectionModel().clearSelection();
        orderList.setDisable(false);
        currentPane.setOpacity(1);
        action.setValue(false);
    }
}
