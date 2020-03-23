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
    Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
    public static Stage currentStage;
    public static Controller currentController;
    public static Alert currentAlert;

    public static Alert firstLoginAlert;
    public static Controller firstLoginController;
    public static Stage firstLoginStage;
    public static Alert secondLoginAlert;
    public static Controller secondLoginController;
    public static Stage secondLoginStage;
    public static Alert thirdLoginAlert;
    public static Controller thirdLoginController;
    public static Stage thirdLoginStage;
    public static Alert firstLoggedAlert;
    public static Controller firstLoggedController;
    public static Stage firstLoggedStage;
    public static Alert secondLoggedAlert;
    public static Controller secondLoggedController;
    public static Stage secondLoggedStage;
    public static Alert thirdLoggedAlert;
    public static Controller thirdLoggedController;
    public static Stage thirdLoggedStage;

    public void initializeStages(Stage primaryStage) throws Exception {
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
    public static void changeStage(Stage stage){
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

    private static Alert createAlert(Stage stage) {
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
                    RestaurantApplication.logout();
                    RestaurantApplication.showAlert(e.getMessage());
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
