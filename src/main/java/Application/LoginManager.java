package Application;

import Models.Restaurant;
import Models.User;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import org.apache.http.impl.client.HttpClients;
import java.io.IOException;
import java.net.ConnectException;

import static Application.RestaurantApplication.stageManager;
import static Helpers.ServerRequests.httpClientLongPolling;

public class LoginManager {
    private LoginService loginService = new LoginService();
    private RegisterService registerService = new RegisterService();
    public User loggedUser = new User();

    private LoginManager(){
        loginService.setOnSucceeded(eventSuccess -> onSuccessfulService(loginService));
        loginService.setOnFailed(eventFail -> updateError(loginService));

        registerService.setOnSucceeded(eventSuccess -> onSuccessfulService(registerService));
        registerService.setOnFailed(eventFail -> updateError(loginService));
    }
    static LoginManager initialize(){
        return new LoginManager();
    }

    public void bindLoginFields(StringProperty username, StringProperty password){
        loginService.username.bind(username);
        loginService.password.bind(password);
    }

    public void bindRegisterFields(StringProperty username, StringProperty password, StringProperty repeatPassword){
        registerService.username.bind(username);
        registerService.password.bind(password);
        registerService.repeatPassword.bind(password);
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

        stageManager.currentController.resetStage();
        stageManager.showAlert(exceptionMessage);
        service.reset();
    }

    public void login(){
        loginService.start();
    }
    public void register(){
        registerService.start();
    }
    private void onSuccessfulService(Service service) {
        loggedUser.setUser((User) service.getValue());
        service.reset();

        stageManager.changeToOwner();
    }

    public void logout(){
        //Todo: remove close + reset user
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        stageManager.changeToOwner();
    }
}
