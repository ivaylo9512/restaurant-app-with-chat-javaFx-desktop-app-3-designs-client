package sample;

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
    private ResizeWidthTransition resizeLogin;
    private Pane currentPane = new Pane();
    private Boolean editModeOn = false;
    private JwtUser jwtUser;
//the waiter sends order take id from token set created by dish ready take id from
// token cooked by and stream the dishes filter which are not ready if length is 0 set order ready
    //when the edit of profile is proceed you take the information and create new user with it so the orders and dishes are not changed

    ///NEW NEW NEW vrustham podredeni po updated date tezi for eachvam lista tezi koito sa s created date po-golqm ot moq current date koi e v momenta gi addvam kum hashmapa s orderite a tezi koito ne sa purvo updatevam viewto sled tova
    // for eachvam vsichki dishes tezi koito sa ready sled current date gi puskam s notification i pazq length i gi
    // broq i ako sa ravni na lenghta deletevam ot viewto i ot hashmapa namiram v hashmapa po id i gi updatevam tam
    // i nakraq vzimam na purviq element date-a i tova stava currentUpdateDate koito polzvam
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
                resizeLogin = new ResizeWidthTransition(Duration.millis(450), edit, 90);
                resizeLogin.play();
            }
        });
        editButton.setOnMouseExited(event -> {
            if(!editModeOn) {
                resizeLogin.stop();
                edit.setMinWidth(36);
            }
        });
//        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
//        String date = dateFormat.format(order.getCreated());
//        String time = timeFormat.format(order.getCreated());
//        System.out.println(date);
//        System.out.println(time);

    }
    @FXML
    public void addPane(MouseEvent event){
        System.out.println(event.getPickResult().getIntersectedNode().getTypeSelector());
        System.out.println(event.getPickResult().getIntersectedNode().getLayoutX());
        System.out.println(event.getPickResult().getIntersectedNode().getLayoutY());
        Pane pane = new Pane();
        pane.getStyleClass().add("order");
        pane.setLayoutY(event.getPickResult().getIntersectedNode().getLayoutY() + 33);
        pane.setLayoutX(event.getPickResult().getIntersectedNode().getLayoutX() + 21);
        orders.getChildren().add(pane);
//        MoveStage.moveStage((Pane)event.getPickResult().getIntersectedNode(),(Pane)event.getPickResult().getIntersectedNode());
//        if(pane.getId() == null || !pane.getId().equals("flowPane")) {
//            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), pane);
//            fadeTransition.setToValue(0);
//            ResizeHeightTransition resize = new ResizeHeightTransition(Duration.millis(600), pane, 0);
//            resize.setDelay(Duration.millis(500));
//            resize.play();
//        }else{
//            Pane pane1 = new Pane();
//            Pane flowPane = (Pane) event.getSource();
//            flowPane.getChildren().add(0, pane1);
//        }
    }

}
