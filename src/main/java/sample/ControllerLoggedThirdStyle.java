package sample;

import Animations.MoveRoot;
import Animations.ResizeRoot;
import Animations.TransitionResizeWidth;
import Application.RestaurantApplication;
import Helpers.ListViews.*;
import Helpers.Scrolls;
import Application.MessageService;
import Application.OrderService;
import Models.*;
import Models.Menu;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.apache.commons.collections4.map.ListOrderedMap;
import sample.base.ControllerLogged;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static Application.RestaurantApplication.*;
import static Helpers.ServerRequests.*;
import static Application.OrderService.mostRecentOrderDate;

public class ControllerLoggedThirdStyle extends ControllerLogged implements Controller {
    @FXML public ListView<Order> ordersList;
    @FXML public ListView<TextField> notificationsList;
    @FXML public ListView<Chat> chatsList;
    @FXML public AnchorPane profileView, ordersView, chatsView, ordersMenu, chatsMenu, createRoot, contentRoot, menuBar, mainChatContainer, secondChatContainer;
    @FXML public Pane profileImageContainer, profileImageClip;
    @FXML VBox chatsContainer, mainChatBlock, secondChatBlock;
    @FXML ScrollPane mainChatScroll, secondChatScroll;
    @FXML TextArea mainChatTextArea, secondChatTextArea;
    @FXML HBox notificationsInfo;
    @FXML Text chatsBtn;

    private Map<Integer, ChatValue> chatsMap = new HashMap<>();

    private User loggedUser;

    private MessageService messageService;
    private AnchorPane currentView, currentMenu;
    private Text currentText;
    private Order currentOrder;

    private ChatValue mainChat;
    private ChatValue secondChat;

    @Override
    public void initialize(){
        super.initialize();

        ordersList.setCellFactory(ordersCell -> new OrderListViewCellSecond());
        chatsList.setCellFactory(chatCell -> new ChatsListViewCellSecond());

        waitForNewMessages();


        mainChatBlock.idProperty().addListener((observable1, oldValue1, newValue1) -> {
            if ((newValue1.equals("append") || newValue1.equals("beginning-append")) && mainChat != null) {
                loadOlderHistory(mainChat, mainChatBlock);
            }
        });

        secondChatBlock.idProperty().addListener((observable1, oldValue1, newValue1) -> {
            if ((newValue1.equals("append") || newValue1.equals("beginning-append")) && secondChat != null) {
                loadOlderHistory(secondChat, secondChatBlock);
            }
        });
        secondChatTextArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                addNewMessage(secondChatTextArea, secondChat, secondChatBlock);
                event.consume();
            }
        });

        mainChatTextArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                addNewMessage(mainChatTextArea, mainChat, mainChatBlock);
                event.consume();
            }
        });

        Scrolls scrolls = new Scrolls(mainChatScroll, secondChatScroll, mainChatTextArea, secondChatTextArea);
        scrolls.manageScrollsThirdStyle();

        Rectangle clip = new Rectangle();
        clip.heightProperty().bind(createRoot.heightProperty());
        clip.widthProperty().bind(createRoot.widthProperty());
        createRoot.setClip(clip);

        Circle profileClip = new Circle(40.5, 40.5, 40.5);
        profileImageClip.setClip(profileClip);

        chatsContainer.getChildren().remove(mainChatContainer);
        chatsContainer.getChildren().remove(secondChatContainer);

        mainChatBlock.prefWidthProperty().bind(mainChatScroll.widthProperty().subtract(16));
        secondChatBlock.prefWidthProperty().bind(secondChatScroll.widthProperty().subtract(16));

        MoveRoot.move(menuBar, contentRoot);
        ResizeRoot.addListeners(contentRoot);
    }

    public void setStage() throws Exception{
        ObservableList<Order> orders = FXCollections.observableArrayList(loggedUser.getOrders().values());
        FXCollections.reverse(orders);

        ObservableList<Chat> chats = FXCollections.observableArrayList(getChats());//Todo
        setChatValues(chats);
        chatsList.setItems(chats);

        mostRecentOrderDate = getMostRecentOrderDate(orderManager.userRestaurant.getId());

        orderService.start();
        messageService.start();

        userMenu.setAll(orderManager.userMenu.values());
        ordersList.setItems(orders);

        loginAnimation();
    }

    private void loginAnimation() {
        menuBar.setTranslateX(contentRoot.getPrefWidth() / 2 - menuBar.getPrefWidth() / 2);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(700), menuBar);
        fadeIn.setDelay(Duration.millis(1000));
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public void resetStage(){
        mostRecentOrderDate = null;
        loggedUser = null;
        mainChat = null;
        secondChat = null;
        mainChatTextArea.setText(null);
        secondChatTextArea.setText(null);
        menuSearch.setText("");

        menuBar.setOpacity(0);

        mainChatBlock.getChildren().remove(1, mainChatBlock.getChildren().size());
        secondChatBlock.getChildren().remove(1, secondChatBlock.getChildren().size());

        newOrderList.getItems().clear();
        menuList.getItems().clear();
        chatsList.getItems().clear();
        ordersList.getItems().clear();
        notificationsList.getItems().clear();

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        contentRoot.setPrefWidth(contentRoot.getMinWidth());
        contentRoot.setPrefHeight(contentRoot.getMinHeight());

        contentRoot.setLayoutY((primaryScreenBounds.getHeight() - contentRoot.getPrefHeight()) / 2);
        contentRoot.setLayoutX((primaryScreenBounds.getWidth() - contentRoot.getPrefWidth()) / 2);

        if(currentText != null){
            currentText.getStyleClass().remove("strikethrough");
        }
        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);
        }
        if(currentMenu != null){
            currentMenu.setOpacity(0);
            currentMenu.setDisable(true);
        }

        currentView = null;
        currentMenu= null;

        orderService.cancel();
        messageService.cancel();
    }
    
