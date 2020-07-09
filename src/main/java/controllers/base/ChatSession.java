package controllers.base;

import helpers.FontIndicator;
import helpers.ObservableOrderedMapChange;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.*;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
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
import java.util.*;

import static application.RestaurantApplication.chatManager;
import static application.RestaurantApplication.loginManager;
import static application.ServerRequests.pageSize;
import static helpers.FontIndicator.fontPx;

public class ChatSession {
    private ObjectProperty<ChatValue> chatValue;
    private VBox chatBlock;
    private Text chatInfo;
    private TextArea chatTextArea;
    private Node chatContainer;

    private ObservableList<Message> chatCurrentSession, chatLastSession;

    private ListChangeListener<Message> currentMessageListener = createMessageListener();
    private ListChangeListener<Message> lastMessageListener  = createMessageListener();

    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");

    public ChatSession(Node chatContainer, ObjectProperty<ChatValue> chatValue, VBox chatBlock, Text chatInfo, TextArea chatTextArea) {
        this.chatValue = chatValue;
        this.chatBlock = chatBlock;
        this.chatInfo = chatInfo;
        this.chatTextArea = chatTextArea;
        this.chatContainer = chatContainer;
    }

    public void init(){
        setHistoryListener();
        setChatAreaListener();
    }

    public void setHistoryListener(){
        chatBlock.idProperty().addListener((observable1, oldValue1, newValue1) -> {
            if ((newValue1.equals("append") || newValue1.equals("beginning-append")) && chatValue.get() != null) {
                loadOlderHistory();
            }
        });
    }

    public void setChatAreaListener(){
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
        List<Session> lastSessions = chatSessions.subList(chatSessions.size() - Math.min(pageSize, chatSessions.size()), chatSessions.size());

        if (lastSessions.size() == pageSize) {
            chatInfo.setText("Scroll for more history");
            chatValue.get().setDisplayedSessions(pageSize);
        } else {
            chatInfo.setText("Beginning of the chat");
            chatValue.get().setMoreSessions(false);
            chatValue.get().setDisplayedSessions(lastSessions.size());
        }

        lastSessions.forEach(session -> appendSession(session, chatBlock.getChildren().size()));
        chatValue.get().getSessions().addListener(sessionChange);
    }

    private MapChangeListener<LocalDate, Session> sessionChange = c -> {
        int index = ((ObservableOrderedMapChange)c).getIndex();
        addNewSession(c.getValueAdded(), index + 1);
    };

    private void addNewSession(Session session, int index) {
        if (!chatValue.get().isMoreSessions()) {
            chatInfo.setText("Beginning of the chat");
        }

        chatValue.get().setDisplayedSessions(chatValue.get().getDisplayedSessions() + 1);
        appendSession(session, index);
    }

    private void loadOlderHistory() {
        int displayedSessions = chatValue.get().getDisplayedSessions();
        int loadedSessions = chatValue.get().getSessions().size();

        if (loadedSessions > displayedSessions) {
            ListOrderedMap<LocalDate, Session> sessionsMap = chatValue.get().getSessions();
            Deque<Session> nextSessions = new ArrayDeque<>(
                    sessionsMap.valueList().subList(loadedSessions - displayedSessions - Math.min(pageSize, loadedSessions - displayedSessions),
                    loadedSessions - displayedSessions));

            if (displayedSessions + nextSessions.size() == loadedSessions && !chatValue.get().isMoreSessions()) {
                chatInfo.setText("Beginning of the chat");
            }
            chatValue.get().setDisplayedSessions(displayedSessions + nextSessions.size());

            while (!nextSessions.isEmpty()) appendSession(nextSessions.removeLast(), 1);

        } else if (chatValue.get().isMoreSessions()) {
            chatManager.getNextSessions(chatValue.get());
        } else {
            chatInfo.setText("Beginning of the chat");
        }
    }

    public void addNewMessage(){
        int chatId = chatValue.get().getChatId();
        int receiverId = chatValue.get().getUserId();

        String messageText = chatTextArea.getText();
        chatTextArea.clear();

        if (messageText.length() > 0){
            Message message = new Message(receiverId, LocalTime.now(),LocalDate.now(), messageText, chatId);
            chatManager.sendMessage(messageText, chatId, receiverId);

            ObservableMap<LocalDate, Session> sessions = chatValue.get().getSessions();

            chatBlock.setId("new-message");
            Session session = sessions.get(message.getSession());
            if (session == null) {
                LocalDate sessionDate = message.getSession();

                session = new Session();
                session.setDate(sessionDate);
                session.getMessages().add(message);
                sessions.put(sessionDate, session);
            } else {
                session.getMessages().add(message);
            }
        }
    }

