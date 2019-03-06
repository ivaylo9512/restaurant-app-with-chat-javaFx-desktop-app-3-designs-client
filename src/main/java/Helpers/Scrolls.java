package Helpers;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;



public class Scrolls {
    private ScrollPane menuScroll, userInfoScroll, chatUsersScroll,
            ordersScroll;
    private TextArea mainChatTextArea;
    private ScrollPane mainChatScroll;
    private ScrollBar mainChatScrollBar;
    private double offsetY;
    private double heightDiff;

    public Scrolls(ScrollPane menuScroll,ScrollPane userInfoScroll,
            ScrollPane chatUsersScroll, ScrollPane ordersScroll,ScrollPane mainChatScroll, TextArea mainChatTextArea) {
        this.menuScroll = menuScroll;
        this.userInfoScroll = userInfoScroll;
        this.chatUsersScroll = chatUsersScroll;
        this.ordersScroll = ordersScroll;
        this.mainChatScroll = mainChatScroll;
        this.mainChatTextArea = mainChatTextArea;

        fixBlurryContent();

        reverseOrderScroll();
        changeMenuScrollBehaviour();
        listenForHistoryRequest();
    }
    private void listenForHistoryRequest(){
        VBox content = (VBox) mainChatScroll.getContent();
        mainChatScroll.skinProperty().addListener((observable, oldValue, newValue) -> {

            for (Node node : mainChatScroll.lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar) {
                    ScrollBar bar = (ScrollBar) node;
                    if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                        mainChatScrollBar = bar;
                    }
                }
            }

            mainChatScrollBar.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                offsetY = event.getScreenY();
            });

            mainChatScrollBar.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
                double move = offsetY - event.getScreenY();
                double value = move / mainChatScrollBar.getHeight();
                mainChatScrollBar.setValue(mainChatScrollBar.getValue() - value);
                offsetY = event.getScreenY();
                event.consume();
            });

            content.heightProperty().addListener((observable1, oldValue1, newValue1) -> {
                if(!content.getId().equals("beginning")) {
                    double oldHeight = oldValue1.doubleValue();
                    double newHeight = newValue1.doubleValue();
                    heightDiff = oldHeight / newHeight;
                    double nextValue = 1 - heightDiff;
                    mainChatScrollBar.setValue(nextValue);
                    content.setId("listen");
                }else{
                    mainChatScrollBar.setValue(1);
                    content.setId("listen");
                }
            });
            mainChatScrollBar.valueProperty().addListener((observable1, oldValue1, newValue1) -> {
                double newPosition = newValue1.doubleValue();
                double oldPosition = oldValue1.doubleValue();
                System.out.println(oldPosition);
                if (newPosition <= mainChatScrollBar.getMin() && !content.getId().equals("beginning")) {
                    mainChatScrollBar.setValue(0);
                    content.setId("append");
                }else if(newPosition > 1){
                    mainChatScrollBar.setValue(1);
                }
            });
        });
    }

    private void changeMenuScrollBehaviour() {
        AnchorPane anchorPane = (AnchorPane) menuScroll.getContent();
        menuScroll.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {

                if (menuScroll.getHeight() <= 211) {

                    ScrollPane scrollPane;
                    if (menuScroll.getVvalue() == 0) {
                        scrollPane = userInfoScroll;
                    }else {
                        scrollPane = chatUsersScroll;
                    }
                    Pane content = (Pane) scrollPane.getContent();
                    scrollPane.setVvalue(scrollPane.getVvalue() - event.getDeltaY() / content.getHeight());
                    event.consume();

                }else{
                    chatUsersScroll.setDisable(true);
                    userInfoScroll.setDisable(false);

                    if(anchorPane.getHeight() <= menuScroll.getHeight()){
                        chatUsersScroll.setDisable(false);
                    }else if(menuScroll.getVvalue() == 1){
                        chatUsersScroll.setDisable(false);
                        userInfoScroll.setDisable(true);
                    }
                }

            }
        });
    }

    private void reverseOrderScroll() {

        ordersScroll.setOnScroll(event -> {
            if(event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                FlowPane pane = (FlowPane) ordersScroll.getContent();
                ordersScroll.setHvalue(ordersScroll.getHvalue() - event.getDeltaY() / pane.getWidth());
            }
        });
    }

    private void fixBlurryContent(){
        fixBlurriness(menuScroll);
        fixBlurriness(userInfoScroll);
        fixBlurriness(chatUsersScroll);
        fixBlurriness(ordersScroll);
        fixBlurriness(mainChatScroll);
        mainChatTextArea.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TextAreaSkin textAreaSkin= (TextAreaSkin) newValue;
                ScrollPane textAreaScroll = (ScrollPane) textAreaSkin.getChildren().get(0);
                fixBlurriness(textAreaScroll);

            }
        });
    }

    private void fixBlurriness(ScrollPane scrollPane){
        scrollPane.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                scrollPane.getChildrenUnmodifiable().get(0).setCache(false);
            }
        });
    }

}
