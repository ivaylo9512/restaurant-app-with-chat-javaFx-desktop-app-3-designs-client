package controllers.base;

import javafx.beans.property.ObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import models.ChatValue;
import models.Message;
import models.Session;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static application.RestaurantApplication.chatManager;
import static application.RestaurantApplication.loginManager;
import static application.ServerRequests.pageSize;

public class ChatSession {
    private ObjectProperty<ChatValue> chatValue;
    private VBox chatBlock;
    private Text chatInfo;
    private TextArea chatTextArea;
    
    public ChatSession(ObjectProperty<ChatValue> chatValue, VBox chatBlock, Text chatInfo, TextArea chatTextArea) {
        this.chatValue = chatValue;
        this.chatBlock = chatBlock;
        this.chatInfo = chatInfo;
        this.chatTextArea = chatTextArea;
    }

    public void setHistoryListener(){
        chatBlock.idProperty().addListener((observable1, oldValue1, newValue1) -> {
            if ((newValue1.equals("append") || newValue1.equals("beginning-append")) && chatValue.get() != null) {
                loadOlderHistory();
            }
        });
    }
    public void setChatAreaListener(ObjectProperty<ChatValue> chat, VBox chatBlock, TextArea chatTextArea){
        chatTextArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                addNewMessage();
                event.consume();
            }
        });
    }

    public void setChat(){

        chatBlock.setId("beginning");
        chatBlock.getChildren().remove(1, chatBlock.getChildren().size());

        ListOrderedMap<LocalDate, Session> sessionsMap = chatValue.get().getSessions();
        List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
        List<Session> lastSessions = chatSessions.subList(0, Math.min(pageSize, chatSessions.size()));

        if (lastSessions.size() == pageSize) {
            chatInfo.setText("Scroll for more history");
            chatValue.get().setDisplayedSessions(pageSize);
        } else {
            chatInfo.setText("Beginning of the chat");
            chatValue.get().setMoreSessions(false);
            chatValue.get().setDisplayedSessions(lastSessions.size());
        }

        lastSessions.forEach(session -> appendSession(session, 1));

        chatValue.get().getSessionsObservable().addListener((MapChangeListener<LocalDate, Session>) c -> {
            LocalDate sessionDate = c.getKey();
            int index = chatValue.get().getSessions().indexOf(sessionDate);
            newSessions(c.getValueAdded(), index);
        });
    }

    private void newSessions(Session session, int index) {
        if (index == 0 && !chatValue.get().isMoreSessions()) {
            chatTextArea.setText("Beginning of the chat");
        }

        chatValue.get().setDisplayedSessions(chatValue.get().getDisplayedSessions() + 1);
        appendSession(session, 1);
    }

    private void loadOlderHistory() {
        int displayedSessions = chatValue.get().getDisplayedSessions();
        int loadedSessions = chatValue.get().getSessions().size();

        ListOrderedMap<LocalDate, Session> sessionsMap = chatValue.get().getSessions();
        List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
        List<Session> nextSessions;
        if (loadedSessions > displayedSessions) {

            nextSessions = chatSessions.subList(displayedSessions,
                    Math.min(displayedSessions + pageSize, loadedSessions));

            if (displayedSessions + nextSessions.size() == loadedSessions && !chatValue.get().isMoreSessions()) {
                chatInfo.setText("Beginning of the chat");
            }
            chatValue.get().setDisplayedSessions(displayedSessions + nextSessions.size());

            nextSessions.forEach(session -> appendSession(session, 1));

        } else if (chatValue.get().isMoreSessions()) {
            chatManager.getNextSessions(chatValue.get());
        } else {
            chatInfo.setText("Beginning of the chat");
        }
    }

    @FXML
    public void addNewMessage(){
        int chatId = chatValue.get().getChatId();
        int receiverId = chatValue.get().getUserId();
        int index = chatBlock.getChildren().size();

        String messageText = chatTextArea.getText();
        chatTextArea.clear();

        if (messageText.length() > 0){
            Message message = new Message(receiverId, LocalTime.now(),LocalDate.now(), messageText, chatId);
            chatManager.sendMessage(messageText, chatId, receiverId);

            ListOrderedMap<LocalDate, Session> sessions = chatValue.get().getSessions();

            chatBlock.setId("new-message");
            Session session = sessions.get(message.getSession());
            if (session == null) {
                LocalDate sessionDate = message.getSession();

                session = new Session();
                session.setDate(sessionDate);
                sessions.put(0, sessionDate, session);
                session.getMessages().add(message);

                if (chatValue.get().getChatId() == message.getChatId()) {
                    chatValue.get().setDisplayedSessions(chatValue.get().getDisplayedSessions() + 1);
                    appendSession(session, index);
                }
            } else {
                session.getMessages().add(message);
                if (chatValue.get().getChatId() == message.getChatId()) {
                    appendMessage(message);
                }
            }
        }
    }

    private void appendSession(Session session, int index) {

        Text date = new Text(dateFormatter.format(session.getDate()));
        TextFlow dateFlow = new TextFlow(date);
        dateFlow.setTextAlignment(TextAlignment.CENTER);

        HBox sessionDate = new HBox(dateFlow);
        HBox.setHgrow(dateFlow, Priority.ALWAYS);
        sessionDate.getStyleClass().add("session-date");

        VBox sessionBlock = new VBox(sessionDate);
        sessionBlock.setId(session.getDate().toString());
        session.getMessages()
                .forEach(this::appendMessage);
        chatBlock.getChildren().add(index, sessionBlock);
    }

    private void appendMessage(Message message) {
        VBox sessionBlock = (VBox) chatBlock.lookup("#" + message.getSession().toString());

        HBox hBox = new HBox();
        VBox newBlock = new VBox();
        Text text = new Text();
        Text time = new Text();
        ImageView imageView = new ImageView();
        TextFlow textFlow = new TextFlow();

        time.getStyleClass().add("time");
        text.getStyleClass().add("message");
        newBlock.getStyleClass().add("chat-block");

        imageView.setFitHeight(34);
        imageView.setFitWidth(34);
        imageView.setLayoutX(3);
        imageView.setLayoutY(7);

        Circle clip = new Circle(20, 20, 20);

        Pane imageContainer = new Pane(imageView);
        imageContainer.setClip(clip);
        imageContainer.setMaxHeight(40);
        imageContainer.setMaxWidth(40);
        imageContainer.setMinWidth(40);


        Pane imageShadow = new Pane(imageContainer);
        imageShadow.setMaxHeight(40);
        imageShadow.setMaxWidth(40);
        imageShadow.setMinWidth(40);
        imageShadow.getStyleClass().add("imageShadow");

        HBox.setMargin(imageShadow, new Insets(-20, 0, 0, 0));

        if (message.getReceiverId() == loginManager.userId.get()) {
            imageView.setImage(chatValue.get().getSecondUserPicture());
            text.setText(message.getMessage());
            time.setText("  " + timeFormatter.format(message.getTime()));
            textFlow.getChildren().addAll(text, time);
            hBox.setAlignment(Pos.TOP_LEFT);

            imageView.setViewOrder(3);
            textFlow.setViewOrder(1);
        } else {
            imageView.setImage(loginManager.profileImage.get());
            text.setText(message.getMessage());
            time.setText(timeFormatter.format(message.getTime()) + "  ");
            textFlow.getChildren().addAll(time, text);
            hBox.setAlignment(Pos.TOP_RIGHT);
        }

        boolean timeElapsed;
        int timeToElapse = 10;

        List<Node> messageBlocks = sessionBlock.getChildren();
        if (messageBlocks.size() > 0 && messageBlocks.get(messageBlocks.size() - 1) instanceof VBox) {
            VBox lastBlock = (VBox) messageBlocks.get(messageBlocks.size() - 1);
            HBox lastMessage = (HBox) lastBlock.getChildren().get(lastBlock.getChildren().size() - 1);
            LocalTime lastBlockStartedDate;
            TextFlow firstTextFlow = (TextFlow) lastMessage.lookup("TextFlow");
            Text lastBlockStartedText = (Text) firstTextFlow.lookup(".time");
            lastBlockStartedDate = LocalTime.parse(lastBlockStartedText.getText().replaceAll("\\s+", ""));

            timeElapsed = java.time.Duration.between(lastBlockStartedDate, message.getTime()).toMinutes() > timeToElapse;

            if (message.getReceiverId() == loginManager.userId.get()) {
                if (!timeElapsed && lastMessage.getStyleClass().get(0).startsWith("second-user-message")) {

                    hBox.getStyleClass().add("second-user-message");
                    hBox.getChildren().addAll(textFlow);
                    lastBlock.getChildren().add(hBox);

                } else {

                    hBox.getStyleClass().add("second-user-message-first");
                    hBox.getChildren().addAll(imageShadow, textFlow);
                    newBlock.getChildren().add(hBox);
                    sessionBlock.getChildren().add(newBlock);

                }
            } else {
                if (!timeElapsed && lastMessage.getStyleClass().get(0).startsWith("user-message")) {

                    hBox.getStyleClass().add("user-message");
                    hBox.getChildren().addAll(textFlow);
                    lastBlock.getChildren().add(hBox);

                } else {

                    hBox.getStyleClass().add("user-message-first");
                    hBox.getChildren().addAll(textFlow, imageShadow);
                    newBlock.getChildren().add(hBox);
                    sessionBlock.getChildren().add(newBlock);

                }
            }
        } else {

            if (message.getReceiverId() == loginManager.userId.get()) {
                hBox.getStyleClass().add("second-user-message-first");
                hBox.getChildren().addAll(imageShadow, textFlow);
                newBlock.getChildren().add(hBox);
            } else {
                hBox.getStyleClass().add("user-message-first");
                hBox.getChildren().addAll(textFlow, imageShadow);
                newBlock.getChildren().add(hBox);
            }
            sessionBlock.getChildren().add(newBlock);
        }
    }

}
