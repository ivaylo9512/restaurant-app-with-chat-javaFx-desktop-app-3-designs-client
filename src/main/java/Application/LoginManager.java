package Application;

import Models.User;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.scene.image.Image;
import org.apache.http.impl.client.HttpClients;
import java.io.IOException;
import java.net.ConnectException;

import static Application.RestaurantApplication.stageManager;
import static Application.RestaurantApplication.orderManager;
import static Helpers.ServerRequests.httpClientLongPolling;

public class LoginManager {
    private LoginService loginService = new LoginService();
    private RegisterService registerService = new RegisterService();
    private User loggedUser = new User();
    public static IntegerProperty userId = new SimpleIntegerProperty();

    private LoginManager(){
        userId.bind(loggedUser.getId());

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
        registerService.repeatPassword.bind(repeatPassword);
    }

    public void bindUserFields(StringProperty username, StringProperty firstName, StringProperty lastName,
                               StringProperty country, StringProperty role, StringProperty age, ObjectProperty<Image> image){
        loggedUser.getUsername().bindBidirectional(username);
        loggedUser.getFirstName().bindBidirectional(firstName);
        loggedUser.getLastName().bindBidirectional(lastName);
        loggedUser.getCountry().bindBidirectional(country);
        loggedUser.getAge().bindBidirectional(age);
        loggedUser.getRole().bindBidirectional(role);
        loggedUser.getImage().bindBidirectional(image);
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
        User loggedUser = (User) service.getValue();
        setUser(loggedUser);
        orderManager.setRestaurant(loggedUser.getRestaurant());

        service.reset();

        stageManager.changeToOwner();
    }

    public void logout(){
        //Todo: remove close
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        resetUser();
        orderManager.resetRestaurant();

        stageManager.changeToOwner();
    }

    public void setUser(User user) {
        loggedUser.setId(user.getId().get());
        loggedUser.setUsername(user.getUsername().get());
        loggedUser.setFirstName(user.getFirstName().get());
        loggedUser.setLastName(user.getLastName().get());
        loggedUser.setAge(Integer.valueOf(user.getAge().get()));
        loggedUser.setCountry(user.getCountry().get());
        loggedUser.setRole(user.getRole().get());
        loggedUser.setProfilePicture(user.getProfilePicture().get());
    }

    public void resetUser(){
        loggedUser.setId(null);
        loggedUser.setUsername(null);
        loggedUser.setUsername(null);
        loggedUser.setLastName(null);
        loggedUser.setAge(null);
        loggedUser.setCountry(null);
        loggedUser.setRole(null);
        loggedUser.setProfilePicture(null);
    }

    public boolean isUserEdited(User oldInfo) {
        return oldInfo.equals(loggedUser);
    }
}
