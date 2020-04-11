package Application;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sample.base.Controller;


import java.io.IOException;

import static Application.RestaurantApplication.loginManager;

public class StageManager {
    private Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
    private Stage primaryStage;

    public Stage currentStage;
    public Controller currentController;
    public Alert currentAlert;

    public Alert firstLoginAlert;
    public Controller firstLoginController;
    public Stage firstLoginStage;
    public Alert secondLoginAlert;
    public Controller secondLoginController;
    public Stage secondLoginStage;
    public Alert thirdLoginAlert;
    public Controller thirdLoginController;
    public Stage thirdLoginStage;
    public Alert firstLoggedAlert;
    public Controller firstLoggedController;
    public Stage firstLoggedStage;
    public Alert secondLoggedAlert;
    public Controller secondLoggedController;
    public Stage secondLoggedStage;
    public Alert thirdLoggedAlert;
    public Controller thirdLoggedController;
    public Stage thirdLoggedStage;

    private StageManager(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;

        initializeFirstLoginStyle(primaryStage);

        currentStage = primaryStage;
        currentAlert = firstLoginAlert;
        currentController = firstLoginController;

        initializeSecondLoginStyle(new Stage());
//        initializeThirdLoginStyle(new Stage());

        initializeFirstLoggedStyle(new Stage());
        initializeSecondLoggedStyle(new Stage());
//        initializeThirdLoggedStyle(new Stage());

        firstLoggedStage.initOwner(firstLoginStage);
        secondLoginStage.initOwner(secondLoggedStage);
//        secondLoggedStage.initOwner(secondLoginStage);
//        thirdLoginStage.initOwner(thirdLoggedStage);
//        thirdLoggedStage.initOwner(thirdLoginStage);

        currentStage.show();
    }
    static StageManager initialize(Stage primaryStage) throws Exception {
        return new StageManager(primaryStage);
    }

    void changeToOwner(){
        Stage stage = currentStage == primaryStage ? firstLoggedStage
                : (Stage)currentStage.getOwner();
        changeStage(stage);
    }

    public void changeStage(Stage stage){
        currentStage.close();
        if(stage == firstLoginStage){
            currentStage = firstLoginStage;
            currentController = firstLoginController;
            currentAlert = firstLoginAlert;
        }else if(stage == secondLoginStage){
            currentStage = secondLoginStage;
            currentController = secondLoginController;
            currentAlert = secondLoginAlert;
        }else if(stage == thirdLoginStage){
            currentStage = thirdLoginStage;
            currentController = thirdLoginController;
            currentAlert = thirdLoginAlert;
        }else if(stage == firstLoggedStage){
            currentStage = firstLoggedStage;
            currentController = firstLoggedController;
            currentAlert = firstLoggedAlert;
        }else if(stage == secondLoggedStage){
            currentStage = secondLoggedStage;
            currentController = secondLoggedController;
            currentAlert = secondLoggedAlert;
        }else {
            currentStage = thirdLoggedStage;
            currentController = thirdLoggedController;
            currentAlert = thirdLoggedAlert;
        }
        currentStage.show();
    }

    public void showAlert(String exception) {
        currentAlert.getDialogPane().setContentText(exception);
        currentAlert.showAndWait();
    }

    private void initializeFirstLoginStyle(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-first.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-first.css").toString());
        scene.setFill(Color.TRANSPARENT);

        firstLoginController = loader.getController();
        firstLoginStage = createStage(stage, scene, firstLoginController);
        firstLoginAlert = createAlert(firstLoginStage);
    }

    private void initializeSecondLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-second.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-second.css").toString());
        scene.setFill(Color.TRANSPARENT);

        secondLoginController = loader.getController();
        secondLoginStage = createStage(stage, scene, secondLoginController);
        secondLoginAlert = createAlert(secondLoginStage);
    }

    private void initializeThirdLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-third.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-third.css").toString());
        scene.setFill(Color.TRANSPARENT);

        thirdLoginController = loader.getController();
        thirdLoginStage = createStage(stage, scene, thirdLoginController);
        thirdLoginAlert = createAlert(thirdLoginStage);
    }

    private void initializeFirstLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-first.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/logged-first.css").toString());
        scene.setFill(Color.TRANSPARENT);

        firstLoggedController = loader.getController();
        firstLoggedStage = createStage(stage, scene, firstLoggedController);
        firstLoggedAlert = createAlert(thirdLoginStage);
    }

    private void initializeSecondLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-second.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/logged-second.css").toString());
        scene.setFill(Color.TRANSPARENT);

        secondLoggedController = loader.getController();
        secondLoggedStage = createStage(stage, scene, secondLoggedController);
        secondLoggedAlert = createAlert(thirdLoginStage);
    }

    private void initializeThirdLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-third.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/logged-third.css").toString());
        scene.setFill(Color.TRANSPARENT);

        thirdLoggedController = loader.getController();
        thirdLoggedStage = createStage(stage, scene, thirdLoggedController);
        thirdLoggedAlert = createAlert(thirdLoginStage);
    }

    private Alert createAlert(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.initStyle(StageStyle.TRANSPARENT);
        DialogPane dialog = alert.getDialogPane();
        dialog.setGraphic(null);
        dialog.getStyleClass().add("alert-box");
        return alert;
    }

    private Stage createStage(Stage stage, Scene scene, Controller controller) {

        stage.showingProperty().addListener((observable, oldValue, isShowing) -> {
            if(!isShowing){
                controller.resetStage();
            }else{
                try {
                    controller.setStage();
                } catch (Exception e) {
                    loginManager.logout();
                    showAlert(e.getMessage());
                }
            }
        });
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);

        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        return stage;
    }
}
