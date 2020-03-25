package Application;

import Helpers.Services.LoginService;
import Helpers.Services.RegisterService;
import Models.User;
import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.http.impl.client.HttpClients;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.net.ConnectException;

import static Helpers.ServerRequests.httpClientLongPolling;

public class RestaurantApplication extends Application{
    public static LoginManager loginManager;
    public static StageManager stageManager;
    private Stage primaryStage;
    public static User loggedUser = new User();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loginManager = LoginManager.initialize();
        stageManager = StageManager.initialize(primaryStage);

        loginService.setOnSucceeded(eventSuccess -> login(loginService));
        loginService.setOnFailed(eventFail -> updateError(loginService));

        this.primaryStage = primaryStage;
    }

    private void updateError(Service service) {
        Throwable exception = service.getException();
        String exceptionMessage = exception.getMessage();

        try {
            throw exception;
        } catch (ConnectException e) {
            exceptionMessage = "No connection to the server.";
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        StageManager.currentController.resetStage();
        showAlert(exceptionMessage);
        service.reset();
    }

    public static void showAlert(String exception) {
        Alert alert = StageManager.currentAlert;
        alert.getDialogPane().setContentText(exception);
        alert.showAndWait();
    }

    public static void bindLoginFields(StringProperty username, StringProperty password){
        loginService.bind(username, password);
    }

    public static void bindRegisterFields(StringProperty username, StringProperty password, StringProperty repeatPassword){
        registerService.bind(username, password, repeatPassword);
    }

    private void login(Service service) {
        loggedUser.setUser((User) service.getValue());

        Stage currentStage = StageManager.currentStage;
        Stage stage = currentStage == primaryStage ? StageManager.firstLoggedStage
                : (Stage)currentStage.getOwner();

        StageManager.changeStage(stage);
        service.reset();
    }
    public static void logout(){
        //Todo: remove close + reset user
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        StageManager.changeStage((Stage)StageManager.currentStage.getOwner());
    }
}
