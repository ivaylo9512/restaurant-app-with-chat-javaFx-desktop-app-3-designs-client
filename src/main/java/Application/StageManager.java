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
import sample.Controller;


import java.io.IOException;

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

        currentStage = initializeFirstLoginStyle(primaryStage);
        currentAlert = firstLoginAlert;
        currentController = firstLoginController;

        initializeSecondLoginStyle(new Stage());
        initializeThirdLoginStyle(new Stage());

        initializeFirstLoggedStyle(new Stage());
        initializeSecondLoggedStyle(new Stage());
        initializeThirdLoggedStyle(new Stage());

        firstLoggedStage.initOwner(firstLoginStage);
        secondLoginStage.initOwner(secondLoggedStage);
        secondLoggedStage.initOwner(secondLoginStage);
        thirdLoginStage.initOwner(thirdLoggedStage);
        thirdLoggedStage.initOwner(thirdLoginStage);

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
        }else if(stage == thirdLoggedStage){
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

    private Stage initializeFirstLoginStyle(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-first.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-first.css").toString());
        scene.setFill(Color.TRANSPARENT);

        firstLoginController = loader.getController();
        firstLoginAlert = createAlert(firstLoginStage);
        return firstLoginStage = createStage(stage, scene, firstLoginController);
    }

    private Stage initializeSecondLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-second.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-second.css").toString());
        scene.setFill(Color.TRANSPARENT);

        secondLoginController = loader.getController();
        secondLoginAlert = createAlert(secondLoginStage);
        return secondLoginStage = createStage(stage, scene, secondLoginController);
    }

    private Stage initializeThirdLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-third.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-third.css").toString());
        scene.setFill(Color.TRANSPARENT);

        thirdLoginController = loader.getController();
        thirdLoginAlert = createAlert(thirdLoginStage);
        return thirdLoginStage= createStage(stage, scene, thirdLoginController);
    }

    private Stage initializeFirstLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-first.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/logged-first.css").toString());
        scene.setFill(Color.TRANSPARENT);

        firstLoggedController = loader.getController();
        firstLoggedAlert = createAlert(thirdLoginStage);
        return firstLoggedStage = createStage(stage, scene, firstLoggedController);
    }

    private Stage initializeSecondLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-second.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/logged-second.css").toString());
        scene.setFill(Color.TRANSPARENT);

        secondLoggedController = loader.getController();
        secondLoggedAlert = createAlert(thirdLoginStage);
        return secondLoggedStage = createStage(stage, scene, secondLoggedController);
    }

    private Stage initializeThirdLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-third.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/logged-third.css").toString());
        scene.setFill(Color.TRANSPARENT);

        thirdLoggedController = loader.getController();
        thirdLoggedAlert = createAlert(thirdLoginStage);
        return thirdLoggedStage = createStage(stage, scene, thirdLoggedController);
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
                    RestaurantApplication.loginManager.logout();
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
