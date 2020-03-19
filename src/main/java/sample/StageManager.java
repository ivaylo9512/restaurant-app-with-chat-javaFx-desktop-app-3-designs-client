package sample;

import javafx.application.Application;
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

import java.io.IOException;

public class StageManager extends Application {
    static Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    @Override
    public void start(Stage primaryStage) throws Exception {
        initializeFirstLoginStyle(primaryStage).show();
        initializeSecondLoginStyle(new Stage());
        initializeThirdLoginStyle(new Stage());

    }
    static Alert firstLoginAlert;
    static Stage firstLoginStage;
    static Alert secondLoginAlert;
    static Stage secondLoginStage;
    static Alert thirdLoginAlert;
    static Stage thirdLoginStage;
    private Stage initializeFirstLoginStyle(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login-first.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/login-first.css").toString());
        scene.setFill(Color.TRANSPARENT);

        ControllerLoginFirstStyle controller = loader.getController();
        firstLoginAlert = createAlert(firstLoginStage);
        return firstLoginStage = createStage(stage, scene, controller);
    }

    private Stage initializeSecondLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoggedSecondStyle.class.getResource("/FXML/login-second.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(LoggedSecondStyle.class.getResource("/css/login-second.css").toString());
        scene.setFill(Color.TRANSPARENT);

        ControllerLoginSecondStyle controller = loader.getController();
        secondLoginAlert = createAlert(secondLoginStage);
        return secondLoginStage = createStage(stage, scene, controller);
    }

    private Stage initializeThirdLoginStyle(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoggedSecondStyle.class.getResource("/FXML/login-third.fxml"));
        Pane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(LoggedSecondStyle.class.getResource("/css/login-third.css").toString());
        scene.setFill(Color.TRANSPARENT);

        ControllerLoginThirdStyle controller = loader.getController();
        thirdLoginAlert = createAlert(thirdLoginStage);
        return thirdLoginStage= createStage(stage, scene, controller);
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

    private static Stage createStage(Stage stage, Scene scene, Controller controller) {

        stage.showingProperty().addListener((observable, oldValue, isShowing) -> {
            if(!isShowing){
                controller.resetStage();
            }else{
                controller.setStage();
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
