package sample;

import Animations.MoveRoot;
import Animations.ResizeRoot;
import Animations.TransitionResizeHeight;
import Animations.TransitionResizeWidth;
import Application.RestaurantApplication;
import Application.StageManager;
import Helpers.ListViews.ChatsListViewCell;
import Helpers.ListViews.DishListViewCell;
import Helpers.ListViews.MenuListViewCell;
import Helpers.Scrolls;
import Helpers.Services.MessageService;
import Helpers.Services.OrderService;
import Models.*;
import Models.Menu;
import javafx.animation.*;
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
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static Helpers.ServerRequests.*;
import static Helpers.Services.OrderService.mostRecentOrderDate;

public class ControllerLoggedSecondStyle implements Controller {
    @FXML Label dishesCountLabel, orderIdLabel, updatedDateLabel, updatedTimeLabel,
            createdDateLabel, createdTimeLabel, roleField, usernameField, firstNameLabel,
            lastNameLabel, countryLabel, ageLabel, roleLabel, usernameLabel;

    @FXML AnchorPane menuRoot,menu, menuButtons, menuButtonsContainer, contentRoot, profileView,
            notificationsView, menuContent, orderInfo, userInfoLabels, userInfoFields, orderView,
            chatView, userChatsClip, createView, dishesContainer, chatContainer;

    @FXML TextField firstNameField, lastNameField, countryField, ageField, menuSearch;
    @FXML Button menuButton, editButton;
    @FXML HBox notificationsInfo;
    @FXML Pane profileImageContainer, profileImageClip, contentBar;
    @FXML ListView<String> ordersList, notificationsList;
    @FXML ListView<Chat> userChats;
    @FXML ListView<Menu> menuList, newOrderList;
    @FXML ListView<Dish> dishesList;
    @FXML ImageView profileImage;
    @FXML TextArea chatTextArea;
    @FXML ScrollPane chatScroll;
    @FXML VBox chatBlock;

    private static Image userProfileImage;
    private AnchorPane currentView, currentMenuView;

    private MediaPlayer notificationSound;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");

    private DateTimeFormatter dateFormatterSimple = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private TreeMap<String, Menu> menuMap = new TreeMap<>();
    private Map<Integer, ChatValue> chatsMap = new HashMap<>();

    private User loggedUser;
    private static MessageService messageService;
    private static OrderService orderService;

    private ChatValue chatValue;

