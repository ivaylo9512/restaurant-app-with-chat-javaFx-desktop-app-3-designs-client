package sample;

import Animations.*;
import Helpers.ListViews.OrderListViewCell;
import Helpers.Services.MessageService;
import Helpers.Services.OrderService;
import Helpers.Scrolls;
import Helpers.ListViews.MenuListViewCell;
import Models.*;
import Models.Menu;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static Helpers.ServerRequests.*;
import static Helpers.Services.OrderService.mostRecentOrderDate;


public class ControllerLoggedFirstStyle {
    @FXML ScrollPane menuScroll, userInfoScroll, chatUsersScroll, mainChatScroll, notificationsScroll;
    @FXML VBox mainChatBlock, chatUsers, notificationBlock;
    @FXML FlowPane notificationInfo, chatInfo, userInfo, userInfoEditable;
    @FXML Label firstNameLabel, lastNameLabel, countryLabel, ageLabel, roleLabel, roleField;
    @FXML TextField firstNameField, lastNameField, countryField, ageField;
    @FXML AnchorPane contentRoot, contentPane, mainChat, ordersPane, profileImageContainer;
    @FXML Pane moveBar, notificationIcon, profileRoot;
    @FXML TextArea mainChatTextArea;
    @FXML ImageView roleImage, profileImage;
    @FXML TextField menuSearch;
    @FXML ListView<Menu> menu, newOrderMenu;
    @FXML ListView<Order> ordersList;

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    private DateTimeFormatter dateFormatterSimple = DateTimeFormatter.ofPattern("MM/dd/yyyy");


    private TreeMap<String, Menu> menuMap = new TreeMap<>();
    private Map<Integer, ChatValue> chatsMap = new HashMap<>();

    private ScrollBar ordersScrollBar;
    private Image userProfileImage;
    private ChatValue mainChatValue;
    private MediaPlayer notificationSound;

    private static MessageService messageService;
    private static OrderService orderService;
    private User loggedUser;

