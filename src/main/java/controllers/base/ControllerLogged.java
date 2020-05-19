package controllers.base;

import helpers.listviews.DishListViewCell;
import helpers.listviews.MenuListViewCell;
import javafx.beans.property.ObjectProperty;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import models.*;
import models.Menu;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import static application.RestaurantApplication.*;
import static application.ServerRequests.pageSize;

public class ControllerLogged {
    @FXML
    public AnchorPane contentRoot;
    @FXML
    protected VBox mainChatBlock;
    @FXML
    ImageView profileImage;
    @FXML
    HBox notificationsInfo;

    @FXML
    protected Label orderId, updatedDate, createdDate, createdTime, updatedTime;
    @FXML
    protected TextField usernameField, firstNameField, lastNameField, countryField, ageField, menuSearch, roleField;
    @FXML
    protected Button saveButton, editButton, createButton;
    @FXML
    protected Pane userInfo;
    @FXML
    protected AnchorPane mainChat, orderPane, notificationsView;
    @FXML
    protected ListView<Order> ordersList;
    @FXML
    protected ListView<Menu> menuList, newOrderList;
    @FXML
    protected ListView<Notification> notificationsList;
    @FXML
    protected ListView<Dish> currentDishList;
    @FXML
    protected ListView<ChatValue> chatUsersList;
    @FXML
    protected Node notificationIcon;
    @FXML
    protected ScrollPane mainChatScroll;
    @FXML
    protected TextArea mainChatTextArea;
    @FXML
    protected Text mainChatInfo;

    protected static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    protected static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    protected static DateTimeFormatter dateFormatterSimple = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    protected static Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    protected static BooleanProperty isNewNotificationChecked = new SimpleBooleanProperty(true);
    protected ObservableList<Menu> userMenu = FXCollections.observableArrayList();

    protected ProgressIndicator editIndicator = new ProgressIndicator();
    protected ProgressIndicator createIndicator = new ProgressIndicator();

    private Node editButtonGraphic, createButtonGraphic;
    private String createButtonText;
    public Order currentOrder;

    protected ObjectProperty<ChatValue> mainChatValue = chatManager.mainChatValue;
    protected ObjectProperty<ChatValue> secondChatValue = chatManager.secondChatValue;

    protected void setNotificationsListeners() {
        notificationsList.getItems().addListener((ListChangeListener<Notification>)c -> {
            c.next();
            if(c.getRemovedSize() > 0) {
                removeNotification();
            }else{
                addNotification();
            }
        });

        isNewNotificationChecked.addListener(c -> {
            if(!isNewNotificationChecked.get() && notificationsView.isDisabled()){
                notificationIcon.setOpacity(1);
                return;
            }
            isNewNotificationChecked.setValue(true);
            notificationIcon.setOpacity(0);
        });
    }

    protected void bindChat(AnchorPane chat, ObjectProperty<ChatValue> chatValue,
                            VBox chatBlock, Text textInfo, TextArea chatTextArea){
        chat.disableProperty().bind(chatValue.isNull());
        chat.opacityProperty().bind(Bindings.createDoubleBinding(()-> {
            if(chatValue.isNull().get()) return 0.0;

            return 1.0;
        }, chatValue));

        chatValue.addListener(observable -> {
            if(chatValue.get() != null) setChat(chatValue.get(), chatBlock, textInfo, chatTextArea);
        });
    }

    protected void setUserGraphicIndicator(){
        editButtonGraphic = editButton.getGraphic();
        loginManager.sendInfo.runningProperty().addListener((observable, oldValue, newValue)->{
            if(newValue){
                editButton.setGraphic(editIndicator);
                editButton.setText(null);
            }else {
                editButton.setGraphic(editButtonGraphic);
                editButton.setText("Edit");
            }
        });
    }
    protected void setCreateGraphicIndicators() {
        createButtonGraphic = createButton.getGraphic();
        createButtonText = createButton.getText();

        orderManager.sendOrder.runningProperty().addListener((observable, oldValue, newValue)->{
            if(newValue){
                createButton.setGraphic(createIndicator);
                createButton.setText(null);
            }else {
                createButton.setGraphic(createButtonGraphic);
                createButton.setText(createButtonText);
            }
        });
    }

