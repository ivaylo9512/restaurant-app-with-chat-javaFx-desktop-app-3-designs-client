package sample;

import Animations.MoveRoot;
import Animations.TransitionResizeHeight;
import Animations.TransitionResizeWidth;
import Helpers.Services.MessageService;
import Helpers.Services.OrderService;
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
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static Helpers.ServerRequests.*;
import static Helpers.Services.OrderService.mostRecentOrderDate;

public class ControllerLoggedSecondStyle {
    @FXML Label dishesCountLabel, orderIdLabel, updatedDateLabel, updatedTimeLabel,
            createdDateLabel, createdTimeLabel, roleField, usernameField, firstNameLabel,
            lastNameLabel, countryLabel, ageLabel, roleLabel, usernameLabel;

    @FXML AnchorPane menuRoot,menu, menuButtons, menuButtonsContainer, contentRoot,
            menuContent, orderInfo, userInfoLabels, userInfoFields, orderView, chatView;

    @FXML TextField firstNameField, lastNameField, countryField, ageField;
    @FXML VBox dishesContainer;
    @FXML Button menuButton, editButton;
    @FXML Pane profileImageContainer, profileImageClip, contentBar;
    @FXML ListView<String> ordersList;
    @FXML ImageView profileImage;

    private Image userProfileImage;
    private AnchorPane currentView;

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private User loggedUser;
    public static MessageService messageService;
    public static OrderService orderService;



    @FXML
    public void initialize() {

        ordersList.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            String menuItem = ordersList.getSelectionModel().getSelectedItem();
            int orderId = Integer.valueOf(menuItem.substring(6));
            Order selectedOrder = loggedUser.getOrders().get(orderId);

            showOrder(selectedOrder);
        });

        waitForNewOrders();

        MoveRoot.move(menuButton, menuRoot);
        MoveRoot.move(contentBar, contentRoot);

        menuRoot.getChildren().remove(menuContent);

        Circle clip = new Circle(0, 0, 30.8);
        clip.setLayoutX(30.8);
        clip.setLayoutY(30.8);
        profileImageClip.setClip(clip);
    }


    @FXML
    public void showChatView(){
        DialogPane dialog = LoggedSecondStyle.alert.getDialogPane();
        dialog.setHeaderText("HEY");
        dialog.setContentText(" ");
        LoggedSecondStyle.alert.showAndWait();
        displayView(chatView);
    }
    @FXML
    public void showOrderView(){
        displayView(orderView);
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
                        Label ready = (Label) dishesContainer.lookup("#dish" + dish.getId());

                        if (ready.getText().equals("X") && dish.getReady()) {
//                        addNotification(dish.getName() + " from order " + orderId + " is ready.");
                            ready.setText("O");
                        }
                    }
                });

                if (order.isReady()) {
//                    addNotification("Order " + orderId + " is ready.");
                }

            } else {
                ordersList.getItems().add(0, "Order " + orderId);
                if(order.getUserId() != loggedUser.getId()){
//                    addNotification("New order created " + orderId);
                }
            }
            loggedUser.getOrders().put(orderId, order);
        });
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

    private void showLoginStageAlert(String message) {
        DialogPane dialog = LoginFirstStyle.alert.getDialogPane();
        dialog.setContentText(message);
        LoginFirstStyle.alert.showAndWait();
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
    public void displayUserInfo() throws Exception{
        loggedUser = loggedUserProperty.getValue();

        mostRecentOrderDate = getMostRecentOrderDate(loggedUser.getRestaurant().getId());

        ObservableList<String> orders = FXCollections.observableArrayList();
        loggedUser.getOrders().forEach((integer, order) -> orders.add("Order " + order.getId()));
        FXCollections.reverse(orders);
        ordersList.setItems(orders);

        orderService.start();

        InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
        userProfileImage = new Image(in);
        in.close();

        displayUserFields();
    }
    public void resetStage(){
        ordersList.getItems().clear();
        mostRecentOrderDate = null;
        userProfileImage = null;
        loggedUser = null;

        resetUserFields();


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
        LoginFirstStyle.stage.show();
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
            }
        }
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
    public void showProfile(){
        if(menuContent.isDisabled()) {
            menuRoot.getChildren().add(menuContent);
            menuContent.setDisable(false);

            TransitionResizeHeight expand = new TransitionResizeHeight(Duration.millis(800), menuContent, menuContent.getMaxHeight());
            expand.play();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), profileImageContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(300));
            fadeIn.play();

        }else if(menuContent.getPrefHeight() == menuContent.getMaxHeight()){
            TransitionResizeHeight reverse = new TransitionResizeHeight(Duration.millis(800), menuContent, 0);
            reverse.play();
            profileImageContainer.setOpacity(0);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(600), profileImageContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
            Timeline removeView = new Timeline(new KeyFrame(Duration.millis(800), event -> {
                menuContent.setDisable(true);
                menuRoot.getChildren().remove(menuContent);
            }));
            removeView.play();
        }
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

    private void showOrder(Order order){

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


        dishesContainer.getChildren().clear();
        order.getDishes().forEach(dish -> {
            Label price = new Label(String.valueOf(dish.getId()));
            Label name = new Label(dish.getName());
            Label ready = new Label();

            if (dish.getReady()) {
                ready.setText("O");
            } else {
                ready.setText("X");
            }
            ready.setId("dish" + dish.getId());

            price.getStyleClass().add("price");
            ready.getStyleClass().add("ready");
            name.getStyleClass().add("name");
            HBox.setHgrow(name, Priority.ALWAYS);
            HBox dishContainer = new HBox(price, name, ready);

            dishesContainer.getChildren().add(dishContainer);
        });
    }
}