//    @FXML
//    public void editUserInfo(){
//        userInfoLabels.setDisable(true);
//        userInfoLabels.setOpacity(0);
//        userInfoFields.setDisable(false);
//        userInfoFields.setOpacity(1);
//
//        editButton.setText("Save");
//        editButton.setOnMouseClicked(event -> saveUserInfo());
//    }
//
//    private void saveUserInfo() {
//        userInfoLabels.setDisable(false);
//        userInfoLabels.setOpacity(1);
//        userInfoFields.setDisable(true);
//        userInfoFields.setOpacity(0);
//
//        boolean edited = !firstNameLabel.getText().equals(firstNameField.getText()) || !lastNameLabel.getText().equals(lastNameField.getText()) ||
//                !ageLabel.getText().equals(ageField.getText()) || !countryLabel.getText().equals(countryField.getText());
//
//        if (edited) {
//            User user = sendUserInfo(firstNameField.getText(), lastNameField.getText(),
//                    ageField.getText(), countryField.getText());
//
//            if (user != null) {
//                loggedUser.setFirstName(user.getFirstName());
//                loggedUser.setLastName(user.getLastName());
//                loggedUser.setAge(user.getAge());
//                loggedUser.setCountry(user.getCountry());
//
//                firstNameLabel.setText(user.getFirstName());
//                lastNameLabel.setText(user.getLastName());
//                ageLabel.setText(String.valueOf(user.getAge()));
//                countryLabel.setText(user.getCountry());
//            }
//        }
//        editButton.setText("Edit");
//        editButton.setOnMouseClicked(event -> editUserInfo());
//    }
    @FXML
    public void displayOrdersView(MouseEvent event){
        Text clickedText = (Text)event.getSource();
        setStrikeThrough(clickedText);
        displayView(ordersView, ordersMenu);
    }
    @FXML
    public void displayProfileView(MouseEvent event){
        Text clickedText = (Text)event.getSource();
        setStrikeThrough(clickedText);
        displayView(profileView, chatsMenu);
    }
    @FXML
    public void displayChatsView(MouseEvent event){
        Text clickedText = (Text)event.getSource();
        setStrikeThrough(clickedText);
        displayView(chatsView, chatsMenu);
    }

    private void setStrikeThrough(Text clickedText) {
        clickedText.getStyleClass().add("strikethrough");
        if(currentText != null) {
            currentText.getStyleClass().remove("strikethrough");
        }
        currentText = clickedText;
    }

    private void setChatValues(List<Chat> chats) {
        chats.forEach(chat -> {
            InputStream in;
            Image profilePicture;
            User user;

            if (chat.getFirstUser().getId() == loggedUser.getId()) {
                user = chat.getSecondUser();
                try {
                    in = new BufferedInputStream(
                            new URL(chat.getSecondUser().getProfilePicture()).openStream());
                    profilePicture = new Image(in);
                    in.close();
                }catch(Exception e){
                    profilePicture = new Image(getClass().getResourceAsStream("/images/default-picture.png"));
                }
            } else {
                user= chat.getFirstUser();
                try {
                    in = new BufferedInputStream(
                            new URL(chat.getFirstUser().getProfilePicture()).openStream());
                    profilePicture = new Image(in);
                    in.close();
                }catch(Exception e){
                    profilePicture = new Image(getClass().getResourceAsStream("/images/default-picture.png"));
                }
            }
            user.setImage(profilePicture);

            ChatValue chatValue = new ChatValue(chat.getId(), user.getId(), profilePicture);
            chat.getSessions().forEach(session -> chatValue.getSessions().put(session.getDate(), session));
            chatsMap.put(chat.getId(), chatValue);
        });
    }

    public void setChat(MouseEvent event) {
        if(chatsView.getOpacity() == 0) {
            displayView(chatsView, chatsMenu);

            currentText.getStyleClass().remove("strikethrough");
            chatsBtn.getStyleClass().add("strikethrough");
            currentText = chatsBtn;
        }

        Chat selectedChat = chatsList.getSelectionModel().getSelectedItem();

        GridPane container = (GridPane) event.getSource();
        VBox name = (VBox) container.getChildren().get(1);
        if(selectedChat != null) {

            VBox chatBlock = null;
            int chatId = selectedChat.getId();
            ChatValue chat = chatsMap.get(chatId);

            if (mainChat != null && mainChat.getChatId() == chatId){
                mainChat = null;

                mainChatBlock.getChildren().remove(1, mainChatBlock.getChildren().size());

                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), event1 ->
                        chatsContainer.getChildren().remove(mainChatContainer)));
                timeline.play();

                TransitionResizeWidth resizeWidth = new TransitionResizeWidth(Duration.millis(500), name, 133);
                resizeWidth.play();
            } else if (secondChat != null && secondChat.getChatId() == chatId){
                secondChat = null;

                secondChatBlock.getChildren().remove(1, secondChatBlock.getChildren().size());

                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), event1 ->
                        chatsContainer.getChildren().remove(secondChatContainer)));
                timeline.play();

                TransitionResizeWidth resizeWidth = new TransitionResizeWidth(Duration.millis(500), name, 133);
                resizeWidth.play();
            }else if(mainChat == null){
                chatBlock = mainChatBlock;
                mainChat = chat;

                chatsContainer.getChildren().add(0, mainChatContainer);
                TransitionResizeWidth resizeWidth = new TransitionResizeWidth(Duration.millis(500), name, 0);
                resizeWidth.play();
            }else if (secondChat == null){
                chatBlock = secondChatBlock;
                secondChat = chat;

                chatsContainer.getChildren().add(2, secondChatContainer);

                TransitionResizeWidth resizeWidth = new TransitionResizeWidth(Duration.millis(500), name, 0);
                resizeWidth.play();
            } else{
                chatBlock = secondChatBlock;

                TransitionResizeWidth resizeWidth = new TransitionResizeWidth(Duration.millis(500), name, 0);
                resizeWidth.play();

                GridPane currentContainer = (GridPane) contentRoot.lookup("#" + secondChat.getChatId());
                VBox currentName = (VBox) currentContainer.getChildren().get(1);

                TransitionResizeWidth reverseWidth = new TransitionResizeWidth(Duration.millis(500), currentName, 133);
                reverseWidth.play();

                secondChat = chat;
            }

            if(chatBlock != null) {
                chatBlock.setId("beginning");
                chatBlock.getChildren().remove(1, chatBlock.getChildren().size());
                HBox sessionInfo = (HBox) chatBlock.getChildren().get(0);
                Text info = (Text) sessionInfo.lookup("Text");

                ListOrderedMap<LocalDate, Session> sessionsMap = chat.getSessions();
                List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
                List<Session> lastSessions = chatSessions.subList(0, Math.min(pageSize, chatSessions.size()));

                if (lastSessions.size() == pageSize) {
                    info.setText("Scroll for more history");
                    chat.setDisplayedSessions(pageSize);
                } else {
                    info.setText("Beginning of the chat");
                    chat.setMoreSessions(false);
                    chat.setDisplayedSessions(lastSessions.size());
                }

                for (Session session : lastSessions) {
                    appendSession(session, chatBlock, chat, 1);
                }

            }
        }
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
    private void appendSession(Session session, VBox chatBlock, ChatValue chatValue, int index) {

        Text date = new Text(dateFormatter.format(session.getDate()));
        TextFlow dateFlow = new TextFlow(date);
        dateFlow.setTextAlignment(TextAlignment.CENTER);

        HBox sessionDate = new HBox(dateFlow);
        sessionDate.setAlignment(Pos.CENTER);
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

        Circle clip = new Circle(20.5, 20.5, 20.5);

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

        imageShadow.setViewOrder(1);
        textFlow.setViewOrder(5);

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
    public void sendMessageMainChat(){
        addNewMessage(mainChatTextArea, mainChat, mainChatBlock);
    }

    @FXML
    public void sendMessageSecondChat(){
        addNewMessage(secondChatTextArea, secondChat, secondChatBlock);
    }

    public void addNewMessage(TextArea textArea, ChatValue chatValue, VBox chatBlock){
        String messageText = textArea.getText();
        int chatId = chatValue.getChatId();
        int receiverId = chatValue.getUserId();
        int index = chatBlock.getChildren().size();
        textArea.clear();

        if (messageText.length() > 0){
            Message message = sendMessage(messageText, chatId, receiverId);

            ChatValue chat = chatsMap.get(chatId);
            ListOrderedMap<LocalDate, Session> sessions = chat.getSessions();

            chatBlock.setId("new-message");
            Session session = sessions.get(LocalDate.now());
            if (session == null) {
                LocalDate sessionDate = LocalDate.now();

                session = new Session();
                session.setDate(sessionDate);
                sessions.put(0, sessionDate, session);
                session.getMessages().add(message);

                if (chatValue.getChatId() == message.getChatId()) {
                    chatValue.setDisplayedSessions(chatValue.getDisplayedSessions() + 1);
                    appendSession(session, chatBlock, chatValue, index);
                }
            } else {
                session.getMessages().add(message);
                if (chatValue.getChatId() == message.getChatId()) {
                    appendMessage(message, chatValue, (VBox) chatBlock.getChildren().get(index - 1));
                }
            }
        }
    }

    private void displayView(AnchorPane requestedView, AnchorPane requestedMenu){
        Timeline delayView = new Timeline(new KeyFrame(Duration.millis(1), event -> {
            if (currentMenu != null) {
                currentMenu.setOpacity(0);
                currentMenu.setDisable(true);
            }

            requestedMenu.setOpacity(1);
            requestedMenu.setDisable(false);
            requestedView.setOpacity(1);
            requestedView.setDisable(false);

            currentMenu = requestedMenu;
            currentView = requestedView;
        }));
        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);

            delayView.play();
        }else{
            animateMenuBar();

            delayView.setDelay(Duration.millis(1000));
            delayView.play();
        }
    }

    private void animateMenuBar() {
        TranslateTransition animateBar = new TranslateTransition(Duration.millis(1000), menuBar);
        animateBar.setToX(0);
        animateBar.play();
    }

    public void showOrder(){
        Order selectedOrder = ordersList.getSelectionModel().getSelectedItem();
        if(selectedOrder != null){
            currentOrder = loggedUser.getOrders().get(selectedOrder.getId());
            currentOrder.getDishes().forEach(dish -> dish.setOrderId(currentOrder.getId()));

            dishesList.setItems(FXCollections.observableArrayList(currentOrder.getDishes()));

        }
    }
    public void updateDishStatus(Dish dish, Label ready) {

        if(loggedUser.getRole().equals("Chef")){
            try {
                updateDishState(dish.getOrderId(), dish.getId());
                dish.setReady(true);
                ready.setText("O");

            } catch (Exception e) {
                stageManager.showAlert(e.getMessage());
            }
        }else{
            stageManager.showAlert("You must be a chef to update the dish status.");
        }
    }

    private void waitForNewMessages(){
        messageService = new MessageService();
        messageService.setOnSucceeded(event -> {
            MessageService.lastMessageCheck = LocalDateTime.now();
            List<Message> newMessages = (List<Message>) messageService.getValue();
            newMessages.forEach(message -> {

                ChatValue chat = chatsMap.get(message.getChatId());
                ListOrderedMap<LocalDate, Session> sessions = chat.getSessions();
                ChatValue chatValue = null;
                VBox chatBlock = null;
                int index = 0;
                if (mainChat != null && mainChat.getChatId() == message.getChatId()) {
                    chatBlock = mainChatBlock;
                    chatValue = mainChat;

                    index = mainChatBlock.getChildren().size();
                    mainChatBlock.setId("new-message");
                }else if(secondChat != null && secondChat.getChatId() == message.getChatId()) {
                    chatBlock = secondChatBlock;
                    chatValue = secondChat;

                    index = secondChatBlock.getChildren().size();
                    secondChatBlock.setId("new-message");
                }

                Session session = sessions.get(LocalDate.now());
                if (session == null) {
                    LocalDate sessionDate = LocalDate.now();

                    session = new Session();
                    session.setDate(sessionDate);
                    sessions.put(0, sessionDate, session);
                    session.getMessages().add(message);

                    if (chatValue != null) {
                        chatValue.setDisplayedSessions(chatValue.getDisplayedSessions() + 1);
                        appendSession(session, chatBlock, chatValue, index);
                    }
                } else {
                    session.getMessages().add(message);
                    if (chatValue != null) {
                        appendMessage(message, chatValue, (VBox) chatBlock.getChildren().get(index - 1));
                    }
                }
            });
            messageService.restart();
        });

        messageService.setOnFailed(event -> serviceFailed(messageService));
    }

    @FXML
    public void createNewOrder() {
        List<Dish> dishes = new ArrayList<>();
        newOrderList.getItems().forEach(menuItem -> dishes.add(new Dish(menuItem.getName())));

        if (loggedUser.getRole().equals("Server")) {
            if (dishes.size() > 0) {
                try {
                    sendOrder(new Order(dishes));
                    newOrderList.getItems().clear();
                } catch (Exception e) {
                    stageManager.showAlert(e.getMessage());
                }
            } else {
                stageManager.showAlert("Order must have at least one dish.");
            }
        } else {
            stageManager.showAlert("You must be a server to create orders.");
        }
    }

    private void addNotification(String notification) {
        notificationsInfo.setOpacity(0);
        notificationsInfo.setDisable(true);

        TextField textField = new TextField(notification);
        textField.setEditable(false);
        textField.setDisable(true);

        notificationsList.getItems().add(0, textField);
        notificationSound.play();
    }
    @FXML
    public void removeNotification(){
        notificationsList.getItems().remove(notificationsList.getFocusModel().getFocusedItem());
        if(notificationsList.getItems().size() == 0){
            notificationsInfo.setOpacity(1);
            notificationsInfo.setDisable(false);
        }
    }
}
