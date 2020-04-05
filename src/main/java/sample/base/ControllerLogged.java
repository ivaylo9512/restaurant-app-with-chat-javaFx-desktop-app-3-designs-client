package sample.base;

import Application.RestaurantApplication;
import Helpers.ListViews.DishListViewCell;
import Helpers.ListViews.MenuListViewCell;
import Models.Dish;
import Models.Menu;
import Models.Order;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static Application.RestaurantApplication.*;
import static Application.ServerRequests.*;

public class ControllerLogged implements Controller {
    @FXML
    ListView<Menu> menuList, newOrderList;
    @FXML
    ListView<Dish> dishesList;
    @FXML
    ImageView profileImage;

    @FXML
    protected Label orderId, updatedDate, createdDate, createdTime, updatedTime;
    @FXML
    protected ListView<Dish> currentDishList;
    @FXML
    protected TextField usernameField, firstNameField, lastNameField, countryField, ageField, menuSearch, roleField;
    @FXML
    protected Button saveButton, editButton;
    @FXML
    protected Pane userInfo;
    @FXML
    protected AnchorPane contentRoot, orderPane;
    @FXML
    protected ListView<Order> ordersList;

    protected DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    protected DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    protected DateTimeFormatter dateFormatterSimple = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    protected MediaPlayer notificationSound = RestaurantApplication.notificationSound;
    protected ObservableList<Menu> userMenu = FXCollections.observableArrayList();

    protected Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    protected Order currentOrder = new Order();
    private Pane buttonParent;

    @FXML
    public void initialize() {
        currentDishList.setCellFactory(c -> new DishListViewCell());
        menuList.setCellFactory(menuCell -> new MenuListViewCell());
        newOrderList.setCellFactory(menuCell -> new MenuListViewCell());

        menuList.setItems(userMenu);
        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            userMenu.setAll(searchMenu(newValue.toLowerCase()).values());
        });

        if(usernameField == null) usernameField = new TextField();

        usernameField.setEditable(false);
        usernameField.setDisable(true);
        roleField.setEditable(false);
        roleField.setDisable(true);

        Tooltip loading = new Tooltip("Loading...");
        loading.setShowDelay(Duration.millis(50));
        editButton.setTooltip(loading);

        editButton.visibleProperty().bind(editButton.managedProperty());
        editButton.opacityProperty().bind(Bindings.createDoubleBinding(() ->{
          if(loginManager.sendInfo.runningProperty().get()){
              editButton.getTooltip().setOpacity(1);
              return 0.8;
          }
            editButton.getTooltip().setOpacity(0);
            return 1.0;
        },loginManager.sendInfo.runningProperty()));

        saveButton.setManaged(false);
        saveButton.visibleProperty().bind(saveButton.managedProperty());

        ordersList.setItems(orderManager.orders);
        newOrderList.setItems(orderManager.newOrderList);

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

    private SortedMap<String, Menu> searchMenu(String prefix) {
        return orderManager.userMenu.subMap(prefix, prefix + Character.MAX_VALUE);
    }

    protected void setOrder(Order order) {
        currentOrder.setId(order.getId().get());
        currentOrder.setUpdated(order.getUpdated().get());
        currentOrder.setCreated(order.getCreated().get());
        currentOrder.setDishes(order.getDishes());
        currentOrder.setReady(order.isReady());
        currentOrder.setUserId(order.getUserId());
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
        orderManager.sendOrder();
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
