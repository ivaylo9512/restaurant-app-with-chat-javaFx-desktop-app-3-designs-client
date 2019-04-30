package Helpers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


public class Scrolls {
    private ScrollPane menuScroll, userInfoScroll, chatUsersScroll, notificationsScroll;
    private TextArea mainChatTextArea;
    private TextArea secondChatTextArea;
    private ScrollPane mainChatScroll;
    private ScrollPane secondChatScroll;
    private double heightDiff;
    private double offsetY;

    public Scrolls(ScrollPane menuScroll,ScrollPane userInfoScroll, ScrollPane chatUsersScroll,ScrollPane mainChatScroll,ScrollPane notificationsScroll, TextArea mainChatTextArea) {
        this.menuScroll = menuScroll;
        this.userInfoScroll = userInfoScroll;
        this.chatUsersScroll = chatUsersScroll;
        this.mainChatScroll = mainChatScroll;
        this.mainChatTextArea = mainChatTextArea;
        this.notificationsScroll = notificationsScroll;

    }
    public Scrolls(ScrollPane mainChatScroll, TextArea mainChatTextArea) {
        this.mainChatScroll = mainChatScroll;
        this.mainChatTextArea = mainChatTextArea;
    }
    public Scrolls(ScrollPane mainChatScroll, ScrollPane secondChatScroll, TextArea mainChatTextArea, TextArea secondChatTextArea) {
        this.mainChatScroll = mainChatScroll;
        this.secondChatScroll = secondChatScroll;
        this.mainChatTextArea = mainChatTextArea;
        this.secondChatTextArea = secondChatTextArea;
    }
    public void manageScrollsFirstStyle(){
        fixBlurryContent();
        changeMenuScrollBehaviour();
        listenForHistoryRequest(mainChatScroll);

    }
    public void manageScrollsSecondStyle(){
        mainChatTextArea.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TextAreaSkin textAreaSkin= (TextAreaSkin) newValue;
                ScrollPane textAreaScroll = (ScrollPane) textAreaSkin.getChildren().get(0);
                fixBlurriness(textAreaScroll);
            }
        });
        fixBlurriness(mainChatScroll);
        listenForHistoryRequest(mainChatScroll);

    }
    public void manageScrollsThirdStyle(){
        mainChatTextArea.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TextAreaSkin textAreaSkin= (TextAreaSkin) newValue;
                ScrollPane textAreaScroll = (ScrollPane) textAreaSkin.getChildren().get(0);
                fixBlurriness(textAreaScroll);
            }
        });
        secondChatTextArea.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TextAreaSkin textAreaSkin= (TextAreaSkin) newValue;
                ScrollPane textAreaScroll = (ScrollPane) textAreaSkin.getChildren().get(0);
                fixBlurriness(textAreaScroll);
            }
        });

        fixBlurriness(mainChatScroll);
        fixBlurriness(secondChatScroll);

        listenForHistoryRequest(mainChatScroll);
        listenForHistoryRequest(secondChatScroll);

    }
    private void listenForHistoryRequest(ScrollPane chatScroll){
        VBox content = (VBox) chatScroll.getContent();
        chatScroll.skinProperty().addListener((observable, oldValue, newValue) -> {

            ScrollBar chatScrollBar = findVerticalScrollBar(chatScroll);
            chatScrollBar.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> offsetY = event.getScreenY());

            chatScrollBar.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
                double move = offsetY - event.getScreenY();
                double value = move / chatScrollBar.getHeight();
                chatScrollBar.setValue(chatScrollBar.getValue() - value);
                offsetY = event.getScreenY();
                event.consume();
            });

            chatScrollBar.visibleAmountProperty().addListener((observable1, oldValue1, newValue1) -> {
                if(newValue1.doubleValue() > 1 && !chatScrollBar.isDisabled()){
                    content.setId("beginning-append");
                }else if(!content.getId().equals("new-message")){
                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.millis(50), event -> content.setId("listen")));
                    timeline.play();
                }
            });

            content.heightProperty().addListener((observable1, oldValue1, newValue1) -> {
                if(!content.getId().equals("beginning") && !content.getId().equals("new-message") &&
                        !content.getId().equals("beginning-append")) {
                    double oldHeight = oldValue1.doubleValue();
                    double newHeight = newValue1.doubleValue();
                    heightDiff = oldHeight / newHeight;
                    double nextValue = 1 - heightDiff;
                    chatScrollBar.setValue(nextValue);
                    content.setId("listen");
                }else{
                    chatScrollBar.setValue(1);
                }
            });
            chatScrollBar.valueProperty().addListener((observable1, oldValue1, newValue1) -> {
                double newPosition = newValue1.doubleValue();
                if (newPosition <= chatScrollBar.getMin() && !content.getId().equals("beginning") &&
                        !content.getId().equals("beginning-append")) {
                    chatScrollBar.setValue(0);
                    content.setId("append");
                }else if(newPosition > 1){
                    chatScrollBar.setValue(1);
                }
            });
        });
    }

    private void changeMenuScrollBehaviour() {
        AnchorPane anchorPane = (AnchorPane) menuScroll.getContent();
        menuScroll.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {

                if (menuScroll.getHeight() <= 212) {
                    if(menuScroll.getVvalue() == 0 || menuScroll.getVvalue() == 1) {
                        chatUsersScroll.setDisable(false);
                        userInfoScroll.setDisable(false);

                        ScrollPane scrollPane;
                        if (menuScroll.getVvalue() == 0) {
                            scrollPane = userInfoScroll;
                        } else {
                            scrollPane = chatUsersScroll;
                        }
                        Pane content = (Pane) scrollPane.getContent();
                        scrollPane.setVvalue(scrollPane.getVvalue() - event.getDeltaY() / content.getHeight());
                        event.consume();
                    }
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

    private void fixBlurryContent(){
        fixBlurriness(menuScroll);
        fixBlurriness(userInfoScroll);
        fixBlurriness(chatUsersScroll);
        fixBlurriness(mainChatScroll);
        fixBlurriness(mainChatScroll);
        fixBlurriness(notificationsScroll);

        mainChatTextArea.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TextAreaSkin textAreaSkin= (TextAreaSkin) newValue;
                ScrollPane textAreaScroll = (ScrollPane) textAreaSkin.getChildren().get(0);
                fixBlurriness(textAreaScroll);
            }
        });
    }

    public static ScrollBar findVerticalScrollBar(Node scroll) {
        for (Node node : scroll.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) node;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    return bar;
                }
            }
        }
        return null;
    }

    public static void fixBlurriness(ScrollPane scrollPane){
        scrollPane.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                scrollPane.getChildrenUnmodifiable().get(0).setCache(false);
            }
        });
    }

}
