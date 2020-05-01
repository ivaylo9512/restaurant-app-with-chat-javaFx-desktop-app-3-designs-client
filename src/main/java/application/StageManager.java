package application;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import controllers.base.Controller;


import java.io.IOException;

import static application.RestaurantApplication.loginManager;

public class StageManager {
    private Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
    private BoxBlur boxBlur = new BoxBlur(3, 3, 3);

    private Stage primaryStage;

    public Controller currentController, firstLoggedController, secondLoggedController, secondLoggedMenuController,
            thirdLoggedController, firstLoginController, secondLoginController, thirdLoginController;
    public Stage currentStage, currentStageMenu, firstLoggedStage, secondLoggedStage, secondLoggedMenuStage, thirdLoggedStage,
            firstLoginStage, secondLoginStage, thirdLoginStage;

    void initializeStages(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;

        initializeFirstLoginStyle(primaryStage);

        currentStage = primaryStage;
        currentController = firstLoginController;

        initializeSecondLoginStyle(new Stage());
//        initializeThirdLoginStyle(new Stage());

        initializeFirstLoggedStyle(new Stage());
        initializeSecondLoggedStyle(new Stage());
        initializeSecondLoggedMenuStyle(new Stage());
        initializeThirdLoggedStyle(new Stage());

        secondLoggedMenuStage.setAlwaysOnTop(true);

        firstLoginStage.setUserData(firstLoggedStage);
        firstLoggedStage.setUserData(firstLoginStage);
        secondLoginStage.setUserData(secondLoggedStage);
        secondLoggedStage.setUserData(secondLoginStage);
        thirdLoginStage.setUserData(thirdLoggedStage);
        thirdLoggedStage.setUserData(thirdLoginStage);

        currentStage.show();
        if(currentStageMenu != null) {
            ((Stage)currentStageMenu.getOwner()).show();
            currentStageMenu.show();
        }
    }
    static StageManager initialize() throws Exception {
        return new StageManager();
    }

    void changeToOwner(){
        changeStage((Stage)currentStage.getUserData());
    }

    public void changeStage(Stage stage){
        currentStage.close();
        if(currentStageMenu != null) ((Stage)currentStageMenu.getOwner()).close();
        currentStageMenu = null;

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
            currentStageMenu = secondLoggedMenuStage;
        }else {
            currentStage = thirdLoggedStage;
            currentController = thirdLoggedController;
        }
        currentStage.show();
        if(currentStageMenu != null){
            ((Stage)currentStageMenu.getOwner()).show();
            currentStageMenu.show();
        }
    }

    public void showAlert(String exception) {
        currentStage.getScene().getRoot().setEffect(boxBlur);

        Alert alert = createAlert(currentStage);
        alert.getDialogPane().setContentText(exception);
        alert.setOnHiding(event -> currentStage.getScene().getRoot().setEffect(null));
        alert.showAndWait();
    }

    private void initializeFirstLoginStyle(Stage stage) throws IOException {
        firstLoginStage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-first.fxml"));
        Pane root = loader.load();

        firstLoginController = loader.getController();
        createStage(root, "/css/login-first.css", stage , firstLoginController);
    }

    private void initializeSecondLoginStyle(Stage stage) throws IOException {
        secondLoginStage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-second.fxml"));
        Pane root = loader.load();

        secondLoginController = loader.getController();
        createStage(root, "/css/login-second.css", stage , secondLoginController);
    }

    private void initializeThirdLoginStyle(Stage stage) throws IOException {
        thirdLoginStage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-third.fxml"));
        Pane root = loader.load();

        thirdLoginController = loader.getController();
        createStage(root, "/css/login-third.css", stage , thirdLoginController);
    }

    private void initializeFirstLoggedStyle(Stage stage) throws IOException {
        firstLoggedStage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-first.fxml"));
        Pane root = loader.load();

        firstLoggedController = loader.getController();
        createStage(root, "/css/logged-first.css", stage , firstLoggedController);
    }

    private void initializeSecondLoggedStyle(Stage stage) throws IOException {
        secondLoggedStage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-second.fxml"));
        Pane root = loader.load();

        secondLoggedController = loader.getController();
        createStage(root, "/css/logged-second.css", stage , secondLoggedController);
    }

    private void initializeSecondLoggedMenuStyle(Stage stage) throws IOException {
        secondLoggedMenuStage = stage;
        stage.initOwner(createTransparentUtilityStage());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-second-menu.fxml"));
        Pane root = loader.load();

        secondLoggedMenuController = loader.getController();
        createStage(root, "/css/logged-second-menu.css", stage , secondLoggedMenuController);
    }

    private void initializeThirdLoggedStyle(Stage stage) throws IOException {
        thirdLoggedStage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/logged-third.fxml"));
        Pane root = loader.load();

        thirdLoggedController = loader.getController();
        createStage(root, "/css/logged-third.css", stage , thirdLoggedController);
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

    private Stage createTransparentUtilityStage() {
        Stage utilityStage = new Stage();
        utilityStage.initStyle(StageStyle.UTILITY);
        utilityStage.setOpacity(0);

        utilityStage.setHeight(0);
        utilityStage.setWidth(0);
        utilityStage.setY(-primaryScreenBounds.getMaxY());

        return utilityStage;
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
