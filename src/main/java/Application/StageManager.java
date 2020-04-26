package Application;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.effect.BoxBlur;
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
    private BoxBlur boxBlur = new BoxBlur(3, 3, 3);

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
        currentStage.getScene().getRoot().setEffect(boxBlur);

        Alert alert = createAlert(currentStage);
        alert.getDialogPane().setContentText(exception);
        alert.setOnHiding(event -> currentStage.getScene().getRoot().setEffect(null));
        alert.showAndWait();
    }

    private void initializeFirstLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-first.fxml"));
        Pane root = loader.load();

        firstLoginController = loader.getController();
        firstLoginStage = createStage(root, "/css/login-first.css", stage , firstLoginController);
    }

    private void initializeSecondLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-second.fxml"));
        Pane root = loader.load();

        secondLoginController = loader.getController();
        secondLoginStage = createStage(root, "/css/login-second.css", stage , secondLoginController);
    }

    private void initializeThirdLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-third.fxml"));
        Pane root = loader.load();

        thirdLoginController = loader.getController();
        thirdLoginStage = createStage(root, "/css/login-third.css", stage , thirdLoginController);
    }

    private void initializeFirstLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-first.fxml"));
        Pane root = loader.load();

        firstLoggedController = loader.getController();
        firstLoggedStage = createStage(root, "/css/logged-first.css", stage , firstLoggedController);
    }

    private void initializeSecondLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-second.fxml"));
        Pane root = loader.load();

        secondLoggedController = loader.getController();
        secondLoggedStage = createStage(root, "/css/logged-second.css", stage , secondLoggedController);
    }

    private void initializeThirdLoggedStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-third.fxml"));
        Pane root = loader.load();

        thirdLoggedController = loader.getController();
        thirdLoggedStage = createStage(root, "/css/logged-third.css", stage , thirdLoggedController);
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

    private Stage createStage(Parent root, String rootCss, Stage stage, Controller controller) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(rootCss).toString());
        scene.setFill(Color.TRANSPARENT);

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
