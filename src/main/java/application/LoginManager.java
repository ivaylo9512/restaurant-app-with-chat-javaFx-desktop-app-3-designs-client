package application;

import helpers.RequestEnum;
import helpers.RequestService;
import helpers.RequestTask;
import models.User;
import models.UserRequest;
import javafx.beans.property.*;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import org.apache.http.impl.client.HttpClients;
import java.io.IOException;
import java.net.URISyntaxException;

import static application.RestaurantApplication.*;
import static application.ServerRequests.*;

public class LoginManager {
    public RequestService<User> sendInfo = new RequestService<>(User.class, null, RequestEnum.sendUserInfo);
    public RequestService<UserRequest> longPollingService = new RequestService<>(UserRequest.class, null, RequestEnum.longPollingRequest);
    private User savedUserInfo;

    User loggedUser = new User();
    public IntegerProperty userId = loggedUser.getId();
    public ObjectProperty<Image> profileImage = loggedUser.getImage();
    public StringProperty role = loggedUser.getRole();

    public BooleanProperty loading = new SimpleBooleanProperty(false);
    public RequestService currentService;

    LoginManager(){
        sendInfo.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onSendUserInfoSuccess);
        sendInfo.addEventFilter(WorkerStateEvent.WORKER_STATE_FAILED, onSendUserInfoFail);

        longPollingService.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onLongPollingSuccess);
    }

    void checkIfLogged() throws URISyntaxException {
        if(userPreference.get("jwt", null) != null){
            loading.setValue(true);

            RequestTask<User> task = new RequestTask<>(User.class, ServerRequests.getLoggedUser());
            tasks.execute(task);

            task.setOnSucceeded(event -> setLoggedUser((User)event.getSource().getValue()));
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
        userRequest.getMessages().forEach(chatManager::appendMessage);

        loggedUser.setLastCheck(userRequest.getLastCheck());

        longPollingService.restart();
    };

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

    private void updateError() {
        loading.setValue(false);
        stageManager.currentController.resetStage();
    }

    public void login(){
        RequestTask<User> login = new RequestTask<>(User.class, ServerRequests.login());

        login.setOnSucceeded(eventSuccess -> onSuccessfulAuthentication(login));
        login.setOnFailed(eventFail -> updateError());

        loading.setValue(true);

        currentService = loginService;
        tasks.execute(login);
    }
    public void register(){
        loading.setValue(true);

        currentService = registerService;
        registerService.start();
    }

    private void onSuccessfulAuthentication(RequestTask task) {
        alertManager.resetLoginAlerts();

        User loggedUser = (User) task.getValue();
        setLoggedUser(loggedUser);
    }

    private void setLoggedUser(User loggedUser){
        sendInfo.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onSendUserInfoSuccess);
        sendInfo.addEventFilter(WorkerStateEvent.WORKER_STATE_FAILED, onSendUserInfoFail);

        longPollingService.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onLongPollingSuccess);

        loading.setValue(false);

        savedUserInfo = new User(loggedUser);
        setUserFields(loggedUser);
        this.loggedUser.setLastCheck(loggedUser.getLastCheck());

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

        alertManager.resetLoggedAlerts();
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
