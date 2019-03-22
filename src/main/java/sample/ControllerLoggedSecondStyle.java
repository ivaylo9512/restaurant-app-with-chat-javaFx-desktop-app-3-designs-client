package sample;

import Animations.MoveRoot;
import Animations.TransitionResizeHeight;
import Animations.TransitionResizeWidth;
import Models.Order;
import Models.User;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static Helpers.ServerRequests.*;

public class ControllerLoggedSecondStyle {
    @FXML Label dishesCount, orderId, updatedDate, updatedTime, createdDate, createdTime, roleField, usernameField;
    @FXML Label firstNameLabel, lastNameLabel, countryLabel, ageLabel, roleLabel, usernameLabel;
    @FXML TextField firstNameField, lastNameField, countryField, ageField;
    @FXML AnchorPane menuRoot,menu, menuButtons, menuButtonsContainer, contentRoot,
    profileView, orderInfo, userInfoLabels, userInfoFields, orderView, chatView;
    @FXML VBox orderContainer, dishesContainer;
    @FXML Button menuButton, editButton;
    @FXML Pane profileImageContainer, profileImageClip,profileImageClip1, contentBar;

    @FXML ImageView profileImage;

    private Button displayedOrder;
    private Image userProfileImage;
    private AnchorPane currentView;

    private User loggedUser;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");


    @FXML
    public void initialize() throws Exception{
        loggedUser = loggedUserProperty.getValue();

        loggedUser.getOrders().forEach((integer, order) -> appendOrder(order));

        InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
        userProfileImage = new Image(in);
        in.close();

        displayUserInfo();

        MoveRoot.move(menuButton, menuRoot);
        MoveRoot.move(contentBar, contentRoot);

        menuRoot.getChildren().remove(profileView);

        Circle clip = new Circle(0, 0, 30.8);
        clip.setLayoutX(30.8);
        clip.setLayoutY(30.8);
    }
    @FXML
    public void showChatView(){
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
            ControllerLoggedFirstStyle.orderService.start();
            ControllerLoggedFirstStyle.messageService.start();
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
        if(profileView.isDisabled()) {
            menuRoot.getChildren().add(profileView);
            profileView.setDisable(false);

            TransitionResizeHeight expand = new TransitionResizeHeight(Duration.millis(800), profileView, profileView.getMaxHeight());
            expand.play();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), profileImageContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(300));
            fadeIn.play();
        }else if(profileView.getHeight() == profileView.getMaxHeight()){
            TransitionResizeHeight reverse = new TransitionResizeHeight(Duration.millis(800), profileView, 0);
            reverse.play();
            profileImageContainer.setOpacity(0);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(600), profileImageContainer);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
            Timeline removeView = new Timeline(new KeyFrame(Duration.millis(800), event -> {
                profileView.setDisable(true);
                menuRoot.getChildren().remove(profileView);
            }));
            removeView.play();

        }
    }
    private void displayUserInfo() {
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
    private void appendOrder(Order order){
        int orderId = order.getId();

        Button orderButton = new Button("Order " + orderId);
        orderButton.setId(String.valueOf(orderId));
        orderButton.setOnMouseClicked(e -> showOrder(order, orderButton));

        orderContainer.getChildren().add(0, orderButton);

    }
    private void showOrder(Order order, Button orderButton){
        if(displayedOrder != null){
            displayedOrder.getStyleClass().remove("focused");
        }
        displayedOrder = orderButton;
        displayedOrder.getStyleClass().add("focused");

        orderId.setText(String.valueOf(order.getId()));
        dishesCount.setText(null);
        dishesCount.setText(String.valueOf(order.getDishes().size()));

        createdDate.setText(dateFormatter.format(order.getCreated()));
        createdTime.setText(timeFormatter.format(order.getCreated()));
        updatedDate.setText(dateFormatter.format(order.getUpdated()));
        updatedTime.setText(timeFormatter.format(order.getUpdated()));


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
