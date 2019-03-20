package sample;

import Animations.ExpandOrderPane;
import Animations.MoveRoot;
import Animations.TransitionResizeHeight;
import Animations.ResizeMainChat;
import Helpers.Services.MessageService;
import Helpers.Services.OrderService;
import Helpers.Scrolls;
import Helpers.MenuListViewCell;
import Models.*;
import Models.Menu;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.io.*;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static Helpers.ServerRequests.*;


public class ControllerLoggedFirstStyle {
    @FXML ScrollPane menuScroll, userInfoScroll, chatUsersScroll,
            ordersScroll, mainChatScroll, notificationsScroll;
    @FXML VBox mainChatBlock, chatUsers, notificationBlock;
    @FXML FlowPane ordersFlow, notificationInfo, chatInfo, userInfo, userInfoEditable;
    @FXML Label firstNameLabel, lastNameLabel, countryLabel, ageLabel, roleLabel, roleField;
    @FXML TextField firstNameField, lastNameField, countryField, ageField;
    @FXML AnchorPane contentRoot, contentPane, mainChat, ordersPane;
    @FXML Pane moveBar, notificationIcon;
    @FXML TextArea mainChatTextArea;
    @FXML ImageView roleImage;
    @FXML TextField menuSearch;
    @FXML ListView<Menu> menu, newOrderMenu;

    public static LocalDateTime mostRecentOrderDate;

    private TreeMap<String, Menu> menuMap = new TreeMap<>();
    private HashMap<Integer, ChatValue> chatsMap = new HashMap<>();
    private Image userProfileImage;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    private ChatValue mainChatValue;
    private MediaPlayer notificationSound;
    private MessageService messageService;