    @FXML
    public void initialize() {
        menuList.setCellFactory(menuCell -> new MenuListViewCell());
        newOrderList.setCellFactory(menuCell -> new MenuListViewCell());
        userChats.setCellFactory(chatCell -> new ChatsListViewCell());
        dishesList.setCellFactory(dishCell -> new DishListViewCell());

        waitForNewOrders();
        waitForNewMessages();

        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Menu> observableList = FXCollections.observableArrayList();
            searchMenu(newValue.toLowerCase()).forEach((s, menu) -> observableList.add(menu));
            menuList.setItems(observableList);
        });

        chatBlock.idProperty().addListener((observable1, oldValue1, newValue1) -> {
            if ((newValue1.equals("append") || newValue1.equals("beginning-append")) && chatValue != null) {
                loadOlderHistory(chatValue, chatBlock);
            }
        });

        chatTextArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                addNewMessage();
                event.consume();
            }
        });

        Scrolls scrolls = new Scrolls(chatScroll, chatTextArea);
        scrolls.manageScrollsSecondStyle();

        MoveRoot.move(menuButton, menuRoot);
        MoveRoot.move(contentBar, contentRoot);
        ResizeRoot.addListeners(contentRoot);

        menuContent.getChildren().remove(profileView);

        Media sound = new Media(getClass()
                .getResource("/notification.mp3")
                .toExternalForm());
        notificationSound = new MediaPlayer(sound);
        notificationSound.setOnEndOfMedia(() -> notificationSound.stop());

        Circle clip = new Circle(30.8, 30.8, 30.8);
        profileImageClip.setClip(clip);

        Rectangle chatsClip = new Rectangle(211, 421);
        userChatsClip.setClip(chatsClip);

        Rectangle notificationClip = new Rectangle();
        notificationClip.setArcHeight(33);
        notificationClip.setArcWidth(33);
        notificationClip.heightProperty().bind(notificationsList.heightProperty());
        notificationClip.widthProperty().bind(notificationsList.widthProperty());

        notificationsList.setClip(notificationClip);

        chatBlock.prefWidthProperty().bind(chatScroll.widthProperty().subtract(25));

    }
    private SortedMap<String, Menu> searchMenu(String prefix) {
        return menuMap.subMap(prefix, prefix + Character.MAX_VALUE);
    }

    @FXML
    public void showChatView(){
        displayView(chatView);
    }
    @FXML
    public void showOrderView(){
        displayView(orderView);
    }
    @FXML
    public void showCreateView(){
        displayView(createView);
    }

    private void displayView(AnchorPane requestedView){
        if(requestedView.equals(currentView)){
            contentRoot.setOpacity(0);
            contentRoot.setDisable(true);
            requestedView.setOpacity(0);
            requestedView.setDisable(true);

            currentView = null;
        }else if(currentView == null) {
            requestedView.setDisable(false);
            requestedView.setOpacity(1);

            contentRoot.setOpacity(1);
            contentRoot.setDisable(false);
            currentView = requestedView;
        }else{
            requestedView.setDisable(false);
            requestedView.setOpacity(1);

            currentView.setDisable(true);
            currentView.setOpacity(0);
            currentView = requestedView;
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
                    RestaurantApplication.showAlert(e.getMessage());
                }
            } else {
                RestaurantApplication.showAlert("Order must have at least one dish.");
            }
        } else {
            RestaurantApplication.showAlert("You must be a server to create orders.");
        }
    }
    private void waitForNewMessages(){
        messageService = new MessageService();
        messageService.setOnSucceeded(event -> {
            MessageService.lastMessageCheck = LocalDateTime.now();
            List<Message> newMessages = (List<Message>) messageService.getValue();
            newMessages.forEach(message -> {
                int index = chatBlock.getChildren().size();
                chatBlock.setId("new-message");

                ChatValue chat = chatsMap.get(message.getChatId());
                ListOrderedMap<LocalDate, Session> sessions = chat.getSessions();

                Session session = sessions.get(LocalDate.now());
                if (session == null) {
                    LocalDate sessionDate = LocalDate.now();

                    session = new Session();
                    session.setDate(sessionDate);
                    sessions.put(0, sessionDate, session);
                    session.getMessages().add(message);

                    if (chatValue != null && chatValue.getChatId() == message.getChatId()) {
                        chatValue.setDisplayedSessions(chatValue.getDisplayedSessions() + 1);
                        appendSession(session, chatBlock, chatValue, index);
                    }
                } else {
                    session.getMessages().add(message);
                    if (chatValue != null && chatValue.getChatId() == message.getChatId()) {
                        appendMessage(message, chatValue, (VBox) chatBlock.getChildren().get(index - 1));
                    }
                }
                updateLastMessage(message.getChatId(), message.getMessage());
            });
            messageService.restart();
        });

        messageService.setOnFailed(event -> serviceFailed(messageService));
    }

    private void waitForNewOrders() {
        orderService = new OrderService();
        orderService.setOnSucceeded(event -> {
            List<Order> newOrders = (List<Order>) orderService.getValue();

            updateNewOrders(newOrders);
            orderService.restart();
        });

        orderService.setOnFailed(event -> serviceFailed(orderService));
    }


    private void updateNewOrders(List<Order> newOrders) {
        newOrders.forEach(order -> {

            if(order.getUpdated().isAfter(mostRecentOrderDate)) {
                mostRecentOrderDate = order.getUpdated();
            }else if(order.getCreated().isAfter(mostRecentOrderDate)){
                mostRecentOrderDate = order.getCreated();
            }

            int orderId = order.getId();
            Order orderValue = loggedUser.getOrders().get(orderId);

            if (orderValue != null) {
                order.getDishes().forEach(dish -> {

                    if(orderIdLabel.getText().equals(String.valueOf(orderId))) {
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
                ordersList.getItems().add(0, "Order " + orderId);
                if(order.getUserId() != loggedUser.getId()){
                    addNotification("New order created " + orderId);
                }
            }
            loggedUser.getOrders().put(orderId, order);
        });
    }

    private void addNotification(String notification) {
        notificationsInfo.setOpacity(0);
        notificationsInfo.setDisable(true);

        notificationsList.getItems().add(0, notification);
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
    public void addMenuItem(){
        Menu menuItem = menuList.getSelectionModel().getSelectedItem();
        newOrderList.getItems().add(0, menuItem);
    }
    @FXML
    public void removeMenuItem(){
        Menu menuItem = newOrderList.getSelectionModel().getSelectedItem();
        newOrderList.getItems().remove(menuItem);
    }
    private void serviceFailed(Service service){

        if(service.getException() != null) {
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
        if(!LoginSecondStyle.alert.isShowing()) {
            DialogPane dialog = LoginSecondStyle.alert.getDialogPane();
            dialog.setContentText(message);
            LoginSecondStyle.alert.showAndWait();
        }
    }
    @FXML public void expandMenu(){
        if(menuButtonsContainer.getChildren().size() == 1){
            menuButtonsContainer.getChildren().add(0, menuButtons);
        }
        TransitionResizeWidth expand = new TransitionResizeWidth(Duration.millis(700), menu, 518);
        expand.play();
    }
    @FXML
    public void reverseMenu(){
        if(currentMenuView == null) {
            TransitionResizeWidth reverse = new TransitionResizeWidth(Duration.millis(700), menu, 38.5);
            reverse.play();
            menuButtonsContainer.getChildren().remove(menuButtons);
        }
    }
    private void expandMenuContent(){
        TransitionResizeHeight expand = new TransitionResizeHeight(Duration.millis(800), menuContent, menuContent.getMaxHeight());
        expand.play();
    }
    private void reverseMenuContent(){
        TransitionResizeHeight reverse = new TransitionResizeHeight(Duration.millis(800), menuContent, 0);
        reverse.play();
    }
    public void setStage() throws Exception{
        loggedUser = loggedUserProperty.getValue();
        loggedUser.getRestaurant().getMenu().forEach(menu -> menuMap.put(menu.getName().toLowerCase(), menu));

        mostRecentOrderDate = getMostRecentOrderDate(loggedUser.getRestaurant().getId());

        orderService.start();
        messageService.start();

        menuList.setItems(FXCollections.observableArrayList(loggedUser.getRestaurant().getMenu()));

        ObservableList<Chat> chats = FXCollections.observableArrayList(getChats());
        setChatValues(chats);
        userChats.setItems(chats);

        ObservableList<String> orders = FXCollections.observableArrayList(loggedUser.getOrders().values()
                .stream()
                .map(order -> "Order " + order.getId())
                .collect(Collectors.toList()));

        FXCollections.reverse(orders);
        ordersList.setItems(orders);

        try{
            InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
            userProfileImage = new Image(in);
            in.close();
        }catch(Exception e){
            userProfileImage = new Image(getClass().getResourceAsStream("/images/default-picture.png"));
        }

        displayUserFields();
    }

    public void resetStage(){
        mostRecentOrderDate = null;
        userProfileImage = null;
        loggedUser = null;
        chatValue = null;
        chatTextArea.setText(null);
        menuSearch.setText("");

        chatBlock.getChildren().remove(1,chatBlock.getChildren().size());

        chatContainer.setDisable(true);
        chatContainer.setOpacity(0);

        menuMap.clear();

        newOrderList.getItems().clear();
        menuList.getItems().clear();
        userChats.getItems().clear();
        ordersList.getItems().clear();
        notificationsList.getItems().clear();

        resetUserFields();

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        contentRoot.setPrefWidth(contentRoot.getMinWidth());
        contentRoot.setPrefHeight(contentRoot.getMinHeight());

        contentRoot.setLayoutY((primaryScreenBounds.getHeight() - contentRoot.getPrefHeight()) / 2);
        contentRoot.setLayoutX((primaryScreenBounds.getWidth() - contentRoot.getPrefWidth()) / 2);

        menuRoot.setLayoutX((primaryScreenBounds.getWidth() - menuRoot.getWidth()) / 2);
        menuRoot.setLayoutY(contentRoot.getLayoutY() - 60);

        menuContent.setDisable(true);
        menuContent.getChildren().remove(profileView);
        notificationsView.setOpacity(0);
        notificationsView.setDisable(true);

        contentRoot.setOpacity(0);
        contentRoot.setDisable(true);

        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);
        }

        currentMenuView = null;
        currentView = null;

        expandMenu();
        reverseMenuContent();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), profileImageContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.play();

        orderService.cancel();
        messageService.cancel();
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
    public void logOut(){
        RestaurantApplication.logout();
    }

    @FXML
    public void showLoggedFirstStyle() {
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        StageManager.changeStage(StageManager.firstLoggedStage);
    }

    @FXML
    public void showLoggedThirdStyle(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        StageManager.changeStage(StageManager.thirdLoggedStage);
    }
    @FXML
    public void showProfile(){
        if(menuContent.isDisabled()) {
            expandMenuContent();
            currentMenuView = profileView;

            menuContent.getChildren().add(profileView);
            menuContent.setDisable(false);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), profileImageContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(300));
            fadeIn.play();

        }else if(!currentMenuView.equals(profileView) && menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            currentMenuView.setOpacity(0);
            currentMenuView.setDisable(true);
            currentMenuView = profileView;

            profileImageContainer.setOpacity(1);
            menuContent.getChildren().add(profileView);

        }else if(menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            reverseMenuContent();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(600), profileImageContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
            Timeline removeView = new Timeline(new KeyFrame(Duration.millis(800), event -> {
                menuContent.setDisable(true);
                menuContent.getChildren().remove(profileView);
                currentMenuView = null;
                reverseMenu();
            }));
            removeView.play();
        }
    }
    @FXML
    public void showNotification(){
        if(menuContent.isDisabled()) {
            expandMenuContent();

            currentMenuView = notificationsView;
            currentMenuView.setOpacity(1);
            currentMenuView.setDisable(false);
            menuContent.setDisable(false);
        }else if(!currentMenuView.equals(notificationsView) && menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            menuContent.getChildren().remove(currentMenuView);
            profileImageContainer.setOpacity(0);

            currentMenuView = notificationsView;
            currentMenuView.setDisable(false);
            currentMenuView.setOpacity(1);
        }else if(menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            reverseMenuContent();

            Timeline removeView = new Timeline(new KeyFrame(Duration.millis(800), event -> {
                menuContent.setDisable(true);
                currentMenuView.setDisable(true);
                currentMenuView.setOpacity(0);
                currentMenuView = null;
                reverseMenu();
            }));
            removeView.play();
        }

    }
    @FXML
    public void profileButtonHoverOver(MouseEvent event){
        AnchorPane shadowContainer = (AnchorPane) ((Button)event.getSource()).getParent();
        shadowContainer.getStyleClass().add("profile-button-hovered");
    }
    @FXML
    public void profileButtonHoverOut(MouseEvent event){
        AnchorPane shadowContainer = (AnchorPane) ((Button)event.getSource()).getParent();
        shadowContainer.getStyleClass().remove("profile-button-hovered");
    }
    @FXML
    public void focus(MouseEvent event){
        Button button = (Button) event.getSource();
        AnchorPane.setTopAnchor(button, -5.5);
        AnchorPane.setBottomAnchor(button, -4.0);
    }
    @FXML
    public void unFocus(MouseEvent event){
        Button button = (Button) event.getSource();
        AnchorPane.setTopAnchor(button, -1.0);
        AnchorPane.setBottomAnchor(button, 0.0);
    }

    @FXML
    private void showOrder(){
        String selectedItem = ordersList.getSelectionModel().getSelectedItem();
        int orderId = Integer.valueOf(selectedItem.substring(6));
        Order order = loggedUser.getOrders().get(orderId);

        if(!currentView.equals(orderView)){
            currentView.setOpacity(0);
            currentView.setDisable(true);

            orderView.setOpacity(1);
            orderView.setDisable(false);
            currentView = orderView;
        }
        orderIdLabel.setText(String.valueOf(order.getId()));
        dishesCountLabel.setText("Dishes " + order.getDishes().size());

        createdDateLabel.setText(dateFormatterSimple.format(order.getCreated()));
        createdTimeLabel.setText(timeFormatter.format(order.getCreated()));
        updatedDateLabel.setText(dateFormatterSimple.format(order.getUpdated()));
        updatedTimeLabel.setText(timeFormatter.format(order.getUpdated()));


        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), orderInfo);
        fadeIn.setFromValue(0.36);
        fadeIn.setToValue(1);
        fadeIn.play();

        order.getDishes().forEach(dish -> dish.setOrderId(order.getId()));
        dishesList.setItems(FXCollections.observableArrayList(order.getDishes()));
    }

    public void updateDishStatus(Dish dish, Label ready) {

        if(loggedUser.getRole().equals("Chef")){
            //todo: if jwt expired
            try {
                updateDishState(dish.getOrderId(), dish.getId());
                dish.setReady(true);
                ready.setText("O");

            } catch (Exception e) {
                RestaurantApplication.showAlert(e.getMessage());
            }
        }else{
            RestaurantApplication.showAlert("You must be a chef to update the dish status.");
        }
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

    @FXML
    private void setChat() {
        Chat selectedChat = userChats.getSelectionModel().getSelectedItem();

        if(selectedChat != null) {
            chatContainer.setOpacity(1);
            chatContainer.setDisable(false);

            int chatId = selectedChat.getId();
            ChatValue chat = chatsMap.get(chatId);

            HBox sessionInfo = (HBox) chatBlock.getChildren().get(0);
            Text info = (Text) sessionInfo.lookup("Text");

            if (chatValue == null || chatId != chatValue.getChatId()) {
                chatBlock.setId("beginning");
                chatBlock.getChildren().remove(1, chatBlock.getChildren().size());

                chatValue = chat;

                ListOrderedMap<LocalDate, Session> sessionsMap = chatValue.getSessions();
                List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
                List<Session> lastSessions = chatSessions.subList(0, Math.min(pageSize, chatSessions.size()));

                if (lastSessions.size() == pageSize) {
                    info.setText("Scroll for more history");
                    chatValue.setDisplayedSessions(pageSize);
                } else {
                    info.setText("Beginning of the chat");
                    chatValue.setMoreSessions(false);
                    chatValue.setDisplayedSessions(lastSessions.size());
                }

                lastSessions.forEach(session -> appendSession(session, chatBlock, chatValue, 1));

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
    public void addNewMessage(){
        String messageText = chatTextArea.getText();
        int chatId = chatValue.getChatId();
        int receiverId = chatValue.getUserId();
        int index = chatBlock.getChildren().size();
        chatTextArea.clear();

        if (messageText.length() > 0){
            Message message = sendMessage(messageText, chatId, receiverId);
            updateLastMessage(chatId, messageText);

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
    private void updateLastMessage(int chatId, String message) {
        Label lastMessage = (Label) contentRoot.lookup("#lastMessage" + chatId);
        lastMessage.setText(message);
    }
}
