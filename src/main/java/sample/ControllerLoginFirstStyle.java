package sample;

import Helpers.ServerRequests;
import Helpers.Services.LoginService;
import Helpers.Services.RegisterService;
import Models.User;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.util.Duration;
import java.net.ConnectException;
import java.util.ArrayList;

public class ControllerLoginFirstStyle implements  Controller{
    @FXML TextField username, password, regUsername, regPassword, regRepeatPassword;
    @FXML AnchorPane contentRoot;
    @FXML Pane root, background, menu, loginFields, registerFields, nextRegisterFields, styleButtons;
    @FXML Button loginButton, registerButton, actionButton;

    private Pane currentMenu;
    private LoginService loginService;
    private RegisterService registerService;

    private ParallelTransition expand, reverse;
    private SequentialTransition changeTransition;

    @FXML
    public void initialize(){
        loginService = new LoginService();
        loginService.usernameProperty().bind(username.textProperty());
        loginService.passwordProperty().bind(password.textProperty());

        registerService = new RegisterService();
        registerService.usernameProperty().bind(regUsername.textProperty());
        registerService.passwordProperty().bind(regPassword.textProperty());
        registerService.repeatPasswordProperty().bind(regRepeatPassword.textProperty());

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        background.setPrefHeight(primaryScreenBounds.getHeight());
        background.setPrefWidth(primaryScreenBounds.getWidth());

        TranslateTransition translateMenu = new TranslateTransition(Duration.millis(1300), menu);
        translateMenu.setToX(-417);
        TranslateTransition translateRoot = new TranslateTransition(Duration.millis(1300), contentRoot);
        translateRoot.setToX(209);

        TranslateTransition reverseMenu = new TranslateTransition(Duration.millis(800), menu);
        reverseMenu.setFromX(-417);
        reverseMenu.setToX(0);
        TranslateTransition reverseRoot = new TranslateTransition(Duration.millis(800), contentRoot);
        reverseRoot.setFromX(209);
        reverseRoot.setToX(0);

        reverse = new ParallelTransition(reverseMenu, reverseRoot);
        expand = new ParallelTransition(translateMenu, translateRoot);
        changeTransition = new SequentialTransition(
                new ParallelTransition(reverseMenu, reverseRoot),
                new ParallelTransition(translateMenu, translateRoot));

    }
    @FXML
    public void login(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            try {
                reverse.play();

                loginFields.setDisable(true);
                root.setCursor(Cursor.WAIT);

                loginService.start();
            } catch (IllegalStateException e) {
                System.out.println("request is executing");
            }
        }

        loginService.setOnSucceeded(eventSuccess -> changeScene(loginService));
        loginService.setOnFailed(eventFail -> updateError(loginService));
    }
    @FXML
    public void register(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            try {
                registerService.start();
            } catch (IllegalStateException e) {
                System.out.println("request is executing");
            }
        }

        registerService.setOnSucceeded(eventSuccess -> changeScene(registerService));
        registerService.setOnFailed(eventFail -> updateError(registerService));
    }
    private void updateError(Service service) {
        username.setDisable(false);
        password.setDisable(false);
        root.setCursor(Cursor.DEFAULT);

        Alert alert = LoginFirstStyle.alert;
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
        expand.play();

        if(service == loginService){
            loginFields.setDisable(false);
        }else{
            registerFields.setDisable(false);
        }
        service.reset();
    }

    private void changeScene(Service service) {
        User loggedUser = (User) service.getValue();
        ServerRequests.loggedUserProperty.set(loggedUser);
        ServerRequests.loggedUser = loggedUser;

        LoggedFirstStyle.stage.show();
        LoginFirstStyle.stage.close();

        loginFields.setOpacity(0);
        loginFields.setDisable(true);
        root.setCursor(Cursor.DEFAULT);
        currentMenu = null;

        resetFields();
        service.reset();
    }

    public void setStage(){
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        contentRoot.setLayoutY((primaryScreenBounds.getHeight() - contentRoot.getHeight()) / 2);
        contentRoot.setLayoutX((primaryScreenBounds.getWidth() - contentRoot.getWidth()) / 2 - 17.2);
    }

    public void resetStage(){
        menu.setTranslateX(0);
        contentRoot.setTranslateX(0);
        if(currentMenu != null) {
            currentMenu.setOpacity(0);
            currentMenu.setDisable(true);
            currentMenu = null;
        }
        resetFields();
    }
    private void resetFields() {
        username.setText(null);
        password.setText(null);
        regUsername.setText(null);
        regPassword.setText(null);
        regRepeatPassword.setText(null);
    }

    @FXML
    public void animateLoginFields(){
        resetFields();
        animateMenu(loginFields);
        actionButton.setOnMousePressed(this::login);
    }
    @FXML
    public void animateRegisterFields(){
        resetFields();
        animateMenu(registerFields);
        actionButton.setOnMousePressed(this::showNextRegisterFields);
    }
    @FXML
    public void animateStyleButtons(){
        resetFields();
        animateMenu(styleButtons);
    }
    private void animateMenu(Pane requestedMenu){
        if(expand.getCurrentRate() == 0 && reverse.getCurrentRate() == 0 && changeTransition.getCurrentRate() == 0) {
            if (requestedMenu.equals(currentMenu)) {
                reverse.play();
                currentMenu.setDisable(true);

                Timeline fade = new Timeline(new KeyFrame(Duration.millis(800), event1 -> {
                    currentMenu.setOpacity(0);
                    currentMenu = null;
                }));
                fade.play();

            } else if (contentRoot.getTranslateX() > 0) {
                currentMenu.setDisable(true);

                Timeline changeFields = new Timeline(new KeyFrame(Duration.millis(800), event1 -> {
                    if(!requestedMenu.equals(styleButtons)){
                        actionButton.setOpacity(1);
                        actionButton.setDisable(false);
                    }else{
                        actionButton.setOpacity(0);
                        actionButton.setDisable(true);
                    }
                    currentMenu.setOpacity(0);
                    requestedMenu.setOpacity(1);
                    requestedMenu.setDisable(false);
                    currentMenu = requestedMenu;
                }));
                changeFields.play();

                changeTransition.play();
            } else {
                expand.play();
                if(requestedMenu.equals(styleButtons)){
                    actionButton.setOpacity(0);
                    actionButton.setDisable(true);
                }else {
                    actionButton.setOpacity(1);
                    actionButton.setDisable(false);
                }

                currentMenu = requestedMenu;
                currentMenu.setOpacity(1);
                currentMenu.setDisable(false);
            }
        }

    }
    @FXML
    public void showNextRegisterFields(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            registerFields.setOpacity(0);
            registerFields.setDisable(true);

            nextRegisterFields.setOpacity(1);
            nextRegisterFields.setDisable(false);

            currentMenu = nextRegisterFields;

            actionButton.setOnMousePressed(this::register);
        }
    }
    @FXML
    public void showLoginThirdStyle(){
        LoginFirstStyle.stage.close();
        LoginThirdStyle.stage.show();
    }
    @FXML
    public void showLoginSecondStyle(){
        LoginFirstStyle.stage.close();
        LoginSecondStyle.stage.show();
    }

    @FXML
    public void minimize(){
        LoginFirstStyle.stage.setIconified(true);
    }

    @FXML
    public void close(){
        LoginFirstStyle.stage.close();
    }

}
