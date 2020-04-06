package Application;

import Helpers.RequestEnum;
import Helpers.RequestService;
import Models.User;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.scene.image.Image;
import org.apache.http.impl.client.HttpClients;
import java.io.IOException;

import static Application.RestaurantApplication.*;
import static Application.ServerRequests.httpClientLongPolling;

public class LoginManager {
    private RequestService<User> loginService = new RequestService<>(User.class, null, RequestEnum.login);
    private RequestService<User> registerService = new RequestService<>(User.class, null, RequestEnum.register);

    public RequestService<User> sendInfo = new RequestService<>(User.class, null, RequestEnum.sendUserInfo);
    private User savedUserInfo;

    User loggedUser = new User();
    public IntegerProperty userId;
    public StringProperty role;

    StringProperty username = new SimpleStringProperty();
    StringProperty password = new SimpleStringProperty();

    StringProperty regUsername = new SimpleStringProperty();
    StringProperty regPassword = new SimpleStringProperty();
    StringProperty repeatPassword = new SimpleStringProperty();

    private LoginManager(){
        userId = loggedUser.getId();
        role = loggedUser.getRole();

        loginService.setOnSucceeded(eventSuccess -> onSuccessfulService(loginService));
        loginService.setOnFailed(eventFail -> updateError(loginService));

        registerService.setOnSucceeded(eventSuccess -> onSuccessfulService(registerService));
        registerService.setOnFailed(eventFail -> updateError(loginService));

        sendInfo.setOnFailed(event -> returnOldInfo());
        sendInfo.setOnSucceeded(event -> setSavedInfo());
    }

    static LoginManager initialize(){
        return new LoginManager();
    }

    public void bindLoginFields(StringProperty username, StringProperty password){
        this.username.bind(username);
        this.password.bind(password);
    }

    public void bindRegisterFields(StringProperty username, StringProperty password, StringProperty repeat){
        regUsername.bind(username);
        regPassword.bind(password);
        repeatPassword.bind(repeat);
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
        stageManager.currentController.resetStage();
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
        savedUserInfo = new User(loggedUser);
        setUser(loggedUser);
        orderManager.setRestaurant(loggedUser.getRestaurant());

        service.reset();

        stageManager.changeToOwner();
    }

    public void logout(){
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
    public void sendUserInfo() {
        if(!savedUserInfo.equals(loggedUser)){
            sendInfo.start();
        }
    }

    private void returnOldInfo() {
        sendInfo.reset();

        loggedUser.setFirstName(savedUserInfo.getFirstName().get());
        loggedUser.setLastName(savedUserInfo.getLastName().get());
        loggedUser.setAge(Integer.valueOf(savedUserInfo.getAge().get()));
        loggedUser.setCountry(savedUserInfo.getCountry().get());
    }
    private void setSavedInfo() {
        sendInfo.reset();

        savedUserInfo = new User(loggedUser);
    }
}
