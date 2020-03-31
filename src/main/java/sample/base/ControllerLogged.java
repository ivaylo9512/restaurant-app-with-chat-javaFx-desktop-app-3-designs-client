package sample.base;

import Application.RestaurantApplication;
import Helpers.ListViews.MenuListViewCell;
import Models.Dish;
import Models.Menu;
import Models.Order;
import Models.User;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static Application.RestaurantApplication.*;
import static Helpers.ServerRequests.*;

public class ControllerLogged implements Controller {
    @FXML
    ListView<Menu> menuList, newOrderList;
    @FXML
    ListView<Dish> dishesList;
    @FXML
    ImageView profileImage;

    @FXML
    protected TextField usernameField, firstNameField, lastNameField, countryField, ageField, menuSearch, roleField;
    @FXML
    protected Button saveButton, editButton;
    @FXML
    protected Pane userInfo;
    @FXML
    protected AnchorPane contentRoot;
    @FXML
    protected ListView<Order> ordersList;

    protected DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    protected DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    protected DateTimeFormatter dateFormatterSimple = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    protected MediaPlayer notificationSound = RestaurantApplication.notificationSound;
    protected ObservableList<Menu> userMenu = FXCollections.observableArrayList();

    protected Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    private BooleanProperty isEditable = new SimpleBooleanProperty(false);
    private User oldInfo;
    private Pane buttonParent;

    @FXML
    public void initialize() {
        menuList.setCellFactory(menuCell -> new MenuListViewCell());
        newOrderList.setCellFactory(menuCell -> new MenuListViewCell());

        menuList.setItems(userMenu);
        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            userMenu.setAll(searchMenu(newValue.toLowerCase()).values());
        });

        buttonParent = (Pane) saveButton.getParent();
        buttonParent.getChildren().remove(saveButton);

        if(usernameField == null) usernameField = new TextField();

        usernameField.setEditable(false);
        usernameField.setDisable(true);
        roleField.setEditable(false);
        roleField.setDisable(true);

        ordersList.setItems(orderManager.orders);
        bindUserFields();
    }

    private void bindUserFields() {
        loginManager.bindUserFields(usernameField.textProperty(), firstNameField.textProperty(), lastNameField.textProperty(), countryField.textProperty(),
                roleField.textProperty(), ageField.textProperty(), profileImage.imageProperty());

        firstNameField.editableProperty().bind(isEditable);
        lastNameField.editableProperty().bind(isEditable);
        ageField.editableProperty().bind(isEditable);
        countryField.editableProperty().bind(isEditable);

        firstNameField.disableProperty().bind(isEditable.not());
        lastNameField.disableProperty().bind(isEditable.not());
        ageField.disableProperty().bind(isEditable.not());
        countryField.disableProperty().bind(isEditable.not());
    }

    private SortedMap<String, Menu> searchMenu(String prefix) {
        return orderManager.userMenu.subMap(prefix, prefix + Character.MAX_VALUE);
    }

    @FXML
    public void editUserInfo() {
        isEditable.setValue(true);

        buttonParent.getChildren().add(saveButton);
        buttonParent.getChildren().remove(editButton);

        oldInfo = new User(usernameField.getText(), firstNameField.getText(), lastNameField.getText(), Integer.valueOf(ageField.getText()), countryField.getText());
    }

    @FXML
    public void saveUserInfo() {
        isEditable.setValue(false);

        buttonParent.getChildren().add(editButton);
        buttonParent.getChildren().remove(saveButton);

        if (loginManager.isUserEdited(oldInfo)) {
            User user = sendUserInfo(firstNameField.getText(), lastNameField.getText(),
                    ageField.getText(), countryField.getText());

            if (user == null) {
                firstNameField.setText(oldInfo.getFirstName().get());
                lastNameField.setText(oldInfo.getLastName().get());
                ageField.setText(oldInfo.getAge().get());
                countryField.setText(oldInfo.getCountry().get());
            }
        }
    }

    @FXML
    public void createNewOrder() {
        if (roleField.getText().equals("Server")) {
            List<Dish> dishes = newOrderList.getItems().stream().map(menu -> new Dish(menu.getName())).collect(Collectors.toList());
            if(dishes.size() > 0) {

                if(sendOrder(new Order(dishes)))
                    newOrderList.getItems().clear();

            }else{
                stageManager.showAlert("Order must have at least one dish.");
            }
        } else {
            stageManager.showAlert("You must be a server to create orders.");
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

    public void setStage() throws Exception{
        userMenu.setAll(orderManager.userMenu.values());

        contentRoot.setPrefWidth(contentRoot.getMinWidth());
        contentRoot.setPrefHeight(contentRoot.getMinHeight());

        contentRoot.setLayoutY((primaryScreenBounds.getHeight() - contentRoot.getPrefHeight()) / 2);
        contentRoot.setLayoutX((primaryScreenBounds.getWidth() - contentRoot.getPrefWidth()) / 2);
    }

    public void resetStage(){
        newOrderList.getItems().clear();
        menuList.getItems().clear();
    }

    @FXML
    public void showLoggedFirstStyle(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        stageManager.changeStage(stageManager.firstLoggedStage);
    }

    @FXML
    public void showLoggedSecondStyle(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        stageManager.changeStage(stageManager.secondLoggedStage);
    }

    @FXML
    public void showLoggedThirdStyle(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

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