    @FXML
    public void initialize() {
        newOrderMenu.setCellFactory(menuCell -> new MenuListViewCell());
        menu.setCellFactory(menuCell -> new MenuListViewCell());
        ordersList.setCellFactory(orderCell -> new OrderListViewCell());

        waitForNewOrders();
        waitForNewMessages();

        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Menu> observableList = FXCollections.observableArrayList();
            searchMenu(newValue.toLowerCase()).forEach((s, menu) -> observableList.add(menu));
            menu.setItems(observableList);
        });

        Scrolls scrolls = new Scrolls(menuScroll, userInfoScroll, chatUsersScroll,
                mainChatScroll, notificationsScroll, mainChatTextArea);
        scrolls.manageScrollsFirstStyle();

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
        Media sound = new Media(getClass()
                .getResource("/notification.mp3")
                .toExternalForm());
        notificationSound = new MediaPlayer(sound);
        notificationSound.setOnEndOfMedia(() -> notificationSound.stop());

        notificationBlock.prefWidthProperty().bind(notificationsScroll.widthProperty().subtract(20));

        contentRoot.getChildren().remove(userInfoEditable);
        ResizeMainChat.addListeners(mainChat);

        Circle clip = new Circle(0, 0, 30);
        clip.setLayoutX(30);
        clip.setLayoutY(30);
        profileImageContainer.setClip(clip);

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

    private void showLoginStageAlert(String message) {
        if(!LoginFirstStyle.alert.isShowing()) {
            DialogPane dialog = LoginFirstStyle.alert.getDialogPane();
            dialog.setContentText(message);
            LoginFirstStyle.alert.showAndWait();
        }
    }
    private void showLoggedStageAlert(String message) {
        if(!LoggedFirstStyle.alert.isShowing()) {
            DialogPane dialog = LoggedFirstStyle.alert.getDialogPane();
            dialog.setContentText(message);
            LoggedFirstStyle.alert.showAndWait();
        }
    }

    private SortedMap<String, Menu> searchMenu(String prefix) {
        return menuMap.subMap(prefix, prefix + Character.MAX_VALUE);
    }

    private void updateNewOrders(List<Order> newOrders) {
        newOrders.forEach(order -> {
            int orderId = order.getId();

            Order orderValue = loggedUser.getOrders().get(orderId);
            if (orderValue != null) {
                orderValue.setDishes(order.getDishes());
                orderValue.setUpdated(order.getUpdated());

                order.getDishes().forEach(dish -> {
                    Label ready = (Label) contentRoot.lookup("#dish" + dish.getId());

                    if (ready != null && ready.getText().equals("X") && dish.getReady()) {
                        addNotification(dish.getName() + " from order " + orderId + " is ready.");
                        ready.setText("O");
                    }
                });

                AnchorPane updateContainer = (AnchorPane) contentRoot.lookup("#update" + order.getId());
                if(updateContainer != null){
                    Label updatedTime = (Label) updateContainer.getChildren().get(0);
                    Label updatedDate = (Label) updateContainer.getChildren().get(1);

                    updatedTime.setText(timeFormatter.format(order.getUpdated()));
                    updatedDate.setText(dateFormatterSimple.format(order.getUpdated()));
                }
                if (order.isReady()) {
                    addNotification("Order " + orderId + " is ready.");
                }

            } else {
                if(ExpandOrderPane.buttonExpandedProperty().getValue()){
                    ExpandOrderPane.reverseOrder();
                }
                loggedUser.getOrders().put(order.getId(), order);
                ordersList.getItems().add(0, order);

                if(order.getUserId() != loggedUser.getId()){
                    addNotification("New order created " + orderId);
                }
            }
        });
    }

    @FXML
    public void createNewOrder() {
        List<Dish> dishes = new ArrayList<>();
        newOrderMenu.getItems().forEach(menuItem -> dishes.add(new Dish(menuItem.getName())));

        if (loggedUser.getRole().equals("Server")) {
            if(dishes.size() > 0) {
                try{
                    sendOrder(new Order(dishes));
                    newOrderMenu.getItems().clear();
                }catch (Exception e){
                    showLoggedStageAlert(e.getMessage());
                }
            }else{
                showLoggedStageAlert("Order must have at least one dish.");
            }
        } else {
            showLoggedStageAlert("You must be a server to create orders.");
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

            } catch (IOException e) {
                e.printStackTrace();
            }
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
    public void addMenuItem(){
        Menu menuItem = menu.getSelectionModel().getSelectedItem();
        newOrderMenu.getItems().add(0, menuItem);
    }
    @FXML
    public void removeMenuItem(){
        Menu menuItem = newOrderMenu.getSelectionModel().getSelectedItem();
        newOrderMenu.getItems().remove(menuItem);
    }

    private void displayUserFields() {
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

        profileImage.setImage(userProfileImage);

        if (loggedUser.getRole().equals("Chef")) {
            roleImage.setImage(new Image(getClass().getResourceAsStream("/images/chef-second.png")));
        } else {
            roleImage.setImage(new Image(getClass().getResourceAsStream("/images/waiter-second.png")));
        }
    }
    private void resetUserFields() {
        firstNameLabel.setText(null);
        lastNameLabel.setText(null);
        countryLabel.setText(null);
        ageLabel.setText(null);
        roleLabel.setText(null);

        firstNameField.setText(null);
        lastNameField.setText(null);
        countryField.setText(null);
        ageField.setText(null);
        roleField.setText(null);

        profileImage.setImage(null);
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

    public void displayUserInfo() throws Exception{
        loggedUser = loggedUserProperty.getValue();
        loggedUser.getRestaurant().getMenu().forEach(menu -> menuMap.put(menu.getName().toLowerCase(), menu));

        ObservableList<Order> orders = FXCollections.observableArrayList(loggedUser.getOrders().values());
        FXCollections.reverse(orders);
        ordersList.setItems(orders);

        List<Chat> chats = getChats();
        appendChats(chats);

        mostRecentOrderDate = getMostRecentOrderDate(loggedUser.getRestaurant().getId());

        orderService.start();
        messageService.start();

        InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
        userProfileImage = new Image(in);
        in.close();

        displayUserFields();
        menu.setItems(FXCollections.observableArrayList(loggedUser.getRestaurant().getMenu()));
    }
    public void resetStage(){
        notificationBlock.getChildren().clear();
        chatUsers.getChildren().clear();
        newOrderMenu.getItems().clear();
        menu.getItems().clear();

        mainChatBlock.getChildren().remove(1,mainChatBlock.getChildren().size());

        mainChat.setDisable(true);
        mainChat.setOpacity(0);
        mainChat.setLayoutX(217);
        mainChat.setLayoutY(231);
        mainChat.setPrefHeight(189);

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        contentRoot.setLayoutY((primaryScreenBounds.getHeight() - contentRoot.getHeight()) / 2);
        contentRoot.setLayoutX((primaryScreenBounds.getWidth() - contentRoot.getWidth()) / 2);
        contentRoot.setPrefHeight(428);
        contentRoot.setPrefWidth(743);

        menuSearch.setText("");
        mainChatTextArea.setText(null);
        profileImage.setImage(null);
        mostRecentOrderDate = null;
        userProfileImage = null;
        mainChatValue = null;
        loggedUser = null;

        userInfoScroll.setVvalue(0);
        menuScroll.setVvalue(0);
        chatUsersScroll.setVvalue(0);

        userInfoScroll.setDisable(false);
        ordersList.setDisable(false);

        profileRoot.setOpacity(0);
        profileRoot.setDisable(true);

        notificationIcon.setOpacity(0);
        ordersPane.setDisable(false);
        ordersPane.setOpacity(1);

        ResizeRoot.resize = true;
        userInfoScroll.setContent(userInfo);
        resetUserFields();

        if(ExpandOrderPane.buttonExpandedProperty().getValue()){
            ExpandOrderPane.reverseOrder();
        }
        chatsMap.clear();
        menuMap.clear();

        Platform.runLater(() -> {
            orderService.reset();
            messageService.reset();
        });
    }
    @FXML
    public void logOut(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        LoggedFirstStyle.stage.close();

        if(LoginFirstStyle.stage != null){
            LoginFirstStyle.stage.show();
        }else{
            try {
                LoginSecondStyle.displayLoginScene();
            } catch (IOException e) {
                LoginFirstStyle.stage.show();

                DialogPane dialog = LoginFirstStyle.alert.getDialogPane();
                dialog.setContentText(e.getMessage());
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

        LoggedFirstStyle.stage.close();
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
    @FXML
    public void showLoggedThirdStyle(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        LoggedFirstStyle.stage.close();
        if(LoggedThirdStyle.stage != null){
            LoggedThirdStyle.stage.show();
        }else {
            try {
                LoggedThirdStyle.displayLoggedScene();
            } catch (Exception e) {
                LoginFirstStyle.stage.show();
                DialogPane dialogPane = LoginFirstStyle.alert.getDialogPane();
                dialogPane.setContentText(e.getMessage());
                LoginFirstStyle.alert.showAndWait();

            }
        }
    }
    @FXML
    public void minimize(){
        LoggedFirstStyle.stage.setIconified(true);
    }

    @FXML
    public void close(){
        LoggedFirstStyle.stage.close();
    }

    public void updateDishStatus(int orderId, int dishId) {
        if(loggedUser.getRole().equals("Chef")){
                try {
                    updateDishState(orderId, dishId);
                    Label ready = (Label) contentRoot.lookup("#dish" + dishId);
                    ready.setText("O");

                } catch (Exception e) {
                    showLoggedStageAlert(e.getMessage());
                }
        }else{
            showLoggedStageAlert("You must be a chef to update the dish status.");
        }
    }

    private void ExpandOrderListeners() {
        ExpandOrderPane.contentRoot = contentRoot;
        ExpandOrderPane.contentPane = contentPane;
        ExpandOrderPane.scrollBar = ordersScrollBar;
        ExpandOrderPane.orderList = ordersList;
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
        if (!ExpandOrderPane.action && (intersectedNode instanceof Button
                || (!intersectedNode.getTypeSelector().equals("ScrollPaneSkin$6") && intersectedNode.getStyleClass().get(0).equals("order")))) {

            ExpandOrderPane.setCurrentOrder(event);

        }
    }
}