    protected void setNotificationsFactories(){
        notificationsList.setCellFactory(param -> new ListCell<Notification>(){
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }
    protected void setListsFactories() {
        currentDishList.setCellFactory(c -> new DishListViewCell());
        menuList.setCellFactory(menuCell -> new MenuListViewCell());
        newOrderList.setCellFactory(menuCell -> new MenuListViewCell());
    }

    protected void setUserFields(){
        if(usernameField == null) usernameField = new TextField();

        usernameField.setEditable(false);
        usernameField.setDisable(true);
        roleField.setEditable(false);
        roleField.setDisable(true);
        saveButton.setManaged(false);

        bindUserFields();
    }

    private void bindUserFields() {
        saveButton.visibleProperty().bind(saveButton.managedProperty());
        editButton.visibleProperty().bind(editButton.managedProperty());

        loginManager.bindUserFields(usernameField.textProperty(), firstNameField.textProperty(), lastNameField.textProperty(), countryField.textProperty(),
                roleField.textProperty(), ageField.textProperty(), profileImage.imageProperty());

        firstNameField.editableProperty().bind(saveButton.managedProperty());
        lastNameField.editableProperty().bind(saveButton.managedProperty());
        ageField.editableProperty().bind(saveButton.managedProperty());
        countryField.editableProperty().bind(saveButton.managedProperty());

        firstNameField.disableProperty().bind(saveButton.managedProperty().not());
        lastNameField.disableProperty().bind(saveButton.managedProperty().not());
        ageField.disableProperty().bind(saveButton.managedProperty().not());
        countryField.disableProperty().bind(saveButton.managedProperty().not());
    }

    public void bindOrderProperties(Order currentOrder) {
        this.currentOrder = currentOrder;
        currentDishList.setItems(currentOrder.getDishes());

        orderId.textProperty().bind(currentOrder.getId().asString());
        createdDate.textProperty().bind(Bindings.createObjectBinding(()->
                dateFormatter.format(currentOrder.getCreated().get()),currentOrder.getCreated()));
        createdTime.textProperty().bind(Bindings.createObjectBinding(()->
                timeFormatter.format(currentOrder.getCreated().get()),currentOrder.getCreated()));
        updatedDate.textProperty().bind(Bindings.createObjectBinding(()->
                dateFormatter.format(currentOrder.getUpdated().get()),currentOrder.getUpdated()));
        updatedTime.textProperty().bind(Bindings.createObjectBinding(()->
                timeFormatter.format(currentOrder.getUpdated().get()),currentOrder.getUpdated()));
    }

    public void unbindOrderProperties() {
        this.currentOrder = null;

        orderId.textProperty().unbind();
        createdDate.textProperty().unbind();
        createdTime.textProperty().unbind();
        updatedDate.textProperty().unbind();
        updatedTime.textProperty().unbind();
    }

    protected SortedMap<String, Menu> searchMenu(String prefix) {
        return orderManager.userMenu.subMap(prefix, prefix + Character.MAX_VALUE);
    }

    @FXML
    public void editUserInfo() {
        if(!loginManager.sendInfo.isRunning()) {
            saveButton.setManaged(true);
            editButton.setManaged(false);
        }
    }

    @FXML
    public void saveUserInfo() {
        saveButton.setManaged(false);
        editButton.setManaged(true);

        loginManager.sendUserInfo();
    }

    @FXML
    public void createNewOrder() {
        if(!orderManager.sendOrder.isRunning()) {
            orderManager.sendOrder();
        }
    }

    public void setHistoryListener(ObjectProperty<ChatValue> chatValue, VBox chatBlock, Text textInfo){
        chatBlock.idProperty().addListener((observable1, oldValue1, newValue1) -> {
            if ((newValue1.equals("append") || newValue1.equals("beginning-append")) && chatValue.get() != null) {
                loadOlderHistory(chatValue.get(), chatBlock, textInfo);
            }
        });
    }
    public void setChatAreaListener(ObjectProperty<ChatValue> chat, VBox chatBlock, TextArea chatTextArea){
        chatTextArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                addNewMessage(chat.get(), chatBlock, chatTextArea);
                event.consume();
            }
        });
    }

    public void setChat(ChatValue chat, VBox chatBlock, Text chatInfo, TextArea chatTextArea){

        chatBlock.setId("beginning");
        chatBlock.getChildren().remove(1, chatBlock.getChildren().size());

        ListOrderedMap<LocalDate, Session> sessionsMap = chat.getSessions();
        List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
        List<Session> lastSessions = chatSessions.subList(0, Math.min(pageSize, chatSessions.size()));

        if (lastSessions.size() == pageSize) {
            chatInfo.setText("Scroll for more history");
            chat.setDisplayedSessions(pageSize);
        } else {
            chatInfo.setText("Beginning of the chat");
            chat.setMoreSessions(false);
            chat.setDisplayedSessions(lastSessions.size());
        }

        lastSessions.forEach(session -> appendSession(session, chatBlock, chat, 1));

        chat.getSessionsObservable().addListener((MapChangeListener<LocalDate, Session>) c -> {
            LocalDate sessionDate = c.getKey();
            int index = chat.getSessions().indexOf(sessionDate);
            newSessions(c.getValueAdded(), index, chat, chatTextArea, chatBlock);
        });
    }

    private void newSessions(Session session, int index, ChatValue chat, TextArea chatTextArea, VBox chatBlock) {
        if (index == 0 && !chat.isMoreSessions()) {
            chatTextArea.setText("Beginning of the chat");
        }

        chat.setDisplayedSessions(chat.getDisplayedSessions() + 1);
        appendSession(session, chatBlock, chat, 1);
    }

    private void loadOlderHistory(ChatValue chatValue, VBox chatBlock, Text chatInfo) {
        int displayedSessions = chatValue.getDisplayedSessions();
        int loadedSessions = chatValue.getSessions().size();

        ListOrderedMap<LocalDate, Session> sessionsMap = chatValue.getSessions();
        List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
        List<Session> nextSessions;
        if (loadedSessions > displayedSessions) {

            nextSessions = chatSessions.subList(displayedSessions,
                    Math.min(displayedSessions + pageSize, loadedSessions));

            if (displayedSessions + nextSessions.size() == loadedSessions && !chatValue.isMoreSessions()) {
                chatInfo.setText("Beginning of the chat");
            }
            chatValue.setDisplayedSessions(displayedSessions + nextSessions.size());

            nextSessions.forEach(session -> appendSession(session, chatBlock, chatValue, 1));

        } else if (chatValue.isMoreSessions()) {
            chatManager.getNextSessions(chatValue);
        } else {
            chatInfo.setText("Beginning of the chat");
        }
    }

    @FXML
    public void addNewMessage(ChatValue chat, VBox chatBlock, TextArea textArea){
        int chatId = chat.getChatId();
        int receiverId = chat.getUserId();
        int index = chatBlock.getChildren().size();

        String messageText = textArea.getText();
        textArea.clear();

        if (messageText.length() > 0){
            Message message = new Message(receiverId, LocalTime.now(),LocalDate.now(), messageText, chatId);
            chatManager.sendMessage(messageText, chatId, receiverId);

            ListOrderedMap<LocalDate, Session> sessions = chat.getSessions();

            chatBlock.setId("new-message");
            Session session = sessions.get(message.getSession());
            if (session == null) {
                LocalDate sessionDate = message.getSession();

                session = new Session();
                session.setDate(sessionDate);
                sessions.put(0, sessionDate, session);
                session.getMessages().add(message);

                if (chat.getChatId() == message.getChatId()) {
                    chat.setDisplayedSessions(chat.getDisplayedSessions() + 1);
                    appendSession(session, chatBlock, chat, index);
                }
            } else {
                session.getMessages().add(message);
                if (chat.getChatId() == message.getChatId()) {
                    appendMessage(message, chat, chatBlock);
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
            imageView.setImage(chat.getSecondUserPicture());
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

    private void addNotification() {
        notificationsInfo.setOpacity(0);
        notificationsInfo.setDisable(true);
        isNewNotificationChecked.set(false);
    }

    protected void removeNotification() {
        if (notificationsList.getItems().size() == 0) {
            notificationsInfo.setOpacity(1);
            notificationsInfo.setDisable(false);
        }
    }

    protected void setContentRoot(){
        contentRoot.setPrefWidth(contentRoot.getMinWidth());
        contentRoot.setPrefHeight(contentRoot.getMinHeight());

        contentRoot.setLayoutY((primaryScreenBounds.getHeight() - contentRoot.getPrefHeight()) / 2);
        contentRoot.setLayoutX((primaryScreenBounds.getWidth() - contentRoot.getPrefWidth()) / 2);
    }

    @FXML
    public void showLoggedFirstStyle(){
        stageManager.changeStage(stageManager.firstLoggedStage);
    }

    @FXML
    public void showLoggedSecondStyle(){
        stageManager.changeStage(stageManager.secondLoggedStage);
    }

    @FXML
    public void showLoggedThirdStyle(){
        stageManager.changeStage(stageManager.thirdLoggedStage);
    }

    @FXML
    public void minimize(){
        stageManager.currentStage.setIconified(true);
    }

    @FXML
    public void close(){
        stageManager.currentStage.close();
    }

    @FXML
    protected void logout(){
        loginManager.logout();
    }
}
