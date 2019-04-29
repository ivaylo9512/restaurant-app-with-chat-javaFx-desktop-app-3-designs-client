package sample;

import Helpers.ListViews.DishListViewCell;
import Helpers.ListViews.MenuListViewCell;
import Helpers.ListViews.OrderListViewCellSecond;
import Helpers.Services.MessageService;
import Helpers.Services.OrderService;
import Models.Dish;
import Models.Menu;
import Models.Order;
import Models.User;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static Helpers.ServerRequests.*;
import static Helpers.Services.OrderService.mostRecentOrderDate;

public class ControllerLoggedThirdStyle {
    @FXML public ListView<Order> ordersList;
    @FXML public ListView<Dish> dishesList;
    @FXML public ListView<Menu> menuList,newOrderList;
    @FXML public ListView<TextField> notificationsList;
    @FXML public TextField firstNameField, lastNameField, countryField, ageField, menuSearch;
    @FXML public AnchorPane profileView, ordersView, chatsView, ordersMenu, chatsMenu, createRoot,
            userInfoFields, userInfoLabels, contentRoot, menuBar;
    @FXML public Pane profileImageClip;
    @FXML public Label roleField, usernameField, firstNameLabel, lastNameLabel, countryLabel,
            ageLabel, roleLabel, usernameLabel;
    @FXML ImageView profileImage;
    @FXML Button editButton;
    @FXML HBox notificationsInfo;
    private Image userProfileImage;

    private MediaPlayer notificationSound;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private TreeMap<String, Menu> menuMap = new TreeMap<>();

    private User loggedUser;

    private MessageService messageService;
    private OrderService orderService;
    private AnchorPane currentView, currentMenu;
    private Text currentText;
    private Order currentOrder;

    @FXML
    public void initialize(){
        ordersList.setCellFactory(orders -> new OrderListViewCellSecond());
        dishesList.setCellFactory(dish -> new DishListViewCell());
        menuList.setCellFactory(menu -> new MenuListViewCell());
        newOrderList.setCellFactory(menu -> new MenuListViewCell());

        waitForNewOrders();

        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Menu> observableList = FXCollections.observableArrayList();
            searchMenu(newValue.toLowerCase()).forEach((s, menu) -> observableList.add(menu));
            menuList.setItems(observableList);
        });

        Media sound = new Media(getClass()
                .getResource("/notification.mp3")
                .toExternalForm());
        notificationSound = new MediaPlayer(sound);
        notificationSound.setOnEndOfMedia(() -> notificationSound.stop());

        Rectangle clip = new Rectangle();
        clip.heightProperty().bind(createRoot.prefHeightProperty());
        clip.widthProperty().bind(createRoot.prefWidthProperty());
        createRoot.setClip(clip);

        Circle profileClip = new Circle(40.5, 40.5, 40.5);
        profileImageClip.setClip(profileClip);
    }

    public void setStage() throws Exception {
        loginAnimation();

        loggedUser = loggedUserProperty.getValue();
        loggedUser.getRestaurant().getMenu().forEach(menu -> menuMap.put(menu.getName().toLowerCase(), menu));

        ObservableList<Order> orders = FXCollections.observableArrayList(loggedUser.getOrders().values());
        FXCollections.reverse(orders);

        mostRecentOrderDate = getMostRecentOrderDate(loggedUser.getRestaurant().getId());
        orderService.start();

        menuList.setItems(FXCollections.observableArrayList(loggedUser.getRestaurant().getMenu()));

        ordersList.setItems(orders);

        InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
        userProfileImage = new Image(in);
        in.close();


        displayUserFields();
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
        menuMap.clear();

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

    private void displayView(AnchorPane requestedView, AnchorPane requestedMenu){
        if(currentView != null){
            currentView.setOpacity(0);
            currentView.setDisable(true);
        }

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
    }
    public void showOrder(int orderId){
        currentOrder = loggedUser.getOrders().get(orderId);
        currentOrder.getDishes().forEach(dish -> dish.setOrderId(currentOrder.getId()));

        dishesList.setItems(FXCollections.observableArrayList(currentOrder.getDishes()));
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

                    if(currentOrder != null && currentOrder.getId() == orderId) {
                        Label ready = (Label) dishesList.lookup("#dish" + dish.getId());

                        if (ready != null && ready.getText().equals("X") && dish.getReady()) {
                            addNotification(dish.getName() + " from order " + orderId + " is ready.");
                            ready.setText("O");
                            ready.setUserData("ready");
                        }
                    }
                });
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
            LoginSecondStyle.alert.showAndWait();
        }
    }
}
