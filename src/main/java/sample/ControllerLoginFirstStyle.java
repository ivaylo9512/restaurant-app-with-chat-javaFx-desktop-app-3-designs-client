package sample;

import Helpers.ServerRequests;
import Helpers.Services.LoginService;
import Helpers.Services.RegisterService;
import Models.User;
import javafx.animation.*;
import javafx.application.Platform;
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

public class ControllerLoginFirstStyle {
    @FXML TextField username, password, regUsername, regPassword, regRepeatPassword;
    @FXML AnchorPane contentRoot;
    @FXML Pane root, background, menu, loginFields, registerFields, nextRegisterFields;
    @FXML Button loginButton, registerButton, actionButton;

    private Pane currentFields;
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
    public void animateLoginFields(){
        animateFields(loginFields);
        actionButton.setOnMousePressed(this::login);
    }
    @FXML
    public void animateRegisterFields(){
        animateFields(registerFields);
        actionButton.setOnMousePressed(this::showNextRegisterFields);
    }
    private void animateFields(Pane requestedFields){
        if(expand.getCurrentRate() == 0 && reverse.getCurrentRate() == 0 && changeTransition.getCurrentRate() == 0) {
            if (requestedFields.equals(currentFields)) {
                reverse.play();
                currentFields.setDisable(true);

                Timeline fade = new Timeline(new KeyFrame(Duration.millis(800),
                        event1 -> currentFields.setOpacity(0)));
                fade.play();

                currentFields = null;
            } else if (contentRoot.getTranslateX() > 0) {
                currentFields.setDisable(true);

                Timeline changeFields = new Timeline(new KeyFrame(Duration.millis(800), event1 -> {
                    currentFields.setOpacity(0);
                    requestedFields.setOpacity(1);
                    requestedFields.setDisable(false);
                    currentFields = requestedFields;
                }));
                changeFields.play();

                changeTransition.play();
            } else {
                expand.play();

                currentFields = requestedFields;
                currentFields.setDisable(false);
                currentFields.setOpacity(1);
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

            currentFields = nextRegisterFields;

            actionButton.setOnMousePressed(this::register);
        }
    }
    @FXML
    public void login(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            try {
                reverse.play();

                loginFields.setDisable(true);
                username.setDisable(true);
                password.setDisable(true);
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
        ServerRequests.loggedUser = (User)service.getValue();

        if(LoggedFirstStyle.stage != null){
            LoggedFirstStyle.stage.show();
            LoginFirstStyle.stage.close();
        }else {
            Platform.runLater(() -> {
                try {
                    LoggedFirstStyle.displayLoggedScene();
                    LoginFirstStyle.stage.close();
                } catch (Exception e) {
                    DialogPane dialog = LoginFirstStyle.alert.getDialogPane();
                    dialog.setContentText(e.getMessage());
                    LoginFirstStyle.alert.showAndWait();
                }
            });
        }

        username.setDisable(false);
        password.setDisable(false);
        username.setText(null);
        password.setText(null);
        regUsername.setText(null);
        regPassword.setText(null);
        regRepeatPassword.setText(null);
        root.setCursor(Cursor.DEFAULT);

        service.reset();
    }

}
