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
    private ScrollBar mainChatScrollBar;
    private TextArea mainChatTextArea;
    private ScrollPane mainChatScroll;
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
    public void manageScrolls(){
        fixBlurryContent();
        changeMenuScrollBehaviour();
        listenForHistoryRequest();

    }
    private void listenForHistoryRequest(){
        VBox content = (VBox) mainChatScroll.getContent();
        mainChatScroll.skinProperty().addListener((observable, oldValue, newValue) -> {

            mainChatScrollBar = findVerticalScrollBar(mainChatScroll);
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

            mainChatScrollBar.visibleAmountProperty().addListener((observable1, oldValue1, newValue1) -> {
                if(newValue1.doubleValue() > 1 && !mainChatScrollBar.isDisabled()){
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
                    mainChatScrollBar.setValue(nextValue);
                    content.setId("listen");
                }else{
                    mainChatScrollBar.setValue(1);
                }
            });
            mainChatScrollBar.valueProperty().addListener((observable1, oldValue1, newValue1) -> {
                double newPosition = newValue1.doubleValue();
                if (newPosition <= mainChatScrollBar.getMin() && !content.getId().equals("beginning") &&
                        !content.getId().equals("beginning-append")) {
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

    static ScrollBar findVerticalScrollBar(Node scroll) {
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

    static void fixBlurriness(ScrollPane scrollPane){
        scrollPane.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                scrollPane.getChildrenUnmodifiable().get(0).setCache(false);
            }
        });
    }

}
