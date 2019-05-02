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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.net.ConnectException;

public class ControllerLoginSecondStyle {
    @FXML TextField username, password, regUsername, regPassword, regRepeatPassword;
    @FXML AnchorPane root, loginFields, registerFields, nextRegisterFields, styleButtons;
    @FXML Button actionBtn;
    @FXML ImageView actionBtnImage;
    @FXML Image loginImage;
    private LoginService loginService;
    private RegisterService registerService;

    private Pane currentMenu;

    @FXML
    public void initialize(){
        loginService = new LoginService();
        loginService.usernameProperty().bind(username.textProperty());
        loginService.passwordProperty().bind(password.textProperty());

        registerService = new RegisterService();
        registerService.usernameProperty().bind(regUsername.textProperty());
        registerService.passwordProperty().bind(regPassword.textProperty());
        registerService.repeatPasswordProperty().bind(regRepeatPassword.textProperty());

        currentMenu = loginFields;
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

        if(LoggedSecondStyle.stage != null){
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
        loginFields.setDisable(false);
        resetFields();
        root.setCursor(Cursor.DEFAULT);

        service.reset();
    }

    public void resetStage(){
        showMenu(loginFields);

        username.setDisable(false);
        password.setDisable(false);
        regUsername.setDisable(false);
        regPassword.setDisable(false);
        regRepeatPassword.setDisable(false);

        resetFields();
    }
    private void resetFields() {
        username.setText(null);
        password.setText(null);
        regUsername.setDisable(false);
        regPassword.setDisable(false);
        regRepeatPassword.setDisable(false);
        regUsername.setText(null);
        regPassword.setText(null);
        regRepeatPassword.setText(null);
    }

    @FXML
    public void showLoginFields(){
        resetFields();
        showMenu(loginFields);
        actionBtn.setOnMouseClicked(this::login);
    }
    @FXML
    public void showRegisterFields(){
        resetFields();
        showMenu(registerFields);
        actionBtn.setOnMouseClicked(this::showNextRegisterFields);

    }
    @FXML
    public void showNextRegisterFields(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            showMenu(nextRegisterFields);
            actionBtn.setOnMouseClicked(this::register);
        }
    }
    @FXML void showStyleButtons(){
        showMenu(styleButtons);
        actionBtn.setOnMouseClicked(event -> close());

    }

    private void showMenu(Pane requestedMenu){
        if(currentMenu != null){
            currentMenu.setOpacity(0);
            currentMenu.setDisable(true);
        }
        if(requestedMenu.equals(styleButtons)){
            actionBtnImage.setImage(null);
            actionBtn.setText("X");
        }else{
            actionBtnImage.setImage(loginImage);
            actionBtn.setText(null);
        }
        requestedMenu.setOpacity(1);
        requestedMenu.setDisable(false);
        currentMenu = requestedMenu;
    }

    @FXML
    public void showLoginThirdStyle(){

        LoginSecondStyle.stage.close();
        if(LoginThirdStyle.stage != null){
            LoginThirdStyle.stage.show();
        }else {
            try {
                LoginThirdStyle.displayLoginScene();
            } catch (Exception e) {
                LoginSecondStyle.stage.show();
                DialogPane dialogPane = LoginSecondStyle.alert.getDialogPane();
                dialogPane.setContentText(e.getMessage());
                LoginSecondStyle.alert.showAndWait();
            }
        }
    }
    @FXML
    public void showLoginFirstStyle(){
        LoginSecondStyle.stage.close();
        LoginFirstStyle.stage.show();
    }

    @FXML
    public void close(){
        LoginSecondStyle.stage.close();
    }
}