    @FXML
    public void initialize() throws Exception {
        mapper.registerModule(new JavaTimeModule());

        loggedUser.getRestaurant().getMenu().forEach(menu -> menuMap.put(menu.getName().toLowerCase(), menu));
        loggedUser.getRestaurant().getOrders().forEach(this::appendOrder);

        InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
        userProfileImage = new Image(in);
        in.close();

        displayUserInfo();

        newOrderMenu.setCellFactory(menuCell -> new MenuListViewCell());
        menu.setCellFactory(menuCell -> new MenuListViewCell());
        menu.setItems(FXCollections.observableArrayList(loggedUser.getRestaurant().getMenu()));

        menu.setOnMouseClicked(event -> {
            Menu menuItem = menu.getSelectionModel().getSelectedItem();
            newOrderMenu.getItems().add(0, menuItem);
        });
        newOrderMenu.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Menu menuItem = newOrderMenu.getSelectionModel().getSelectedItem();
            newOrderMenu.getItems().remove(menuItem);
        });
        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            SortedMap<String, Menu> currentSearch = searchMenu(newValue.toLowerCase());
            ObservableList<Menu> observableList = FXCollections.observableArrayList();
            currentSearch.forEach((s, menu) -> observableList.add(menu));
            menu.setItems(observableList);
        });


        try {
            List<Chat> chats = getChats();
            appendChats(chats);
            mostRecentOrderDate = getMostRecentOrderDate(loggedUser.getRestaurant().getId());
        }catch (Exception e){
            Alert alert = LoginFirstStyle.alert;
            DialogPane dialog = alert.getDialogPane();
            dialog.setContentText(e.getMessage());
            alert.showAndWait();

            throw new RuntimeException("Stopping logged scene initialization with exception: " + e.getMessage());
        }


        waitForNewOrders();
        waitForNewMessages();

        ExpandOrderListeners();

        Scrolls scrolls = new Scrolls(menuScroll, userInfoScroll, chatUsersScroll, ordersScroll,
                mainChatScroll, notificationsScroll, mainChatTextArea);


        mainChatBlock.idProperty().addListener((observable1, oldValue1, newValue1) -> {
            if (newValue1.equals("append") || newValue1.equals("beginning-append")) {
                loadOlderHistory(mainChatValue, mainChatBlock);
            }
        });

        mainChatTextArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                newMessage();
                event.consume();
            }
        });
        Media sound = new Media(getClass()
                .getResource("/notification.mp3")
                .toExternalForm());
        notificationSound = new MediaPlayer(sound);
        notificationSound.setOnEndOfMedia(() -> notificationSound.stop());

        notificationBlock.prefWidthProperty().bind(notificationsScroll.widthProperty().subtract(20));
        contentRoot.getChildren().remove(userInfoEditable);
        ResizeMainChat.addListeners(mainChat);
        MoveRoot.move(moveBar, contentRoot);


    }

    private void waitForNewMessages(){
        messageService = new MessageService();
        messageService.start();
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

        messageService.setOnFailed(event -> serviceFail(messageService));

    }
    private void waitForNewOrders() {
        OrderService orderService = new OrderService();
        orderService.start();
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

        orderService.setOnFailed(event -> serviceFail(orderService));
    }

    private void serviceFail(Service service){
        if (service.getException().getMessage().equals("Jwt token has expired.")
                && LoggedFirstStyle.stage != null && !LoginFirstStyle.stage.isShowing()) {
            LoggedFirstStyle.stage.close();
            LoginFirstStyle.stage.show();

            Alert alert = LoginFirstStyle.alert;
            DialogPane dialog = alert.getDialogPane();
            dialog.setContentText("Session has expired.");
            alert.showAndWait();

            service.reset();
        } else if(LoginFirstStyle.stage.isShowing()) {
            service.reset();
        }else{
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(3000), event -> service.restart()));
            timeline.play();
        }
    }

    private SortedMap<String, Menu> searchMenu(String prefix) {
        return menuMap.subMap(prefix, prefix + Character.MAX_VALUE);
    }

    private void updateNewOrders(List<Order> newOrders) {
        newOrders.forEach(order -> {
            AnchorPane orderPane = (AnchorPane) contentRoot.lookup("#" + order.getId());
            if (orderPane != null) {
                order.getDishes().forEach(dish -> {
                    Label ready = (Label) contentRoot.lookup("#dish" + dish.getId());
                    if (ready.getText().equals("X") && dish.getReady()) {
                        addNotification(dish.getName() + " from order " + order.getId() + " is ready.");
                    }
                    if (dish.getReady()) {
                        ready.setText("O");
                    } else {
                        ready.setText("X");
                    }

                });
                if (order.isReady()) {
                    addNotification("Order " + order.getId() + " is ready.");
                }
            } else {
                appendOrder(order);
                addNotification("New order created " + order.getId());
            }
        });
    }

    @FXML
    public void createNewOrder() {
        List<Dish> dishes = new ArrayList<>();
        newOrderMenu.getItems().forEach(menuItem -> dishes.add(new Dish(menuItem.getName())));

        if (sendOrder(new Order(dishes))) {
            newOrderMenu.getItems().clear();
        }
    }

    private void addNotification(String notification) {
        Text text = new Text(notification);
        HBox hBox = new HBox(text);

        hBox.setOnMouseClicked(this::removeNotification);
        hBox.setOnMouseEntered(event -> {
            TransitionResizeHeight transitionResizeHeight = new TransitionResizeHeight(Duration.millis(150), hBox, 46);
            transitionResizeHeight.play();
        });
        hBox.setOnMouseExited(event -> {
            TransitionResizeHeight transitionResizeHeight = new TransitionResizeHeight(Duration.millis(150), hBox, 38);
            transitionResizeHeight.play();
        });
        hBox.setMinHeight(0);

        notificationBlock.getChildren().add(0, hBox);

        if (!ordersPane.isDisabled()) {
            notificationIcon.setOpacity(1);
        }
        notificationInfo.setOpacity(0);
        notificationSound.play();
    }

    private void removeNotification(MouseEvent event) {
        HBox notification = (HBox) event.getSource();
        Text text = (Text) notification.getChildren().get(0);

        FadeTransition fade = new FadeTransition(Duration.millis(500), text);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.play();

        TranslateTransition translate = new TranslateTransition(Duration.millis(200), notification);
        translate.setFromY(0);
        translate.setToY(-5);
        translate.play();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), timelineEvent -> {
            notificationBlock.getChildren().remove(notification);
            if (notificationBlock.getChildren().size() == 0) {
                notificationInfo.setOpacity(1);
            }
        }));
        timeline.play();
    }


    private void loadOlderHistory(ChatValue chatValue, VBox chatBlock) {
        int displayedSessions = mainChatValue.getDisplayedSessions();
        int loadedSessions = mainChatValue.getSessions().size();
        int nextPage = loadedSessions / pageSize;

        HBox sessionInfo = (HBox) chatBlock.getChildren().get(0);
        Text info = (Text) sessionInfo.lookup("Text");

        ListOrderedMap<LocalDate, Session> sessionsMap = chatValue.getSessions();
        List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
        List<Session> nextSessions;
        if (loadedSessions > displayedSessions) {

            nextSessions = chatSessions.subList(displayedSessions,
                    Math.min(displayedSessions + pageSize, loadedSessions));

            if (displayedSessions + nextSessions.size() == loadedSessions && !mainChatValue.isMoreSessions()) {
                info.setText("Beginning of the chat");
            }
            mainChatValue.setDisplayedSessions(displayedSessions + nextSessions.size());

            nextSessions.forEach(session -> appendSession(session, mainChatBlock, mainChatValue, 1));

        } else if (mainChatValue.isMoreSessions()) {
            nextSessions = getNextSessions(mainChatValue.getChatId(), nextPage, pageSize);
            if (nextSessions.size() < pageSize) {
                mainChatValue.setMoreSessions(false);
                info.setText("Beginning of the chat");
            }
            mainChatValue.setDisplayedSessions(displayedSessions + nextSessions.size());
            nextSessions.forEach(session -> {

                if (!sessionsMap.containsKey(session.getDate())) {
                    sessionsMap.put(session.getDate(), session);

                    appendSession(session, mainChatBlock, mainChatValue, 1);
                }

            });
        } else {
            info.setText("Beginning of the chat");
        }
    }


    private void displayUserInfo() {
        firstNameLabel.setText(loggedUser.getFirstName());
        lastNameLabel.setText(loggedUser.getLastName());
        countryLabel.setText(loggedUser.getCountry());
        ageLabel.setText(String.valueOf(loggedUser.getAge()));
        roleLabel.setText(loggedUser.getRole());

        firstNameField.setText(loggedUser.getFirstName());
        lastNameField.setText(loggedUser.getLastName());
        countryField.setText(loggedUser.getCountry());
        ageField.setText(String.valueOf(loggedUser.getAge()));
        roleField.setText(loggedUser.getRole());

        if (loggedUser.getRole().equals("Chef")) {
            roleImage.setImage(new Image(getClass().getResourceAsStream("/images/chef-second.png")));
        } else {
            roleImage.setImage(new Image(getClass().getResourceAsStream("/images/waiter-second.png")));
        }
    }

    private void appendChats(List<Chat> chats) {
        chats.forEach(chat -> {
            try {
                InputStream in;
                ChatValue chatValue;
                Image profilePicture;
                if (chat.getFirstUser().getId() == loggedUser.getId()) {
                    in = new BufferedInputStream(
                            new URL(chat.getSecondUser().getProfilePicture()).openStream());

                    profilePicture = new Image(in);
                    chatValue = new ChatValue(chat.getId(), chat.getSecondUser().getId(), profilePicture);
                    chat.getSessions().forEach(session -> chatValue.getSessions().put(session.getDate(), session));
                } else {
                    in = new BufferedInputStream(
                            new URL(chat.getFirstUser().getProfilePicture()).openStream());
                    profilePicture = new Image(in);

                    chatValue = new ChatValue(chat.getId(), chat.getFirstUser().getId(), profilePicture);
                    chat.getSessions().forEach(session -> chatValue.getSessions().put(session.getDate(), session));
                }
                in.close();

                ImageView imageView = new ImageView(profilePicture);
                imageView.setId(String.valueOf(chat.getId()));
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setOnMouseClicked(this::setMainChat);

                chatUsers.getChildren().add(imageView);
                chatsMap.put(chat.getId(), chatValue);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setMainChat(MouseEvent event) {
        ImageView imageView = (ImageView) event.getSource();
        imageView.getStyleClass().set(0, "imagePressed");
          chatInfo.setOpacity(0);

        int chatId = Integer.parseInt(imageView.getId());

        ChatValue chat = chatsMap.get(chatId);
        HBox sessionInfo = (HBox) mainChatBlock.getChildren().get(0);
        Text info = (Text) sessionInfo.lookup("Text");
        if (mainChatValue != null) {
            ImageView currentImageView = (ImageView) chatUsersScroll.lookup("#" + mainChatValue.getChatId());
            currentImageView.getStyleClass().set(0, "imageReleased");

        }

        if (mainChatValue != null && chatId == mainChatValue.getChatId()) {
            if (mainChat.isDisabled()) {
                imageView.getStyleClass().set(0, "imagePressed");
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
    public void newMessage(){
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
        imageView.getStyleClass().add("shadow");
        newBlock.getStyleClass().add("chat-block");
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);
        HBox.setMargin(imageView, new Insets(-20, 0, 0, 0));

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
        if (messageBlocks.size() > 0 && messageBlocks.get(messageBlocks.size() - 1).getTypeSelector().equals("VBox")) {
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
                    hBox.getChildren().addAll(imageView, textFlow);
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
                    hBox.getChildren().addAll(textFlow, imageView);
                    newBlock.getChildren().add(hBox);
                    chatBlock.getChildren().add(newBlock);

                }
            }
        } else {

            if (message.getReceiverId() == loggedUser.getId()) {
                hBox.getStyleClass().add("second-user-message-first");
                hBox.getChildren().addAll(imageView, textFlow);
                newBlock.getChildren().add(hBox);
            } else {
                hBox.getStyleClass().add("user-message-first");
                hBox.getChildren().addAll(textFlow, imageView);
                newBlock.getChildren().add(hBox);
            }
            chatBlock.getChildren().add(newBlock);
        }
    }

    @FXML
    public void editUserInfo() {
        userInfoScroll.setContent(userInfoEditable);
    }

    @FXML
    public void saveUserInfo() {
        boolean edited = !firstNameLabel.getText().equals(firstNameField.getText()) || !lastNameLabel.getText().equals(lastNameField.getText()) ||
                !ageLabel.getText().equals(ageField.getText()) || !countryLabel.getText().equals(countryField.getText());
        userInfoScroll.setContent(userInfo);

        if (edited) {
            User user = sendUserInfo(firstNameField.getText(), lastNameField.getText(),
                    ageField.getText(), countryField.getText());

            if(user != null) {
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

    }

    @FXML
    private void showNotifications() {
        notificationIcon.setOpacity(0);
        ordersPane.setDisable(true);
        ordersPane.setOpacity(0);
    }

    @FXML
    private void showOrders() {
        ordersPane.setDisable(false);
        ordersPane.setOpacity(1);
    }

    @FXML
    private void scrollToChats() {
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(1000), new KeyValue(
                        menuScroll.vvalueProperty(), 1)));
        animation.play();
    }

    @FXML
    private void scrollToProfile() {
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(1000), new KeyValue(
                        menuScroll.vvalueProperty(), 0)));
        animation.play();
    }

    private void appendOrder(Order order) {
        Image clout = new Image(getClass().getResourceAsStream("/images/cloud-down.png"));
        ImageView imageView = new ImageView(clout);
        imageView.setFitWidth(15);
        imageView.setFitHeight(15);
        imageView.fitWidthProperty().setValue(15);
        imageView.fitHeightProperty().setValue(15);

        Button button = new Button("", imageView);
        button.setLayoutX(29);
        button.setLayoutY(48);
        button.setTranslateX(0);
        button.setTranslateY(0);
        button.setPrefWidth(28);
        button.setPrefHeight(28);
        button.setMinWidth(28);
        button.setMinHeight(28);
        button.addEventFilter(MouseEvent.MOUSE_CLICKED, expandOrderHandler);

        Label label = new Label(String.valueOf(order.getId()));
        label.setLayoutX(28);
        label.setLayoutY(11);
        label.setDisable(true);

        VBox dishesBox = new VBox();
        order.getDishes().forEach(dish -> {
            Label amount = new Label("3");
            amount.getStyleClass().add("amount");

            Label ready;
            if (dish.getReady()) {
                ready = new Label("O");
            } else {
                ready = new Label("X");
            }
            ready.setId("dish" + dish.getId());
            ready.getStyleClass().add("ready");

            TextField name = new TextField(dish.getName());
            name.getStyleClass().add("name");
            name.setDisable(true);

            HBox dishBox = new HBox(amount, name, ready);
            dishBox.getStyleClass().add("dish");

            amount.setViewOrder(1);
            name.setViewOrder(3);
            HBox.setHgrow(name, Priority.ALWAYS);
            dishesBox.getChildren().add(dishBox);
        });

        ScrollPane dishesScroll = new ScrollPane(dishesBox);
        dishesScroll.setMinHeight(0);
        dishesScroll.setMinWidth(0);
        dishesScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        AnchorPane dishesAnchor = new AnchorPane(dishesScroll);

        AnchorPane orderPane = new AnchorPane(dishesAnchor, button, label);
        orderPane.setId(String.valueOf(order.getId()));
        orderPane.setLayoutX(20.6);
        orderPane.setLayoutY(51.0);
        orderPane.getStyleClass().add("order");

        Pane orderContainer = new Pane(orderPane);
        orderContainer.getStyleClass().add("order-container");

        AnchorPane.setLeftAnchor(dishesScroll, 0.0);
        AnchorPane.setRightAnchor(dishesScroll, -14.4);
        AnchorPane.setBottomAnchor(dishesScroll, 0.0);
        AnchorPane.setTopAnchor(dishesScroll, 0.0);

        AnchorPane.setLeftAnchor(label, 28.0);
        AnchorPane.setRightAnchor(label, 28.0);
        AnchorPane.setLeftAnchor(dishesAnchor, 28.0);
        AnchorPane.setRightAnchor(dishesAnchor, 28.0);
        AnchorPane.setTopAnchor(dishesAnchor, 41.0);

        dishesAnchor.prefHeightProperty().bind(orderPane.prefHeightProperty().subtract(99));
        dishesBox.prefWidthProperty().bind(dishesScroll.widthProperty().subtract(15));

        Scrolls.fixBlurriness(dishesScroll);

        dishesScroll.skinProperty().addListener((observable, oldValue, newValue) -> {
            ScrollBar bar = Scrolls.findVerticalScrollBar(dishesScroll);
            Objects.requireNonNull(bar).addEventFilter(MouseEvent.MOUSE_DRAGGED, Event::consume);
            Objects.requireNonNull(bar).addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
        });

        button.prefWidthProperty().bind(((orderPane.prefWidthProperty()
                .subtract(81.6))
                .divide(15))
                .add(28));
        button.prefHeightProperty().bind(((orderPane.prefHeightProperty()
                .subtract(81.6))
                .divide(30))
                .add(28));

        dishesAnchor.setDisable(true);
        ordersFlow.getChildren().add(0, orderContainer);
    }

    private void ExpandOrderListeners() {
        ExpandOrderPane.contentRoot = contentRoot;
        ExpandOrderPane.contentPane = contentPane;
        ExpandOrderPane.scrollPane = ordersScroll;
        ExpandOrderPane.buttonExpandedProperty().addListener((observable, oldValue, newValue) -> {
            Button currentButton = ExpandOrderPane.button;
            if (newValue) {
                currentButton.removeEventFilter(MouseEvent.MOUSE_CLICKED, expandOrderHandler);
                currentButton.addEventFilter(MouseEvent.MOUSE_CLICKED, reverseOrderHandler);
            } else {
                currentButton.removeEventFilter(MouseEvent.MOUSE_CLICKED, reverseOrderHandler);
                currentButton.addEventFilter(MouseEvent.MOUSE_CLICKED, expandOrderHandler);
            }
        });
    }

    private EventHandler<MouseEvent> expandOrderHandler = this::expandOrder;
    private EventHandler<MouseEvent> reverseOrderHandler = e -> ExpandOrderPane.reverseOrder();

    @FXML
    public void expandOrder(MouseEvent event) {
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        if (!ExpandOrderPane.action && (intersectedNode.getTypeSelector().equals("Button")
                || (!intersectedNode.getTypeSelector().equals("ScrollPaneSkin$6") && intersectedNode.getStyleClass().get(0).equals("order")))) {

            ExpandOrderPane.setCurrentOrder(event);

        }
    }
}