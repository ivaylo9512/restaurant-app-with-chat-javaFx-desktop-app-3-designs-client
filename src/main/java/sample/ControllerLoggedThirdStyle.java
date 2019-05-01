package sample;

import Animations.MoveRoot;
import Animations.ResizeRoot;
import Animations.TransitionResizeWidth;
import Helpers.ListViews.*;
import Helpers.Scrolls;
import Helpers.Services.MessageService;
import Helpers.Services.OrderService;
import Models.*;
import Models.Menu;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static Helpers.ServerRequests.*;
import static Helpers.Services.OrderService.mostRecentOrderDate;

public class ControllerLoggedThirdStyle {
    @FXML public ListView<Order> ordersList;
    @FXML public ListView<Dish> dishesList;
    @FXML public ListView<Menu> menuList,newOrderList;
    @FXML public ListView<TextField> notificationsList;
    @FXML public ListView<Chat> chatsList;
    @FXML public TextField firstNameField, lastNameField, countryField, ageField, menuSearch;
    @FXML public AnchorPane profileView, ordersView, chatsView, ordersMenu, chatsMenu, createRoot,
            userInfoFields, userInfoLabels, contentRoot, menuBar, mainChatContainer, secondChatContainer;
    @FXML public Pane profileImageClip;
    @FXML public Label roleField, usernameField, firstNameLabel, lastNameLabel, countryLabel,
            ageLabel, roleLabel, usernameLabel;
    @FXML VBox chatsContainer, mainChatBlock, secondChatBlock;
    @FXML ScrollPane mainChatScroll, secondChatScroll;
    @FXML TextArea mainChatTextArea, secondChatTextArea;
    @FXML ImageView profileImage;
    @FXML Button editButton;
    @FXML HBox notificationsInfo;
    @FXML Text chatsBtn;

    private Image userProfileImage;
    private MediaPlayer notificationSound;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private Map<Integer, ChatValue> chatsMap = new HashMap<>();
    private TreeMap<String, Menu> menuMap = new TreeMap<>();

    private User loggedUser;

    private MessageService messageService;
    private OrderService orderService;
    private AnchorPane currentView, currentMenu;
    private Text currentText;
    private Order currentOrder;

    private ChatValue mainChat;
    private ChatValue secondChat;

