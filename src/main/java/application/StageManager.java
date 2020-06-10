package application;

import controllers.base.ControllerAlert;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import controllers.base.Controller;


import java.io.IOException;

import static application.RestaurantApplication.alertManager;
import static application.RestaurantApplication.loginManager;

public class StageManager {
    private Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    public ControllerAlert currentAlertController, secondLoggedAlertController, secondLoginAlertController;
    public Controller currentController, firstLoggedController, secondLoggedController, secondLoggedMenuController,
            thirdLoggedController, firstLoginController, secondLoginController, thirdLoginController;
    public Stage currentStage, currentStageMenu, currentAlertStage, firstLoggedStage, secondLoggedStage, secondLoggedMenuStage, thirdLoggedStage,
            firstLoginStage, secondLoginStage, thirdLoginStage, firstLoginAlert, firstLoggedAlert, secondLoginAlert, secondLoggedAlert, thirdLoginAlert, thirdLoggedAlert;

    StageManager(Stage primaryStage) throws Exception{
        firstLoginStage = primaryStage;
        firstLoginController = createStage("login-first", firstLoginStage);

        secondLoginStage = new Stage();
        secondLoginController = createStage("login-second", secondLoginStage);

        thirdLoginStage = new Stage();
        thirdLoginController = createStage("login-third", thirdLoginStage);

        firstLoggedStage = new Stage();
        firstLoggedController = createStage("logged-first", firstLoginStage);

        secondLoggedStage = new Stage();
        secondLoggedController = createStage("logged-second", secondLoggedStage);
        
        secondLoggedMenuStage = new Stage();
        secondLoggedMenuStage.initOwner(createTransparentUtilityStage());
        secondLoggedMenuController = createStage("logged-second-menu", secondLoggedMenuStage);

        thirdLoggedStage = new Stage();
        thirdLoggedController = createStage("logged-third", thirdLoggedStage);

//        createAlertStage(new Stage(), firstLoginStage, true);
//        createAlertStage(new Stage(), secondLoginStage, false);
//        createAlertStage(new Stage(), thirdLoginStage, true);
        secondLoginAlert = new Stage();
        secondLoginAlertController = createAlertStage(secondLoginAlert, secondLoginStage, true, "second-alert");
        secondLoggedAlert = new Stage();
        secondLoggedAlertController = createAlertStage(secondLoggedAlert, secondLoggedStage, false, "second-alert");
//        createAlertStage(new Stage(), thirdLoggedStage, false);


        secondLoggedMenuStage.setAlwaysOnTop(true);

        firstLoginStage.setUserData(firstLoggedStage);
        firstLoggedStage.setUserData(firstLoginStage);
        secondLoginStage.setUserData(secondLoggedStage);
        secondLoggedStage.setUserData(secondLoginStage);
//        thirdLoginStage.setUserData(thirdLoggedStage);
//        thirdLoggedStage.setUserData(thirdLoginStage);

        changeStage(primaryStage);
    }

    static StageManager initialize(Stage primaryStage) throws Exception {
        return new StageManager(primaryStage);
    }

    void changeToOwner(){
        changeStage((Stage)currentStage.getUserData());
    }

    public void changeStage(Stage stage){
        closeCurrentStage();

        if(stage == firstLoginStage){
            currentStage = firstLoginStage;
            currentController = firstLoginController;
        }else if(stage == secondLoginStage){
            currentStage = secondLoginStage;
            currentController = secondLoginController;
            currentAlertStage = secondLoginAlert;
            currentAlertController = secondLoginAlertController;
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
            currentAlertStage = secondLoggedAlert;
            currentAlertController = secondLoggedAlertController;
        }else {
            currentStage = thirdLoggedStage;
            currentController = thirdLoggedController;
        }
        currentStage.show();
        if(currentStageMenu != null){
            ((Stage)currentStageMenu.getOwner()).show();
            currentStageMenu.show();
        }

        if(currentAlertStage != null && currentAlertStage.getUserData() != null && currentAlertStage.getUserData().equals("active")){
            currentAlertStage.show();
            currentAlertController.fadeInAlert();
        }
    }

    private void closeCurrentStage() {
        if(currentStage != null){
            currentStage.close();
            if(currentStageMenu != null) ((Stage)currentStageMenu.getOwner()).close();
            if(currentAlertStage != null)currentAlertStage.close();
            currentStageMenu = null;
            currentAlertStage = null;
        }
    }

    private ControllerAlert createAlertStage(Stage stage, Stage owner, boolean isLoginStage, String fileName)throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/" + fileName + ".fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/" + fileName + ".css").toString());
        scene.setFill(Color.TRANSPARENT);

        stage.initOwner(owner);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setY(0);

        ObjectProperty<String> alertValue = alertManager.currentLoggedAlert;
        SimpleListProperty<String> alerts = alertManager.loggedAlerts;
        if(isLoginStage){
            alertValue = alertManager.currentLoginAlert;
            alerts = alertManager.loginAlerts;
        }

        ControllerAlert controller = loader.getController();
        controller.currentAlert = alertValue;
        controller.alerts = alerts;
        controller.stage = stage;
        controller.bind();

        return controller;
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

    private Controller createStage(String fileName, Stage stage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/" + fileName + ".fxml"));
        Pane root = loader.load();

        Controller controller = loader.getController();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("css/" + fileName + ".css").toString());
        scene.setFill(Color.TRANSPARENT);

        stage.showingProperty().addListener((observable, oldValue, isShowing) -> {
            if(!isShowing){
                controller.resetStage();
            }else{
                try {
                    controller.setStage();
                } catch (Exception e) {
                    loginManager.logout();
                    alertManager.addLoginAlert(e.getMessage());
                }
            }
        });
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);

        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());

        return controller;
    }
}
