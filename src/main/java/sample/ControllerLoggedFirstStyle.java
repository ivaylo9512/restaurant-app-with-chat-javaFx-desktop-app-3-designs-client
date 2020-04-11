package sample;

import Animations.*;
import Helpers.ListViews.NotificationListViewCell;
import Helpers.ListViews.OrderListViewCell;
import Helpers.Scrolls;
import Models.*;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import sample.base.ControllerLogged;

import java.util.*;
import java.util.List;

import static Animations.ExpandOrderPane.*;

public class ControllerLoggedFirstStyle extends ControllerLogged {
    @FXML ScrollPane menuScroll, userInfoScroll, chatUsersScroll, mainChatScroll;
    @FXML VBox mainChatBlock, chatUsers;
    @FXML FlowPane chatInfo;
    @FXML AnchorPane contentPane, mainChat, ordersPane, profileImageContainer, orderContainer, dishesAnchor, createdContainer, updatedContainer;
    @FXML Pane moveBar, profileRoot;
    @FXML TextArea mainChatTextArea;
    @FXML ImageView roleImage;
    @FXML Button expandButton;
    @FXML GridPane dates;

    private Map<Integer, ChatValue> chatsMap = new HashMap<>();

    private ScrollBar ordersScrollBar;
    private ChatValue mainChatValue;

    private Image chefImage;
    private Image waiterImage;

    @FXML
    public void initialize() {
        super.initialize();
        ordersList.setCellFactory(orderCell -> new OrderListViewCell());
        notificationsList.setCellFactory(menuCell -> new NotificationListViewCell());

        Scrolls scrolls = new Scrolls(menuScroll, userInfoScroll, chatUsersScroll,
                mainChatScroll, mainChatTextArea);
        scrolls.manageScrollsFirstStyle();

        ordersList.addEventFilter(MouseEvent.MOUSE_PRESSED, this::expandOrder);
        ordersList.skinProperty().addListener((observable, oldValue, newValue) -> {
            for (Node node: ordersList.lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar) {
                    ScrollBar bar = (ScrollBar) node;
                    if(bar.getOrientation().equals(Orientation.HORIZONTAL)) {
                        ordersScrollBar = (ScrollBar) node;
                        ExpandOrderListeners();
                    }
                }
            }
        });

        mainChatBlock.idProperty().addListener((observable1, oldValue1, newValue1) -> {
            if ((newValue1.equals("append") || newValue1.equals("beginning-append")) && mainChatValue != null) {
                loadOlderHistory(mainChatValue, mainChatBlock);
            }
        });

        mainChatTextArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                addNewMessage();
                event.consume();
            }
        });

        chefImage = new Image(getClass().getResourceAsStream("/images/chef-second.png"));
        waiterImage = new Image(getClass().getResourceAsStream("/images/waiter-second.png"));

        ResizeMainChat.addListeners(mainChat);

        Circle clip = new Circle(0, 0, 30);
        clip.setLayoutX(30);
        clip.setLayoutY(30);
        profileImageContainer.setClip(clip);

        setOrderPane();

        contentRoot.setCursor(Cursor.DEFAULT);
        MoveRoot.move(moveBar, contentRoot);
    }
    private void waitForNewMessages(){
        messageService = new MessageService();
        messageService.setOnSucceeded(event -> {
            MessageService.lastMessageCheck = LocalDateTime.now();
            List<Message> newMessages = (List<Message>) messageService.getValue();
            newMessages.forEach(message -> {
                int index = mainChatBlock.getChildren().size();
                mainChatBlock.setId("new-message");

                ChatValue chat = chatsMap.get(message.getChatId());
                ListOrderedMap<LocalDate, Session> sessions = chat.getSessions();

                Session session = sessions.get(LocalDate.now());
                if (session == null) {
                    LocalDate sessionDate = LocalDate.now();

                    session = new Session();
                    session.setDate(sessionDate);
                    sessions.put(0, sessionDate, session);
                    session.getMessages().add(message);

                    if (mainChatValue != null && mainChatValue.getChatId() == message.getChatId()) {
                        mainChatValue.setDisplayedSessions(mainChatValue.getDisplayedSessions() + 1);
                        appendSession(session, mainChatBlock, mainChatValue, index);
                    }
                } else {
                    session.getMessages().add(message);
                    if (mainChatValue != null && mainChatValue.getChatId() == message.getChatId()) {
                        appendMessage(message, mainChatValue, (VBox) mainChatBlock.getChildren().get(index - 1));
                    }
                }
            });
            messageService.restart();
        });

        messageService.setOnFailed(event -> serviceFailed(messageService));

    }

    private void loadOlderHistory(ChatValue chatValue, VBox chatBlock) {
        int displayedSessions = chatValue.getDisplayedSessions();
        int loadedSessions = chatValue.getSessions().size();
        int nextPage = loadedSessions / pageSize;

        HBox sessionInfo = (HBox) chatBlock.getChildren().get(0);
        Text info = (Text) sessionInfo.lookup("Text");

        ListOrderedMap<LocalDate, Session> sessionsMap = chatValue.getSessions();
        List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
        List<Session> nextSessions;
        if (loadedSessions > displayedSessions) {

            nextSessions = chatSessions.subList(displayedSessions,
                    Math.min(displayedSessions + pageSize, loadedSessions));

            if (displayedSessions + nextSessions.size() == loadedSessions && !chatValue.isMoreSessions()) {
                info.setText("Beginning of the chat");
            }
            chatValue.setDisplayedSessions(displayedSessions + nextSessions.size());

            nextSessions.forEach(session -> appendSession(session, chatBlock, chatValue, 1));

        } else if (chatValue.isMoreSessions()) {
            nextSessions = getNextSessions(chatValue.getChatId(), nextPage, pageSize);
            if (nextSessions.size() < pageSize) {
                chatValue.setMoreSessions(false);
                info.setText("Beginning of the chat");
            }
            chatValue.setDisplayedSessions(displayedSessions + nextSessions.size());
            nextSessions.forEach(session -> {

                if (!sessionsMap.containsKey(session.getDate())) {
                    sessionsMap.put(session.getDate(), session);

                    appendSession(session, chatBlock, chatValue, 1);
                }

            });
        } else {
            info.setText("Beginning of the chat");
        }
    }

    private void appendChats(List<Chat> chats) {
        chats.forEach(chat -> {
            InputStream in;
            Image profilePicture;
            int userId;
            if (chat.getFirstUser().getId() == loggedUser.getId()) {
                try {
                    in = new BufferedInputStream(
                            new URL(chat.getSecondUser().getProfilePicture()).openStream());
                    profilePicture = new Image(in);
                    in.close();
                }catch(Exception e){
                    profilePicture = new Image(getClass().getResourceAsStream("/images/default-picture.png"));
                }
                userId = chat.getSecondUser().getId();
            } else {
                try {
                    in = new BufferedInputStream(
                            new URL(chat.getFirstUser().getProfilePicture()).openStream());
                    profilePicture = new Image(in);
                    in.close();
                }catch(Exception e){
                    profilePicture = new Image(getClass().getResourceAsStream("/images/default-picture.png"));
                }
                userId = chat.getFirstUser().getId();
            }
            ChatValue chatValue = new ChatValue(chat.getId(), userId, profilePicture);
            chat.getSessions().forEach(session -> chatValue.getSessions().put(session.getDate(), session));

            ImageView imageView = new ImageView(profilePicture);
            imageView.setFitHeight(44);
            imageView.setFitWidth(44);
            imageView.setLayoutX(3);
            imageView.setLayoutY(6);

            Pane imageContainer = new Pane(imageView);
            Circle clip = new Circle(25, 25, 25);
            imageContainer.setClip(clip);
            imageContainer.setMinHeight(50);
            imageContainer.setMinWidth(50);
            imageContainer.setMaxHeight(50);
            imageContainer.setMaxWidth(50);


            Pane shadow = new Pane(imageContainer);
            shadow.setMinHeight(50);
            shadow.setMinWidth(50);
            shadow.setMaxHeight(50);
            shadow.setMaxWidth(50);
            shadow.getStyleClass().add("imageShadow");
            shadow.setOnMouseClicked(this::setMainChat);
            shadow.setId(String.valueOf(chat.getId()));


            chatUsers.getChildren().add(shadow);
            chatsMap.put(chat.getId(), chatValue);
        });
    }

    private void setMainChat(MouseEvent event) {
        Pane container = (Pane) event.getSource();
        container.getStyleClass().set(0, "imageShadowPressed");
        chatInfo.setOpacity(0);

        int chatId = Integer.parseInt(container.getId());

        ChatValue chat = chatsMap.get(chatId);
        HBox sessionInfo = (HBox) mainChatBlock.getChildren().get(0);
        Text info = (Text) sessionInfo.lookup("Text");
        if (mainChatValue != null) {
            Pane currentImageView = (Pane) chatUsersScroll.lookup("#" + mainChatValue.getChatId());
            currentImageView.getStyleClass().set(0, "imageShadow");

        }

        if (mainChatValue != null && chatId == mainChatValue.getChatId()) {
            if (mainChat.isDisabled()) {
                container.getStyleClass().set(0, "imageShadowPressed");
                mainChat.setDisable(false);
                mainChat.setOpacity(1);
            } else {
                chatInfo.setOpacity(1);
                mainChat.setOpacity(0);
                mainChat.setDisable(true);

            }
        } else {
            mainChatBlock.setId("beginning");
            mainChatBlock.getChildren().remove(1, mainChatBlock.getChildren().size());
            mainChat.setDisable(false);
            mainChat.setOpacity(0);

            Timeline opacity = new Timeline(new KeyFrame(Duration.millis(200), event1 -> mainChat.setOpacity(1)));
            opacity.play();

            mainChatValue = chat;

            ListOrderedMap<LocalDate, Session> sessionsMap = mainChatValue.getSessions();
            List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
            List<Session> lastSessions = chatSessions.subList(0, Math.min(pageSize, chatSessions.size()));

            if (lastSessions.size() == pageSize) {
                info.setText("Scroll for more history");
                mainChatValue.setDisplayedSessions(pageSize);
            } else {
                info.setText("Beginning of the chat");
                mainChatValue.setMoreSessions(false);
                mainChatValue.setDisplayedSessions(lastSessions.size());
            }

            lastSessions.forEach(session -> appendSession(session, mainChatBlock, mainChatValue, 1));
        }
    }

    @FXML
    public void addNewMessage(){
        String messageText = mainChatTextArea.getText();
        int chatId = mainChatValue.getChatId();
        int receiverId = mainChatValue.getUserId();
        int index = mainChatBlock.getChildren().size();
        mainChatTextArea.clear();

        if (messageText.length() > 0){
            Message message = sendMessage(messageText, chatId, receiverId);
            ChatValue chat = chatsMap.get(chatId);
            ListOrderedMap<LocalDate, Session> sessions = chat.getSessions();

            mainChatBlock.setId("new-message");
            Session session = sessions.get(LocalDate.now());
            if (session == null) {
                LocalDate sessionDate = LocalDate.now();

                session = new Session();
                session.setDate(sessionDate);
                sessions.put(0, sessionDate, session);
                session.getMessages().add(message);

                if (mainChatValue.getChatId() == message.getChatId()) {
                    mainChatValue.setDisplayedSessions(mainChatValue.getDisplayedSessions() + 1);
                    appendSession(session, mainChatBlock, mainChatValue, index);
                }
            } else {
                session.getMessages().add(message);
                if (mainChatValue.getChatId() == message.getChatId()) {
                    appendMessage(message, mainChatValue, (VBox) mainChatBlock.getChildren().get(index - 1));
                }
            }
        }

    }
    private void appendSession(Session session, VBox chatBlock, ChatValue chatValue, int index) {

        Text date = new Text(dateFormatter.format(session.getDate()));
        TextFlow dateFlow = new TextFlow(date);
        dateFlow.setTextAlignment(TextAlignment.CENTER);

        HBox sessionDate = new HBox(dateFlow);
        HBox.setHgrow(dateFlow, Priority.ALWAYS);
        sessionDate.getStyleClass().add("session-date");

        VBox sessionBlock = new VBox(sessionDate);
        sessionBlock.setId(session.getDate().toString());
        session.getMessages()
                .forEach(message -> appendMessage(message, chatValue, sessionBlock));
        chatBlock.getChildren().add(index, sessionBlock);
    }

    private void appendMessage(Message message, ChatValue chat, VBox chatBlock) {
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

        if (message.getReceiverId() == loggedUser.getId()) {
            imageView.setImage(chat.getSecondUserPicture());
            text.setText(message.getMessage());
            time.setText("  " + timeFormatter.format(message.getTime()));
            textFlow.getChildren().addAll(text, time);
            hBox.setAlignment(Pos.TOP_LEFT);

        } else {
            imageView.setImage(userProfileImage);
            text.setText(message.getMessage());
            time.setText(timeFormatter.format(message.getTime()) + "  ");
            textFlow.getChildren().addAll(time, text);
            hBox.setAlignment(Pos.TOP_RIGHT);

        }

        boolean timeElapsed;
        int timeToElapse = 10;

        List<Node> messageBlocks = chatBlock.getChildren();
        if (messageBlocks.size() > 0 && messageBlocks.get(messageBlocks.size() - 1) instanceof VBox) {
            VBox lastBlock = (VBox) messageBlocks.get(messageBlocks.size() - 1);
            HBox lastMessage = (HBox) lastBlock.getChildren().get(lastBlock.getChildren().size() - 1);
            LocalTime lastBlockStartedDate;
            TextFlow firstTextFlow = (TextFlow) lastMessage.lookup("TextFlow");
            Text lastBlockStartedText = (Text) firstTextFlow.lookup(".time");
            lastBlockStartedDate = LocalTime.parse(lastBlockStartedText.getText().replaceAll("\\s+", ""));

            timeElapsed = java.time.Duration.between(lastBlockStartedDate, message.getTime()).toMinutes() > timeToElapse;

            if (message.getReceiverId() == loggedUser.getId()) {
                if (!timeElapsed && lastMessage.getStyleClass().get(0).startsWith("second-user-message")) {

                    hBox.getStyleClass().add("second-user-message");
                    hBox.getChildren().addAll(textFlow);
                    lastBlock.getChildren().add(hBox);

                } else {

                    hBox.getStyleClass().add("second-user-message-first");
                    hBox.getChildren().addAll(imageShadow, textFlow);
                    newBlock.getChildren().add(hBox);
                    chatBlock.getChildren().add(newBlock);

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
                    chatBlock.getChildren().add(newBlock);

                }
            }
        } else {

            if (message.getReceiverId() == loggedUser.getId()) {
                hBox.getStyleClass().add("second-user-message-first");
                hBox.getChildren().addAll(imageShadow, textFlow);
                newBlock.getChildren().add(hBox);
            } else {
                hBox.getStyleClass().add("user-message-first");
                hBox.getChildren().addAll(textFlow, imageShadow);
                newBlock.getChildren().add(hBox);
            }
            chatBlock.getChildren().add(newBlock);
        }
    }
    @FXML
    private void showNotifications() {
        notificationsView.setDisable(false);
        isNewNotificationChecked.set(true);

        ordersPane.setDisable(true);
        ordersPane.setOpacity(0);
    }

    @FXML
    private void showOrders() {
        notificationsView.setDisable(true);

        ordersPane.setDisable(false);
        ordersPane.setOpacity(1);
    }

    @FXML
    private void scrollToChats() {
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(1000), new KeyValue(
                        menuScroll.vvalueProperty(), 1)));
        animation.play();
        chatUsersScroll.setDisable(false);
        userInfoScroll.setDisable(true);
    }

    @FXML
    private void showProfile() {
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(1000), new KeyValue(
                        menuScroll.vvalueProperty(), 0)));
        animation.play();
        profileRoot.setOpacity(1);
        profileRoot.setDisable(false);
        userInfoScroll.setDisable(false);
        chatUsersScroll.setDisable(true);
    }

    @FXML
    public void showMenu(){
        profileRoot.setOpacity(0);
        profileRoot.setDisable(true);
    }

    @Override
    public void setStage() throws Exception{
        super.setStage();

        if (roleField.getText().equals("Chef")) {
            roleImage.setImage(chefImage);
        } else {
            roleImage.setImage(waiterImage);
        }

//        List<Chat> chats = getChats();
//        appendChats(chats);
    }

    @Override
    public void resetStage(){
        super.resetStage();

        chatUsers.getChildren().clear();

        mainChatBlock.getChildren().remove(1,mainChatBlock.getChildren().size());

        mainChat.setDisable(true);
        mainChat.setOpacity(0);
        mainChat.setLayoutX(217);
        mainChat.setLayoutY(231);
        mainChat.setPrefHeight(189);

        mainChatTextArea.setText(null);
        mainChatValue = null;

        userInfoScroll.setVvalue(0);
        menuScroll.setVvalue(0);
        chatUsersScroll.setVvalue(0);

        userInfoScroll.setDisable(false);
        ordersList.setDisable(false);

        profileRoot.setOpacity(0);
        profileRoot.setDisable(true);

        ordersPane.setDisable(false);
        ordersPane.setOpacity(1);

        ResizeRoot.resize = true;

        if(isButtonExpanded.get()){
            ExpandOrderPane.reverseOrder();
        }

        chatsMap.clear();
    }

    public void setOrderPane(){
        Rectangle rect = new Rectangle(orderContainer.getWidth(), orderContainer.getHeight());
        rect.heightProperty().bind(orderContainer.prefHeightProperty());
        rect.widthProperty().bind(orderContainer.prefWidthProperty());
        rect.setArcWidth(20);
        rect.setArcHeight(20);
        rect.getStyleClass().add("shadow");
        orderPane.setClip(rect);

        expandButton.prefWidthProperty().bind(((orderContainer.prefWidthProperty()
                .subtract(81.6))
                .divide(15))
                .add(28));
        expandButton.prefHeightProperty().bind(((orderContainer.prefHeightProperty()
                .subtract(81.6))
                .divide(30))
                .add(28));
        Rectangle dishesClip = new Rectangle();
        dishesClip.widthProperty().bind(dishesAnchor.widthProperty());
        dishesClip.heightProperty().bind(dishesAnchor.heightProperty());
        currentDishList.setClip(dishesClip);

        orderContainer.disableProperty().bind(isButtonExpanded.not());
        orderContainer.opacityProperty().bind(Bindings.createIntegerBinding(()->{
            if(action.get()){
                return 1;
            }
            return 0;
        },action));
    }

    private void bindProperties(Order currentOrder) {
        orderId.textProperty().bind(currentOrder.getId().asString());
        currentDishList.setItems(currentOrder.getDishes());
        createdDate.textProperty().bind(Bindings.createObjectBinding(()->
                dateFormatter.format(currentOrder.getCreated().get()),currentOrder.getCreated()));
        createdTime.textProperty().bind(Bindings.createObjectBinding(()->
                timeFormatter.format(currentOrder.getCreated().get()),currentOrder.getCreated()));
        updatedDate.textProperty().bind(Bindings.createObjectBinding(()->
                dateFormatter.format(currentOrder.getUpdated().get()),currentOrder.getUpdated()));
        updatedTime.textProperty().bind(Bindings.createObjectBinding(()->
                timeFormatter.format(currentOrder.getUpdated().get()),currentOrder.getUpdated()));
    }

    @FXML
    public void showCreated(){
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), createdDate);
        translate.setToX(30);
        translate.play();
    }

    @FXML
    public void hideCreated(){
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), createdDate);
        translate.setToX(0);
        translate.play();
    }

    @FXML
    public void showUpdated(){
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), updatedDate);
        translate.setToX(-30);
        translate.play();
    }

    @FXML
    public void hideUpdated(){
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), updatedDate);
        translate.setToX(0);
        translate.play();
    }

    private void ExpandOrderListeners() {
        ExpandOrderPane.contentRoot = contentRoot;
        ExpandOrderPane.orderPane = orderContainer;
        ExpandOrderPane.button = expandButton;
        ExpandOrderPane.contentPane = contentPane;
        ExpandOrderPane.scrollBar = ordersScrollBar;
        ExpandOrderPane.orderList = ordersList;
        ExpandOrderPane.dates = dates;

        ExpandOrderPane.setListeners();
    }

    private void expandOrder(MouseEvent event) {
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        String type = intersectedNode.getTypeSelector();
        if(type.equals("Button") || (!ExpandOrderPane.action.get() && type.equals("AnchorPane"))){
            currentPane = type.equals("AnchorPane") ? (AnchorPane) intersectedNode
                    : (AnchorPane) intersectedNode.getParent();
            currentContainer = (Pane)currentPane.getParent();
            cell =  (OrderListViewCell) currentContainer.getParent();
            bindProperties(cell.order);
            ExpandOrderPane.setCurrentOrder(event);

            if(intersectedNode.getTypeSelector().equals("Button"))
                expandOrderOnClick();

        }
    }
}