    @FXML
    public void initialize(){
        ordersList.setCellFactory(ordersCell -> new OrderListViewCellSecond());
        dishesList.setCellFactory(dishCell -> new DishListViewCell());
        menuList.setCellFactory(menuCell -> new MenuListViewCell());
        newOrderList.setCellFactory(menuCell -> new MenuListViewCell());
        chatsList.setCellFactory(chatCell -> new ChatsListViewCellSecond());

        waitForNewOrders();
        waitForNewMessages();


        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Menu> observableList = FXCollections.observableArrayList();
            searchMenu(newValue.toLowerCase()).forEach((s, menu) -> observableList.add(menu));
            menuList.setItems(observableList);
        });

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

        Media sound = new Media(getClass()
                .getResource("/notification.mp3")
                .toExternalForm());
        notificationSound = new MediaPlayer(sound);
        notificationSound.setOnEndOfMedia(() -> notificationSound.stop());

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

    public void setStage() throws Exception {
        loggedUser = loggedUserProperty.getValue();
        loggedUser.getRestaurant().getMenu().forEach(menu -> menuMap.put(menu.getName().toLowerCase(), menu));

        ObservableList<Order> orders = FXCollections.observableArrayList(loggedUser.getOrders().values());
        FXCollections.reverse(orders);

        ObservableList<Chat> chats = FXCollections.observableArrayList(getChats());
        setChatValues(chats);
        chatsList.setItems(chats);

        mostRecentOrderDate = getMostRecentOrderDate(loggedUser.getRestaurant().getId());

        orderService.start();
        messageService.start();

        menuList.setItems(FXCollections.observableArrayList(loggedUser.getRestaurant().getMenu()));

        ordersList.setItems(orders);

        InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
        userProfileImage = new Image(in);
        in.close();

        displayUserFields();

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
        userProfileImage = null;
        loggedUser = null;
        mainChat = null;
        secondChat = null;
        mainChatTextArea.setText(null);
        secondChatTextArea.setText(null);
        menuSearch.setText("");

        menuBar.setOpacity(0);

        mainChatBlock.getChildren().remove(1, mainChatBlock.getChildren().size());
        secondChatBlock.getChildren().remove(1, secondChatBlock.getChildren().size());

        menuMap.clear();

        newOrderList.getItems().clear();
        menuList.getItems().clear();
        chatsList.getItems().clear();
        ordersList.getItems().clear();
        notificationsList.getItems().clear();

        resetUserFields();

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

        Platform.runLater(() -> {
            orderService.reset();
            messageService.reset();
        });
    }
    private void displayUserFields() {
        usernameLabel.setText(loggedUser.getUsername());
        firstNameLabel.setText(loggedUser.getFirstName());
        lastNameLabel.setText(loggedUser.getLastName());
        countryLabel.setText(loggedUser.getCountry());
        ageLabel.setText(String.valueOf(loggedUser.getAge()));
        roleLabel.setText(loggedUser.getRole());

        usernameField.setText(loggedUser.getUsername());
        firstNameField.setText(loggedUser.getFirstName());
        lastNameField.setText(loggedUser.getLastName());
        countryField.setText(loggedUser.getCountry());
        ageField.setText(String.valueOf(loggedUser.getAge()));
        roleField.setText(loggedUser.getRole());

        profileImage.setImage(userProfileImage);
    }
    private void resetUserFields() {
        usernameLabel.setText(null);
        firstNameLabel.setText(null);
        lastNameLabel.setText(null);
        countryLabel.setText(null);
        ageLabel.setText(null);
        roleLabel.setText(null);

        usernameField.setText(null);
        firstNameField.setText(null);
        lastNameField.setText(null);
        countryField.setText(null);
        ageField.setText(null);
        roleField.setText(null);

        profileImage.setImage(null);
    }
    @FXML
    public void editUserInfo(){
        userInfoLabels.setDisable(true);
        userInfoLabels.setOpacity(0);
        userInfoFields.setDisable(false);
        userInfoFields.setOpacity(1);

        editButton.setText("Save");
        editButton.setOnMouseClicked(event -> saveUserInfo());
    }

    private void saveUserInfo() {
        userInfoLabels.setDisable(false);
        userInfoLabels.setOpacity(1);
        userInfoFields.setDisable(true);
        userInfoFields.setOpacity(0);

        boolean edited = !firstNameLabel.getText().equals(firstNameField.getText()) || !lastNameLabel.getText().equals(lastNameField.getText()) ||
                !ageLabel.getText().equals(ageField.getText()) || !countryLabel.getText().equals(countryField.getText());

        if (edited) {
            User user = sendUserInfo(firstNameField.getText(), lastNameField.getText(),
                    ageField.getText(), countryField.getText());

            if (user != null) {
                loggedUser.setFirstName(user.getFirstName());
                loggedUser.setLastName(user.getLastName());
                loggedUser.setAge(user.getAge());
                loggedUser.setCountry(user.getCountry());

                firstNameLabel.setText(user.getFirstName());
                lastNameLabel.setText(user.getLastName());
                ageLabel.setText(String.valueOf(user.getAge()));
                countryLabel.setText(user.getCountry());
            }
        }
        editButton.setText("Edit");
        editButton.setOnMouseClicked(event -> editUserInfo());
    }
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
            try {
                InputStream in;
                ChatValue chatValue;
                Image profilePicture;
                if (chat.getFirstUser().getId() == loggedUser.getId()) {
                    in = new BufferedInputStream(
                            new URL(chat.getSecondUser().getProfilePicture()).openStream());
                    profilePicture = new Image(in);

                    chat.getSecondUser().setImage(profilePicture);

                    chatValue = new ChatValue(chat.getId(), chat.getSecondUser().getId(), profilePicture);
                    chat.getSessions().forEach(session -> chatValue.getSessions().put(session.getDate(), session));
                } else {
                    in = new BufferedInputStream(
                            new URL(chat.getFirstUser().getProfilePicture()).openStream());
                    profilePicture = new Image(in);

                    chat.getFirstUser().setImage(profilePicture);

                    chatValue = new ChatValue(chat.getId(), chat.getFirstUser().getId(), profilePicture);
                    chat.getSessions().forEach(session -> chatValue.getSessions().put(session.getDate(), session));
                }
                in.close();
                chatsMap.put(chat.getId(), chatValue);

            } catch (IOException e) {
                e.printStackTrace();
            }
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
                showLoggedStageAlert(e.getMessage());
            }
        }else{
            showLoggedStageAlert("You must be a chef to update the dish status.");
        }
    }
    private void waitForNewOrders() {
        orderService = new OrderService();
        orderService.setOnSucceeded(event -> {
            List<Order> newOrders = (List<Order>) orderService.getValue();

            if (newOrders.size() > 0) {
                Order mostRecentNewOrder = newOrders.get(0);
                if (mostRecentNewOrder.getCreated().isAfter(mostRecentNewOrder.getUpdated())) {
                    mostRecentOrderDate = mostRecentNewOrder.getCreated();
                } else {
                    mostRecentOrderDate = mostRecentNewOrder.getUpdated();
                }
            }

            updateNewOrders(newOrders);
            orderService.restart();
        });

        orderService.setOnFailed(event -> serviceFailed(orderService));
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

    private void updateNewOrders(List<Order> newOrders) {
        newOrders.forEach(order -> {
            int orderId = order.getId();
            Order orderValue = loggedUser.getOrders().get(orderId);

            if (orderValue != null) {
                order.getDishes().forEach(dish -> {

                    if(currentOrder != null && currentOrder.getId() == orderId) {
                        Label ready = (Label) dishesList.lookup("#dish" + dish.getId());

                        if (ready != null && ready.getText().equals("X") && dish.getReady()) {
                            addNotification(dish.getName() + " from order " + orderId + " is ready.");
                            ready.setText("O");
                            ready.setUserData("ready");
                        }
                    }
                });

                if (order.isReady()) {
                    addNotification("Order " + orderId + " is ready.");
                }

            } else {

                ordersList.getItems().add(0, order);
                if(order.getUserId() != loggedUser.getId()){
                    addNotification("New order created " + orderId);
                }
            }
            loggedUser.getOrders().put(orderId, order);
        });
    }

    private void serviceFailed(Service service){

        if(service.getException() != null && service.isRunning()) {
            if (service.getException().getMessage().equals("Jwt token has expired.")) {
                logOut();
                showLoginStageAlert("Session has expired.");

            } else if(service.getException().getMessage().equals("Socket closed")) {
                service.reset();

            }else{
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(3000), event -> service.restart()));
                timeline.play();
            }
        }
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
                    showLoggedStageAlert(e.getMessage());
                }
            } else {
                showLoggedStageAlert("Order must have at least one dish.");
            }
        } else {
            showLoggedStageAlert("You must be a server to create orders.");
        }
    }
    private SortedMap<String, Menu> searchMenu(String prefix) {
        return menuMap.subMap(prefix, prefix + Character.MAX_VALUE);
    }
    @FXML
    public void addMenuItem(){
        Menu menuItem = menuList.getSelectionModel().getSelectedItem();
        newOrderList.getItems().add(0, menuItem);
    }
    @FXML
    public void removeMenuItem(){
        Menu menuItem = newOrderList.getSelectionModel().getSelectedItem();
        newOrderList.getItems().remove(menuItem);
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

    @FXML
    public void logOut(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        LoggedThirdStyle.stage.close();
        if(LoginThirdStyle.stage != null) {
            LoginThirdStyle.stage.show();
        }else{
            try{
                LoginThirdStyle.displayLoginScene();
            } catch (Exception e) {
                LoginFirstStyle.stage.show();
                DialogPane dialogPane = LoginFirstStyle.alert.getDialogPane();
                dialogPane.setContentText(e.getMessage());
                LoginFirstStyle.alert.showAndWait();

            }
        }
    }
    @FXML
    public void showLoggedFirstStyle(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        LoggedThirdStyle.stage.close();
        if(LoggedFirstStyle.stage != null){
            LoggedFirstStyle.stage.show();

        }else {
            try {
                LoggedFirstStyle.displayLoggedScene();
            } catch (Exception e) {
                LoginFirstStyle.stage.show();
                DialogPane dialogPane = LoginFirstStyle.alert.getDialogPane();
                dialogPane.setContentText(e.getMessage());
                LoginFirstStyle.alert.showAndWait();

            }
        }
    }
    @FXML
    public void showLoggedSecondStyle(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        LoggedThirdStyle.stage.close();
        if(LoggedSecondStyle.stage != null){
            LoggedSecondStyle.stage.show();
        }else {
            try {
                LoggedSecondStyle.displayLoggedScene();
            } catch (Exception e) {
                LoginFirstStyle.stage.show();
                showLoginStageAlert(e.getMessage());
            }
        }
    }
    private void showLoggedStageAlert(String message) {
        if(!LoggedThirdStyle.alert.isShowing()) {
            DialogPane dialog = LoggedThirdStyle.alert.getDialogPane();
            dialog.setContentText(message);
            LoggedThirdStyle.alert.showAndWait();
        }
    }
    private void showLoginStageAlert(String message) {
        if(!LoginThirdStyle.alert.isShowing()) {
            DialogPane dialog = LoginThirdStyle.alert.getDialogPane();
            dialog.setContentText(message);
            LoginThirdStyle.alert.showAndWait();
        }
    }
}
