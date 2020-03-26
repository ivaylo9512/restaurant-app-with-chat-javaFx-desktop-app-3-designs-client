package sample.base;

import Models.Menu;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.SortedMap;

import static Application.RestaurantApplication.loginManager;

public class ControllerLogged {
    @FXML
    Label firstNameLabel, lastNameLabel, countryLabel, ageLabel, roleLabel, roleField;
    @FXML
    TextField firstNameField, lastNameField, countryField, ageField, menuSearch;
    @FXML
    ListView<Menu> menuList, newOrderList;

    private ObservableList<Menu> userMenu = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        menuList.setItems(userMenu);
        menuSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            userMenu.setAll(searchMenu(newValue.toLowerCase()).values());
        });
    }

    private SortedMap<String, Menu> searchMenu(String prefix) {
        return loginManager.userMenu.subMap(prefix, prefix + Character.MAX_VALUE);
    }
}
