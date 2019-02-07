package sample;

import Animations.ResizeWidth;

import Models.JwtUser;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class ControllerLoggedFirstDesign implements Initializable {
    @FXML public Pane menuBar, mainPane, profileSettings, orders;
    @FXML public ImageView jobPosition;
    @FXML public TextField firstName, lastName, userName, age, country, role;
    @FXML public Button edit, editButton;
    @FXML private Text editSaveText;
    @FXML FlowPane flowPane;
    private ResizeWidth resizeLogin;
    private Pane currentPane = new Pane();
    private Boolean editModeOn = false;
    private JwtUser jwtUser;
    @FXML
    public void openButtonPane(MouseEvent event){
        currentPane.getStyleClass().remove("content-pane");
        Pane pane = (Pane) event.getSource();
        String contentPaneId = "#" + pane.getId() + "-content";
        currentPane = (Pane) mainPane.lookup(contentPaneId);
        currentPane.getStyleClass().add("content-pane");
        jobPosition.setImage(new Image(getClass().getResourceAsStream("/chef.png")));
        if(jwtUser.getRole().equals("chef")){
            jobPosition.setImage(new Image(getClass().getResource("/resources/chef.png").toString()));
        }else if(jwtUser.getRole().equals("waiter")){
            jobPosition.setImage(new Image(new File("src/main/java/resources/chef.png").toURI().toString()));
        }
    }
    @FXML
    public void editProperties(){
        if (!editModeOn) {
            profileSettings.getStyleClass().add("editable");
            System.out.println(profileSettings.getStyleClass());
            editSaveText.setText("save");
            editModeOn = true;
            firstName.setDisable(false);
            lastName.setDisable(false);
            userName.setDisable(false);
            age.setDisable(false);
            country.setDisable(false);
            role.setDisable(false);
        }else{
            profileSettings.getStyleClass().remove("editable");
            editSaveText.setText("edit");
            editModeOn = false;
            firstName.setDisable(true);
            lastName.setDisable(true);
            userName.setDisable(true);
            age.setDisable(true);
            country.setDisable(true);
            role.setDisable(true);
            savePropertiesData();
        }
    }

    private void savePropertiesData() {
        jwtUser.setFirstname(firstName.getText());
        jwtUser.setLastname(lastName.getText());
        jwtUser.setUsername(userName.getText());
        jwtUser.setAge(Integer.parseInt(age.getText()));
        jwtUser.setCountry(country.getText());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Preferences userPreference = Preferences.userRoot();
        Gson gson = new Gson();
        jwtUser = gson.fromJson(userPreference.get("user", "{id:0}"), JwtUser.class);
        System.out.println(editButton);
        editButton.setOnMouseEntered(event -> {
            System.out.println("hey");
            if(!editModeOn) {
                resizeLogin = new ResizeWidth(Duration.millis(450), edit, 90);
                resizeLogin.play();
            }
        });
        editButton.setOnMouseExited(event -> {
            if(!editModeOn) {
                resizeLogin.stop();
                edit.setMinWidth(36);
            }
        });
    }
    @FXML
    public void addPane(MouseEvent event){
    }

}
