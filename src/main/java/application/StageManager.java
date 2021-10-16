package application;

import controllers.base.ControllerAdjustable;
import controllers.base.ControllerAlert;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import controllers.base.Controller;

import static application.RestaurantApplication.alertManager;
import static application.RestaurantApplication.loginManager;

public class StageManager {
    private final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    public ControllerAlert currentAlertController, firstLoginAlertController, firstLoggedAlertController, secondLoggedAlertController, secondLoginAlertController;
    public ControllerAdjustable currentController, firstLoggedController, secondLoggedController, secondLoggedMenuController,
            thirdLoggedController, firstLoginController, secondLoginController, thirdLoginController;
    public Stage currentStage, currentStageMenu, currentAlertStage, firstLoggedStage, secondLoggedStage, secondLoggedMenuStage, thirdLoggedStage,
            firstLoginStage, secondLoginStage, thirdLoginStage, firstLoginAlert, firstLoggedAlert, secondLoginAlert, secondLoggedAlert, thirdLoginAlert, thirdLoggedAlert;

    void initializeStages(Stage primaryStage) throws Exception{
        firstLoginStage = primaryStage;
        firstLoginController = createAdjustableStage("login-first", firstLoginStage);

        secondLoginStage = new Stage();
        secondLoginController = createAdjustableStage("login-second", secondLoginStage);

//        thirdLoginStage = new Stage();
//        thirdLoginController = createStage("login-third", thirdLoginStage);

        firstLoggedStage = new Stage();
        firstLoggedController = createAdjustableStage("logged-first", firstLoggedStage);

        secondLoggedStage = new Stage();
        secondLoggedController = createAdjustableStage("logged-second", secondLoggedStage);

        secondLoggedMenuStage = new Stage();
        secondLoggedMenuStage.initOwner(createTransparentUtilityStage());
        secondLoggedMenuController = createAdjustableStage("logged-second-menu", secondLoggedMenuStage);

//        thirdLoggedStage = new Stage();
//        thirdLoggedController = createStage("logged-third", thirdLoggedStage);


        firstLoginAlert = new Stage();
        firstLoginAlertController = createAlertStage(firstLoginAlert, firstLoginStage, true, "first-alert");
        firstLoggedAlert = new Stage();
        firstLoggedAlertController = createAlertStage(firstLoggedAlert, firstLoggedStage, false, "first-alert");
        secondLoginAlert = new Stage();
        secondLoginAlertController = createAlertStage(secondLoginAlert, secondLoginStage, true, "second-alert");
        secondLoggedAlert = new Stage();
        secondLoggedAlertController = createAlertStage(secondLoggedAlert, secondLoggedMenuStage, false, "second-alert");


        secondLoggedMenuStage.setAlwaysOnTop(true);

        firstLoginStage.setUserData(firstLoggedStage);
        firstLoggedStage.setUserData(firstLoginStage);
        secondLoginStage.setUserData(secondLoggedStage);
        secondLoggedStage.setUserData(secondLoginStage);
//        thirdLoginStage.setUserData(thirdLoggedStage);
//        thirdLoggedStage.setUserData(thirdLoginStage);

        changeStage(primaryStage);
    }

    void changeToOwner(){
        changeStage((Stage)currentStage.getUserData());
    }

    public void changeStage(Stage stage){
        closeCurrentStage();

        if(stage == firstLoginStage){
            currentStage = firstLoginStage;
            currentController = firstLoginController;
            currentAlertStage = firstLoginAlert;
            currentAlertController = secondLoginAlertController;
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
            currentAlertStage = firstLoggedAlert;
            currentAlertController = firstLoggedAlertController;
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
        if(currentStageMenu != null){
            ((Stage)currentStageMenu.getOwner()).show();
            currentStageMenu.show();
        }else{
            currentStage.show();
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

    private ControllerAlert createAlertStage(Stage stage, Stage owner, boolean isLoginStage, String fileName)throws  Exception{
        stage.initOwner(owner);
        stage.setY(0);

        ControllerAlert controller = (ControllerAlert) createStage(fileName, stage);

        ObjectProperty<String> alertValue = alertManager.currentLoggedAlert;
        SimpleListProperty<String> alerts = alertManager.loggedAlerts;
        if(isLoginStage){
            alertValue = alertManager.currentLoginAlert;
            alerts = alertManager.loginAlerts;
        }

        controller.currentAlert = alertValue;
        controller.alerts = alerts;
        controller.bind();

        return controller;
    }

    private ControllerAdjustable createAdjustableStage(String fileName, Stage stage) throws Exception{
        ControllerAdjustable controller = (ControllerAdjustable) createStage(fileName, stage);
        stage.showingProperty().addListener((observable, oldValue, isShowing) -> {
            if(!isShowing){
                controller.resetStage();
            }else{
                try {
                    controller.adjustStage(primaryScreenBounds.getHeight(), primaryScreenBounds.getWidth());
                } catch (Exception e) {
                    e.printStackTrace();
                    loginManager.logout();
                    alertManager.addLoginAlert(e.getMessage());
                }
            }
        });
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
        controller.setStage(stage);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/" + fileName + ".css").toString());
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);

        return controller;
    }
}
