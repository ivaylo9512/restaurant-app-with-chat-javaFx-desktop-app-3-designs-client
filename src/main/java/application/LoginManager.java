package application;

import helpers.RequestEnum;
import helpers.RequestService;
import helpers.RequestTask;
import models.User;
import models.UserRequest;
import com.fasterxml.jackson.databind.JavaType;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

import static application.RestaurantApplication.*;
import static application.ServerRequests.*;

public class LoginManager {
    private RequestService<User> loginService = new RequestService<>(User.class, null, RequestEnum.login);
    private RequestService<User> registerService = new RequestService<>(User.class, null, RequestEnum.register);

    public RequestService<User> sendInfo = new RequestService<>(User.class, null, RequestEnum.sendUserInfo);
    public RequestService<UserRequest> longPollingService = new RequestService<>(UserRequest.class, null, RequestEnum.longPollingRequest);
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

        sendInfo.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onSendUserInfoSuccess);
        sendInfo.addEventFilter(WorkerStateEvent.WORKER_STATE_FAILED, onSendUserInfoFail);

        longPollingService.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onLongPollingSuccess);
    }

    private JavaType type = mapper.constructType(User.class);
    void checkIfLogged() {
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
    private EventHandler<WorkerStateEvent> onSendUserInfoSuccess = e -> setSavedInfo();
    private EventHandler<WorkerStateEvent> onSendUserInfoFail = e -> returnOldInfo();

    private EventHandler<WorkerStateEvent> onLongPollingSuccess = e -> {
        UserRequest userRequest = longPollingService.getValue();
        userRequest.getDishes().forEach(orderManager::updateDish);
        userRequest.getOrders().forEach(orderManager::addOrder);

        longPollingService.restart();
    };

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
        sendInfo.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onSendUserInfoSuccess);
        sendInfo.addEventFilter(WorkerStateEvent.WORKER_STATE_FAILED, onSendUserInfoFail);

        longPollingService.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onLongPollingSuccess);

        loading.setValue(false);

        savedUserInfo = new User(loggedUser);
        setUserFields(loggedUser);

        chatManager.setChats(loggedUser.getChats());
        orderManager.setRestaurant(loggedUser.getRestaurant());

        longPollingService.start();
        stageManager.changeToOwner();
    }
    public void logout(){
        sendInfo.removeEventFilter(WorkerStateEvent.WORKER_STATE_FAILED, onSendUserInfoFail);
        sendInfo.removeEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onSendUserInfoSuccess);
        longPollingService.removeEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onLongPollingSuccess);

        if(sendInfo.isRunning()) sendInfo.cancel();
        if(longPollingService.isRunning()) longPollingService.cancel();

        longPollingService.reset();
        sendInfo.reset();

        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClient = HttpClients.createDefault();

        userPreference.remove("jwt");
        resetUser();

        chatManager.resetChats();
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
