package sample.base;

import Helpers.ListViews.MenuListViewCell;
import Models.Dish;
import Models.Menu;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.SortedMap;

import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.stageManager;
import static Helpers.ServerRequests.httpClientLongPolling;

public class ControllerLogged {
    @FXML
    Label firstNameLabel, lastNameLabel, countryLabel, ageLabel, roleLabel, roleField;
    @FXML
    TextField firstNameField, lastNameField, countryField, ageField, menuSearch;
    @FXML
    ListView<Menu> menuList, newOrderList;
    @FXML
    ListView<Dish> dishesList;

    protected ObservableList<Menu> userMenu = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        menuList.setCellFactory(menuCell -> new MenuListViewCell());
        newOrderList.setCellFactory(menuCell -> new MenuListViewCell());

        menuList.setItems(userMenu);
        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            userMenu.setAll(searchMenu(newValue.toLowerCase()).values());
        });
    }

    private SortedMap<String, Menu> searchMenu(String prefix) {
        return loginManager.userMenu.subMap(prefix, prefix + Character.MAX_VALUE);
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
    public void logOut(){
        loginManager.logout();
    }
}
