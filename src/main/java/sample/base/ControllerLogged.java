package sample.base;

import Animations.ResizeRoot;
import Helpers.ListViews.DishListViewCell;
import Helpers.ListViews.MenuListViewCell;
import Models.*;
import Models.Menu;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;

import java.time.format.DateTimeFormatter;
import java.util.SortedMap;

import static Application.RestaurantApplication.*;

public class ControllerLogged implements Controller {
    @FXML
    ListView<Menu> menuList, newOrderList;
    @FXML
    ListView<Dish> dishesList;
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
    protected AnchorPane contentRoot, orderPane, notificationsView;
    @FXML
    protected ListView<Order> ordersList;
    @FXML
    protected ListView<Notification> notificationsList;
    @FXML
    protected ListView<Dish> currentDishList;
    @FXML
    protected ListView<ChatValue> chatUsersList;
    @FXML
    protected Node notificationIcon;


    protected DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    protected DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    protected DateTimeFormatter dateFormatterSimple = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    protected static BooleanProperty isNewNotificationChecked = new SimpleBooleanProperty(true);
    protected ObservableList<Menu> userMenu = FXCollections.observableArrayList();

    protected Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
    protected ProgressIndicator editIndicator = new ProgressIndicator();
    protected ProgressIndicator createIndicator = new ProgressIndicator();

    private Node editButtonGraphic, createButtonGraphic;
    private String createButtonText;
    protected Order currentOrder;

    @FXML
    public void initialize() {
        setListsFactories();
        setListsItems();
        setGraphicIndicators();
        setNotificationsListeners();
        setUserFields();

        menuSearch.textProperty().addListener((observable, oldValue, newValue) ->
                userMenu.setAll(searchMenu(newValue.toLowerCase()).values()));

        saveButton.visibleProperty().bind(saveButton.managedProperty());
        editButton.visibleProperty().bind(editButton.managedProperty());

        ResizeRoot.addListeners(contentRoot);
    }


    private void setNotificationsListeners() {
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
            notificationIcon.setOpacity(0);
        });
    }

    private void setGraphicIndicators() {
        editButtonGraphic = editButton.getGraphic();
        createButtonGraphic = createButton.getGraphic();
        createButtonText = createButton.getText();

        loginManager.sendInfo.runningProperty().addListener((observable, oldValue, newValue)->{
            if(newValue){
                editButton.setGraphic(editIndicator);
                editButton.setText(null);
            }else {
                editButton.setGraphic(editButtonGraphic);
                editButton.setText("Edit");
            }
        });

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

    private void setListsItems() {
        ordersList.setItems(orderManager.orders);
        newOrderList.setItems(orderManager.newOrderList);
        notificationsList.setItems(notificationManager.notifications);
        menuList.setItems(userMenu);
        chatUsersList.setItems(chatManager.chatsList);
    }

    public void setListsFactories() {
        currentDishList.setCellFactory(c -> new DishListViewCell());
        menuList.setCellFactory(menuCell -> new MenuListViewCell());
        newOrderList.setCellFactory(menuCell -> new MenuListViewCell());

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

    private void setUserFields(){
        if(usernameField == null) usernameField = new TextField();

        usernameField.setEditable(false);
        usernameField.setDisable(true);
        roleField.setEditable(false);
        roleField.setDisable(true);
        saveButton.setManaged(false);

        bindUserFields();
    }

    private void bindUserFields() {
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

    private SortedMap<String, Menu> searchMenu(String prefix) {
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

    public void setStage() throws Exception{
        userMenu.setAll(orderManager.userMenu.values());

        contentRoot.setPrefWidth(contentRoot.getMinWidth());
        contentRoot.setPrefHeight(contentRoot.getMinHeight());

        contentRoot.setLayoutY((primaryScreenBounds.getHeight() - contentRoot.getPrefHeight()) / 2);
        contentRoot.setLayoutX((primaryScreenBounds.getWidth() - contentRoot.getPrefWidth()) / 2);
    }

    public void resetStage(){
        if(ordersList.getItems().size() > 0) ordersList.scrollTo(0);
        if(notificationsList.getItems().size() > 0) notificationsList.scrollTo(0);

        menuList.getItems().clear();
        ordersList.getSelectionModel().clearSelection();
        unbindOrderProperties();

        notificationsView.setOpacity(0);
        notificationsView.setDisable(true);
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
