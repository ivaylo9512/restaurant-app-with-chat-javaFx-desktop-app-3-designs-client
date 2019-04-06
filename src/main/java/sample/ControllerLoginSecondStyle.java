package sample;

import Helpers.ServerRequests;
import Helpers.Services.LoginService;
import Helpers.Services.RegisterService;
import Models.User;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.net.ConnectException;

public class ControllerLoginSecondStyle {
    @FXML TextField username, password, regUsername, regPassword, regRepeatPassword;
    @FXML AnchorPane root, loginFields, registerFields, nextRegisterFields;
    @FXML Button actionBtn;
    private LoginService loginService;
    private RegisterService registerService;

    @FXML
    public void initialize(){
        loginService = new LoginService();
        loginService.usernameProperty().bind(username.textProperty());
        loginService.passwordProperty().bind(password.textProperty());

        registerService = new RegisterService();
        registerService.usernameProperty().bind(regUsername.textProperty());
        registerService.passwordProperty().bind(regPassword.textProperty());
        registerService.repeatPasswordProperty().bind(regRepeatPassword.textProperty());
    }

    @FXML
    public void login(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            try {
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
                regUsername.setDisable(true);
                regPassword.setDisable(true);
                regRepeatPassword.setDisable(true);
                root.setCursor(Cursor.WAIT);

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
        regUsername.setDisable(false);
        regPassword.setDisable(false);
        regRepeatPassword.setDisable(false);
        root.setCursor(Cursor.DEFAULT);

        Alert alert = LoginSecondStyle.alert;
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

    private void changeScene(Service service) {
        User loggedUser = (User) service.getValue();
        ServerRequests.loggedUserProperty.set(loggedUser);
        ServerRequests.loggedUser = (User)service.getValue();

        if(LoggedFirstStyle.stage != null){
            LoggedSecondStyle.stage.show();
            LoginSecondStyle.stage.close();
        }else {
            Platform.runLater(() -> {
                try {
                    LoggedSecondStyle.displayLoggedScene();
                    LoginSecondStyle.stage.close();
                } catch (Exception e) {
                    DialogPane dialog = LoginSecondStyle.alert.getDialogPane();
                    dialog.setContentText(e.getMessage());
                    LoginSecondStyle.alert.showAndWait();
                }
            });
        }
        username.setDisable(false);
        password.setDisable(false);
        username.setText(null);
        password.setText(null);
        regUsername.setDisable(false);
        regPassword.setDisable(false);
        regRepeatPassword.setDisable(false);
        regUsername.setText(null);
        regPassword.setText(null);
        regRepeatPassword.setText(null);
        root.setCursor(Cursor.DEFAULT);

        service.reset();
    }

    @FXML
    public void showLoginFields(){
        registerFields.setOpacity(0);
        registerFields.setDisable(true);

        nextRegisterFields.setOpacity(0);
        nextRegisterFields.setDisable(true);

        loginFields.setDisable(false);
        loginFields.setOpacity(1);

        actionBtn.setOnMouseClicked(this::login);
    }
    @FXML
    public void showRegisterFields(){
        loginFields.setOpacity(0);
        loginFields.setDisable(true);

        nextRegisterFields.setOpacity(0);
        nextRegisterFields.setDisable(true);

        registerFields.setDisable(false);
        registerFields.setOpacity(1);
        actionBtn.setOnMouseClicked(this::showNextRegisterFields);

    }
    @FXML
    public void showNextRegisterFields(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            registerFields.setOpacity(0);
            registerFields.setDisable(true);

            nextRegisterFields.setOpacity(1);
            nextRegisterFields.setDisable(false);

            actionBtn.setOnMouseClicked(this::register);
        }
    }
}
