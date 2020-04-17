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

    private Controller firstLoginController;
    private Controller secondLoginController;
    private Controller thirdLoginController;

    private Controller firstLoggedController;
    private Controller secondLoggedController;
    private Controller thirdLoggedController;

    public Stage firstLoggedStage;
    public Stage secondLoggedStage;
    public Stage thirdLoggedStage;

    public Stage firstLoginStage;
    public Stage secondLoginStage;
    public Stage thirdLoginStage;

    private StageManager(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;

        initializeFirstLoginStyle(primaryStage);

        currentStage = primaryStage;
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
        }else if(stage == secondLoginStage){
            currentStage = secondLoginStage;
            currentController = secondLoginController;
        }else if(stage == thirdLoginStage){
            currentStage = thirdLoginStage;
            currentController = thirdLoginController;
        }else if(stage == firstLoggedStage){
            currentStage = firstLoggedStage;
            currentController = firstLoggedController;
        }else if(stage == secondLoggedStage){
            currentStage = secondLoggedStage;
            currentController = secondLoggedController;
        }else {
            currentStage = thirdLoggedStage;
            currentController = thirdLoggedController;
        }
        currentStage.show();
    }

    public void showAlert(String exception) {
        Alert alert = createAlert(currentStage);
        alert.getDialogPane().setContentText(exception);
        alert.showAndWait();
    }

    private void initializeFirstLoginStyle(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-first.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-first.css").toString());
        scene.setFill(Color.TRANSPARENT);

        firstLoginController = loader.getController();
        firstLoginStage = createStage(stage, scene, firstLoginController);
    }

    private void initializeSecondLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-second.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-second.css").toString());
        scene.setFill(Color.TRANSPARENT);

        secondLoginController = loader.getController();
        secondLoginStage = createStage(stage, scene, secondLoginController);
    }

    private void initializeThirdLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-third.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-third.css").toString());
        scene.setFill(Color.TRANSPARENT);

        thirdLoginController = loader.getController();
        thirdLoginStage = createStage(stage, scene, thirdLoginController);
    }

    private void initializeFirstLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-first.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/logged-first.css").toString());
        scene.setFill(Color.TRANSPARENT);

        firstLoggedController = loader.getController();
        firstLoggedStage = createStage(stage, scene, firstLoggedController);
    }

    private void initializeSecondLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-second.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/logged-second.css").toString());
        scene.setFill(Color.TRANSPARENT);

        secondLoggedController = loader.getController();
        secondLoggedStage = createStage(stage, scene, secondLoggedController);
    }

    private void initializeThirdLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-third.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/logged-third.css").toString());
        scene.setFill(Color.TRANSPARENT);

        thirdLoggedController = loader.getController();
        thirdLoggedStage = createStage(stage, scene, thirdLoggedController);
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
