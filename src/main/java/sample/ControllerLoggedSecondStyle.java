package sample;

import Animations.MoveRoot;
import Animations.ResizeRoot;
import Animations.TransitionResizeHeight;
import Animations.TransitionResizeWidth;
import Helpers.ListViews.ChatsListViewCell;
import Helpers.ListViews.DishListViewCell;
import Helpers.ListViews.MenuListViewCell;
import Helpers.Services.MessageService;
import Helpers.Services.OrderService;
import Models.*;
import Models.Menu;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static Helpers.ServerRequests.*;
import static Helpers.Services.OrderService.mostRecentOrderDate;

public class ControllerLoggedSecondStyle {
    @FXML Label dishesCountLabel, orderIdLabel, updatedDateLabel, updatedTimeLabel,
            createdDateLabel, createdTimeLabel, roleField, usernameField, firstNameLabel,
            lastNameLabel, countryLabel, ageLabel, roleLabel, usernameLabel;

    @FXML AnchorPane menuRoot,menu, menuButtons, menuButtonsContainer, contentRoot, profileView,
            notificationsView, menuContent, orderInfo, userInfoLabels, userInfoFields, orderView,
            chatView, userChatsClip, createView, dishesContainer;

    @FXML TextField firstNameField, lastNameField, countryField, ageField, menuSearch;
    @FXML Button menuButton, editButton;
    @FXML HBox notificationsInfo;
    @FXML Pane profileImageContainer, profileImageClip, contentBar;
    @FXML ListView<String> ordersList, notificationsList;
    @FXML ListView<Chat> userChats;
    @FXML ListView<Menu> menuList, newOrderList;
    @FXML ListView<Dish> dishesList;
    @FXML ImageView profileImage;

    public static Image userProfileImage;
    private AnchorPane currentView, currentMenuView;

    private MediaPlayer notificationSound;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private TreeMap<String, Menu> menuMap = new TreeMap<>();
    private Map<Integer, ChatValue> chatsMap = new HashMap<>();

    private User loggedUser;
    private static MessageService messageService;
    private static OrderService orderService;

    @FXML
    public void initialize() {
        menuList.setCellFactory(menuCell -> new MenuListViewCell());
        newOrderList.setCellFactory(menuCell -> new MenuListViewCell());
        userChats.setCellFactory(menuCell -> new ChatsListViewCell());
        dishesList.setCellFactory(dishCell -> new DishListViewCell());

        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Menu> observableList = FXCollections.observableArrayList();
            searchMenu(newValue.toLowerCase()).forEach((s, menu) -> observableList.add(menu));
            menuList.setItems(observableList);
        });

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

    }
    private SortedMap<String, Menu> searchMenu(String prefix) {
        return menuMap.subMap(prefix, prefix + Character.MAX_VALUE);
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


    private void updateNewOrders(List<Order> newOrders) {
        newOrders.forEach(order -> {
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
                messageService.reset();
                orderService.reset();
                showLoginStageAlert("Session has expired.");

            } else if(service.getException().getMessage().equals("Socket closed")) {
                service.reset();

            }else{
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(3000), event -> service.restart()));
                timeline.play();
            }
        }
    }
    private void showLoggedStageAlert(String message) {
        DialogPane dialog = LoggedSecondStyle.alert.getDialogPane();
        dialog.setContentText(message);
        LoggedSecondStyle.alert.showAndWait();
    }
    private void showLoginStageAlert(String message) {
        DialogPane dialog = LoginSecondStyle.alert.getDialogPane();
        dialog.setContentText(message);
        LoginSecondStyle.alert.showAndWait();
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
        TransitionResizeWidth reverse = new TransitionResizeWidth(Duration.millis(700), menu, 38.5);
        reverse.play();
        menuButtonsContainer.getChildren().remove(menuButtons);
    }
    public void expandMenuContent(){
        TransitionResizeHeight expand = new TransitionResizeHeight(Duration.millis(800), menuContent, menuContent.getMaxHeight());
        expand.play();
    }
    public void reverseMenuContent(){
        TransitionResizeHeight reverse = new TransitionResizeHeight(Duration.millis(800), menuContent, 0);
        reverse.play();
    }
    public void displayUserInfo() throws Exception{
        loggedUser = loggedUserProperty.getValue();
        loggedUser.getRestaurant().getMenu().forEach(menu -> menuMap.put(menu.getName().toLowerCase(), menu));

        mostRecentOrderDate = getMostRecentOrderDate(loggedUser.getRestaurant().getId());

        menuList.setItems(FXCollections.observableArrayList(loggedUser.getRestaurant().getMenu()));

        ObservableList<Chat> chats = FXCollections.observableArrayList(getChats());
        userChats.setItems(chats);

        ObservableList<String> orders = FXCollections.observableArrayList(loggedUser.getOrders().values()
                .stream()
                .map(order -> "Order " + order.getId())
                .collect(Collectors.toList()));

        FXCollections.reverse(orders);
        ordersList.setItems(orders);

        waitForNewOrders();
        orderService.start();

        InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
        userProfileImage = new Image(in);
        in.close();

        displayUserFields();
    }

    public void resetStage(){
        mostRecentOrderDate = null;
        userProfileImage = null;
        loggedUser = null;

        menuMap.clear();

        userChats.getItems().clear();
        ordersList.getItems().clear();
        notificationsList.getItems().clear();

        orderService = new OrderService();

        resetUserFields();
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
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        LoggedSecondStyle.stage.close();
        if(LoginSecondStyle.stage != null) {
            LoginSecondStyle.stage.show();
        }else{
            try{
                LoginSecondStyle.displayLoginScene();
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

        LoggedSecondStyle.stage.close();
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

        createdDateLabel.setText(dateFormatter.format(order.getCreated()));
        createdTimeLabel.setText(timeFormatter.format(order.getCreated()));
        updatedDateLabel.setText(dateFormatter.format(order.getUpdated()));
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
}
