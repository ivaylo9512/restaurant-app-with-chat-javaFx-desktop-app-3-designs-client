package Application;

import Helpers.RequestEnum;
import Helpers.RequestService;
import Helpers.RequestTask;
import Models.User;
import com.fasterxml.jackson.databind.JavaType;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.scene.image.Image;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;

import static Application.RestaurantApplication.*;
import static Application.ServerRequests.*;

public class LoginManager {
    private RequestService<User> loginService = new RequestService<>(User.class, null, RequestEnum.login);
    private RequestService<User> registerService = new RequestService<>(User.class, null, RequestEnum.register);

    public RequestService<User> sendInfo = new RequestService<>(User.class, null, RequestEnum.sendUserInfo);
    private User savedUserInfo;

    User loggedUser = new User();
    public IntegerProperty userId;
    public StringProperty role;
    public BooleanProperty loading = new SimpleBooleanProperty(false);
    public RequestService currentService;

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
        registerService.setOnFailed(eventFail -> updateError(registerService));

        sendInfo.setOnFailed(event -> returnOldInfo());
        sendInfo.setOnSucceeded(event -> setSavedInfo());
    }

    JavaType type = mapper.constructType(User.class);
    public void checkIfLogged() {
        if(userPreference.get("jwt", null) != null){
            loading.setValue(true);

            HttpRequestBase request = ServerRequests.getLoggedUser();
            RequestTask<User> task = new RequestTask<>(type, request);
            tasks.execute(task);

            task.setOnSucceeded(event -> {
                setLoggedUser((User)event.getSource().getValue());
            });
            task.setOnFailed(event -> {
                userPreference.remove("jwt");
                loading.setValue(false);
            });
        }
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
        loading.setValue(false);
        stageManager.currentController.resetStage();
        service.reset();
    }

    public void login(){
        loading.setValue(true);

        currentService = loginService;
        loginService.start();
    }
    public void register(){
        loading.setValue(true);

        currentService = registerService;
        registerService.start();
    }

    private void onSuccessfulService(Service service) {
        User loggedUser = (User) service.getValue();
        setLoggedUser(loggedUser);

        service.reset();
    }

    private void setLoggedUser(User loggedUser){
        loading.setValue(false);

        savedUserInfo = new User(loggedUser);
        setUserFields(loggedUser);
        orderManager.setRestaurant(loggedUser.getRestaurant());

        stageManager.changeToOwner();
    }
    public void logout(){
        httpClientLongPolling = HttpClients.createDefault();

        userPreference.remove("jwt");
        resetUser();
        orderManager.resetRestaurant();

        stageManager.changeToOwner();
    }

    public void setUserFields(User user) {
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