    private void appendSession(Session session, int index) {
        if(index == chatBlock.getChildren().size()){
            if(chatLastSession != null){
                chatLastSession.removeListener(lastMessageListener);
                chatCurrentSession.removeListener(currentMessageListener);
            }else if(chatCurrentSession != null){
                chatCurrentSession.removeListener(currentMessageListener);
                chatCurrentSession.addListener(lastMessageListener);
                chatLastSession = chatCurrentSession;
            }

            chatCurrentSession = session.getMessages();
            chatCurrentSession.addListener(currentMessageListener);
        }

        Text date = new Text(dateFormatter.format(session.getDate()));
        TextFlow dateFlow = new TextFlow(date);
        dateFlow.setTextAlignment(TextAlignment.CENTER);

        HBox sessionDate = new HBox(dateFlow);
        HBox.setHgrow(dateFlow, Priority.ALWAYS);
        sessionDate.getStyleClass().add("session-date");

        VBox sessionBlock = new VBox(sessionDate);
        sessionBlock.setId(session.getDate().toString());
        chatBlock.getChildren().add(index, sessionBlock);
        session.getMessages()
                .forEach(this::appendMessage);
    }

    private ListChangeListener<Message> createMessageListener() {
        return c -> {
            c.next();
            c.getAddedSubList().forEach(this::appendMessage);
        };
    }

    private void appendMessage(Message message) {
        VBox sessionBlock = (VBox) chatBlock.lookup("#" + message.getSession().toString());

        HBox hBox = new HBox();
        VBox newBlock = new VBox();
        Text text = new Text();
        Text time = new Text();
        ImageView imageView = new ImageView();
        TextFlow textFlow = new TextFlow();
        textFlow.setMaxHeight(Double.NEGATIVE_INFINITY);

        time.getStyleClass().add("time");
        text.getStyleClass().add("message");
        newBlock.getStyleClass().add("chat-block");

        imageView.setFitHeight(34);
        imageView.setFitWidth(34);

        DoubleBinding fontPx = FontIndicator.fontPx.multiply(1.67);
        Circle background = new Circle();
        Circle clip = new Circle();
        clip.centerXProperty().bind(fontPx);
        clip.centerYProperty().bind(fontPx);
        clip.radiusProperty().bind(fontPx);
        background.radiusProperty().bind(fontPx);

        StackPane imageContainer = new StackPane(background, imageView);
        imageContainer.setClip(clip);

        Pane imageShadow = new Pane(imageContainer);
        imageShadow.setMaxHeight(Double.NEGATIVE_INFINITY);
        imageShadow.setMinWidth(40);

        if (message.getReceiverId() == loginManager.userId.get()) {
            imageView.setImage(chatValue.get().getSecondUserPicture());
            text.setText(message.getMessage());
            time.setText("  " + timeFormatter.format(message.getTime()));
            textFlow.getChildren().addAll(text, time);

            imageShadow.setViewOrder(1);
            textFlow.setViewOrder(3);
        } else {
            imageView.setImage(loginManager.profileImage.get());
            text.setText(message.getMessage());
            time.setText(timeFormatter.format(message.getTime()) + "  ");
            textFlow.getChildren().addAll(time, text);
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

    public void bindChat(){
        chatContainer.disableProperty().bind(chatValue.isNull());

        chatValue.addListener(valueListener);
    }

    public void unBindChat() {
        chatContainer.disableProperty().unbind();
        chatContainer.setDisable(true);

        chatValue.removeListener(valueListener);

        chatBlock.getChildren().remove(1, chatBlock.getChildren().size());
        chatValue.set(null);
        chatTextArea.setText(null);

        resetSessions();
    }

    private void resetSessions() {
        if(chatCurrentSession != null) chatCurrentSession.removeListener(currentMessageListener);
        if(chatLastSession != null) chatLastSession.removeListener(lastMessageListener);

        chatCurrentSession = null;
        chatLastSession = null;
    }

    private ChangeListener<ChatValue> valueListener = (observable, oldValue, newValue) -> {
        if(oldValue != null){
            oldValue.getSessions().removeListener(sessionChange);
            resetSessions();
        }

        if(newValue != null) {
            setChat();
        }else {
            chatBlock.getChildren().remove(1, chatBlock.getChildren().size());
            chatTextArea.setText(null);

        }
    };
}
