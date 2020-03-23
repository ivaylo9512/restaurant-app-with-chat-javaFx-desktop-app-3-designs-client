package Application;

import Helpers.ServerRequests;
import Helpers.Services.LoginService;
import Helpers.Services.RegisterService;
import Models.User;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.ConnectException;

import static Helpers.ServerRequests.httpClientLongPolling;

public class RestaurantApplication extends Application{
    public static LoginService loginService;
    public static RegisterService registerService;
    private Stage primaryStage;
    public static void main(String[] args) {
        Application.launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        loginService = new LoginService();
        registerService = new RegisterService();

        StageManager stageManager = new StageManager();
        stageManager.initializeStages(primaryStage);

        loginService.setOnSucceeded(eventSuccess -> login(loginService));
        loginService.setOnFailed(eventFail -> updateError(loginService));

        this.primaryStage = primaryStage;
    }

    private void updateError(Service service) {
        Alert alert = StageManager.currentAlert;
        DialogPane dialog = alert.getDialogPane();

        try{
            dialog.setContentText(service.getException().getMessage());
            throw service.getException();
        }catch (ConnectException e){
            dialog.setContentText("No connection to the server.");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        alert.showAndWait();
        service.reset();
    }

    private void login(Service service) {
        User loggedUser = (User) service.getValue();
        ServerRequests.loggedUserProperty.set(loggedUser);
        ServerRequests.loggedUser = loggedUser;

        Stage currentStage = StageManager.currentStage;
        Stage stage = currentStage == primaryStage ? StageManager.firstLoggedStage
                : (Stage)currentStage.getOwner();

        StageManager.changeStage(stage);

        service.reset();
    }
    public static void logout(){
        //Todo
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        StageManager.changeStage((Stage)StageManager.currentStage.getOwner());
    }
}