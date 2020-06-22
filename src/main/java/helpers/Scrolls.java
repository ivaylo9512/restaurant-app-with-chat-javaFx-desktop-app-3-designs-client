package helpers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.List;


public class Scrolls {
    private ScrollPane menuScroll, userInfoScroll, mainChatScroll, secondChatScroll;
    private TextArea mainChatTextArea, secondChatTextArea;
    public double heightDiff, offsetY, initialMenuScrollHeight;
    private ListView chatUsersList;
    private ScrollBar chatUsersScrollBar;

    public Scrolls(ScrollPane menuScroll, ScrollPane userInfoScroll, ListView chatUsersList, ScrollPane mainChatScroll, TextArea mainChatTextArea, ScrollPane secondChatScroll, TextArea secondChatTextArea) {
        this.menuScroll = menuScroll;
        this.userInfoScroll = userInfoScroll;
        this.chatUsersList = chatUsersList;
        this.mainChatScroll = mainChatScroll;
        this.mainChatTextArea = mainChatTextArea;
        this.secondChatScroll = secondChatScroll;
        this.secondChatTextArea = secondChatTextArea;
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
        fixBlurryContent(List.of(mainChatTextArea, secondChatTextArea),
                List.of(menuScroll, mainChatScroll, secondChatScroll, userInfoScroll));

        listenForHistoryRequest(mainChatScroll);
        listenForHistoryRequest(secondChatScroll);

        chatUsersList.skinProperty().addListener((observable, oldValue, newValue) -> {
            chatUsersScrollBar = findVerticalScrollBar(chatUsersList);
            changeMenuScrollBehaviour();
        });
    }
    public void manageScrollsSecondStyle(){
        fixBlurryContent(List.of(mainChatTextArea), List.of(mainChatScroll));
        listenForHistoryRequest(mainChatScroll);

    }

    public void manageScrollsThirdStyle(){
        fixBlurryContent(List.of(mainChatTextArea, secondChatTextArea),
                List.of(mainChatScroll, secondChatScroll));

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
        GridPane anchorPane = (GridPane) menuScroll.getContent();
        menuScroll.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                if (menuScroll.getHeight() == initialMenuScrollHeight) {
                    if(menuScroll.getVvalue() == 0 || menuScroll.getVvalue() == 1) {
                        chatUsersList.setDisable(false);
                        userInfoScroll.setDisable(false);

                        if (menuScroll.getVvalue() == 0) {
                            Pane content = (Pane) userInfoScroll.getContent();
                            userInfoScroll.setVvalue(userInfoScroll.getVvalue() - event.getDeltaY() / content.getHeight());
                        } else {
                            double value = chatUsersScrollBar.getValue() - event.getDeltaY() / chatUsersList.getHeight();

                            chatUsersScrollBar.setValue(Math.max(value, 0));
                        }
                        event.consume();
                    }
                }else{
                    chatUsersList.setDisable(true);
                    userInfoScroll.setDisable(false);

                    if(anchorPane.getHeight() <= menuScroll.getHeight()){
                        chatUsersList.setDisable(false);
                    }else if(menuScroll.getVvalue() == 1){
                        chatUsersList.setDisable(false);
                        userInfoScroll.setDisable(true);
                    }
                }

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

    private void fixBlurryContent(List<TextArea> textAreas, List<ScrollPane> scrollPanes){
        scrollPanes.forEach(this::fixBlurriness);
        textAreas.forEach(this::fixTextAreaBlurriness);
    }

    private void fixTextAreaBlurriness(TextArea textArea) {
        textArea.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TextAreaSkin textAreaSkin= (TextAreaSkin) newValue;
                ScrollPane textAreaScroll = (ScrollPane) textAreaSkin.getChildren().get(0);
                fixBlurriness(textAreaScroll);
            }
        });
    }

    public void fixBlurriness(ScrollPane scrollPane){
        scrollPane.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                scrollPane.getChildrenUnmodifiable().get(0).setCache(false);
            }
        });
    }

}
