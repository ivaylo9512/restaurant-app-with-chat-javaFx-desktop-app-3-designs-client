package Application;

import LoginService;
import RegisterService;
import Models.User;
import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.ConnectException;

import static Helpers.ServerRequests.httpClientLongPolling;

public class RestaurantApplication extends Application{
    public static LoginManager loginManager;
    public static StageManager stageManager;
    private Stage primaryStage;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loginManager = LoginManager.initialize();
        stageManager = StageManager.initialize(primaryStage);

        this.primaryStage = primaryStage;
    }

    public static void showAlert(String exception) {
        Alert alert = StageManager.currentAlert;
        alert.getDialogPane().setContentText(exception);
        alert.showAndWait();
    }
}
