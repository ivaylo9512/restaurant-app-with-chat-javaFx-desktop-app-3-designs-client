package controllers.base;

import helpers.listviews.DishListViewCell;
import helpers.listviews.MenuListViewCell;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import models.*;
import models.Menu;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import java.util.SortedMap;
import static application.RestaurantApplication.*;

public abstract class ControllerLogged extends ControllerAdjustable{
    @FXML
    public Pane root, contentRoot;
    @FXML
    ImageView profileImage;
    @FXML
    Node notificationsInfo;

    @FXML
    protected Label orderId, updatedDate, createdDate, createdTime, updatedTime;
    @FXML
    protected TextField usernameField, firstNameField, lastNameField, countryField, ageField, menuSearch, roleField;
    @FXML
    protected Button saveButton, editButton, createButton;
    @FXML
    protected Pane mainChat, secondChat, notificationIcon;
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
    protected Node notificationsView;
    @FXML
    protected ScrollPane mainChatScroll, secondChatScroll;
    @FXML
    protected TextArea mainChatTextArea, secondChatTextArea;
    @FXML
    protected Text mainChatInfo, secondChatInfo;
    @FXML
    protected VBox mainChatBlock, secondChatBlock;
    @FXML
    protected Slider fontSizeSlider;

    protected static BooleanProperty isNewNotificationChecked = new SimpleBooleanProperty(true);
    protected ObservableList<Menu> userMenu = FXCollections.observableArrayList();

    protected ProgressIndicator editIndicator = new ProgressIndicator();
    protected ProgressIndicator createIndicator = new ProgressIndicator();

    protected ObjectProperty<ChatValue> mainChatValue = chatManager.mainChatValue;
    protected ObjectProperty<ChatValue> secondChatValue = chatManager.secondChatValue;

    private Node editButtonGraphic, createButtonGraphic;
    private String createButtonText;

    public ReadOnlyObjectProperty<Order> currentOrder;

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
        orderId.textProperty().unbind();
        createdDate.textProperty().unbind();
        createdTime.textProperty().unbind();
        updatedDate.textProperty().unbind();
        updatedTime.textProperty().unbind();
    }

    public void resetOrderFields(){
        orderId.setText(null);
        createdDate.setText(null);
        createdTime.setText(null);
        updatedDate.setText(null);
        updatedTime.setText(null);
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
        notificationsInfo.setDisable(true);
        isNewNotificationChecked.set(false);
    }

    protected void removeNotification() {
        if (notificationsList.getItems().size() == 0) {
            notificationsInfo.setDisable(false);
        }
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
    protected void logout(){
        loginManager.logout();
    }
}
