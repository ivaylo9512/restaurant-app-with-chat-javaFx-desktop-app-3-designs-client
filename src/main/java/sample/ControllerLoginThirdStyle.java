package sample;

import Animations.TransitionResizeHeight;
import Animations.TransitionResizeWidth;
import Helpers.ServerRequests;
import Helpers.Services.LoginService;
import Helpers.Services.RegisterService;
import Models.User;
import javafx.animation.*;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.ConnectException;

public class ControllerLoginThirdStyle {
    @FXML public TextField username, password, regUsername, regPassword, regRepeatPassword;
    @FXML AnchorPane root, loginFields, registerFields, nextRegisterFields, styleButtons, contentRoot, menu;
    @FXML Button actionBtn;
    @FXML Text loginBtn;
    @FXML Line menuLine;
    @FXML StackPane logo;
    private LoginService loginService;
    private RegisterService registerService;

    private Pane currentMenu;
    private Text currentText;

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
        currentText = loginBtn;
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
    public void register(Event event) {
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

        Alert alert = LoginThirdStyle.alert;
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

        if(LoggedThirdStyle.stage != null){
            LoggedThirdStyle.stage.show();
            loginAnimation();
        }else {
            Platform.runLater(() -> {
                try {
                    LoggedThirdStyle.displayLoggedScene();
                    loginAnimation();
                } catch (Exception e) {
                    DialogPane dialog = LoginThirdStyle.alert.getDialogPane();
                    dialog.setContentText(e.getMessage());
                    LoginThirdStyle.alert.showAndWait();
                }

            });
        }
        username.setDisable(false);
        password.setDisable(false);
        resetFields();
        root.setCursor(Cursor.DEFAULT);

        service.reset();
    }

    private void loginAnimation() {
        menu.setOpacity(0);
        menuLine.setOpacity(0);
        logo.setOpacity(0);
        TransitionResizeWidth resizeWidth = new TransitionResizeWidth(Duration.millis(1200), contentRoot, 161);
        TransitionResizeHeight resizeHeight = new TransitionResizeHeight(Duration.millis(1200), contentRoot, 627);

        TranslateTransition translate = new TranslateTransition(Duration.millis(1200), contentRoot);
        translate.setToX(67.5);
        translate.setToY(-87.5);

        ParallelTransition parallelTransition = new ParallelTransition(resizeHeight,resizeWidth, translate);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(700), contentRoot);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        Timeline closeStage = new Timeline(new KeyFrame(Duration.millis(500), event -> {
            LoginThirdStyle.stage.close();
        }));

        SequentialTransition sequentialTransition = new SequentialTransition(parallelTransition, fadeOut,closeStage);
        sequentialTransition.play();
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
    public void showLoginFields(MouseEvent event){
        Text clicked = (Text) event.getSource();
        setStrikeThrough(clicked);

        resetFields();
        showMenu(loginFields);
        actionBtn.setText("login");
        actionBtn.setOnMouseClicked(this::login);
    }
    @FXML
    public void showRegisterFields(MouseEvent event){
        Text clicked = (Text) event.getSource();
        setStrikeThrough(clicked);

        resetFields();
        showMenu(registerFields);
        actionBtn.setText("next");
        actionBtn.setOnMouseClicked(this::showNextRegisterFields);

    }
    @FXML
    public void showNextRegisterFields(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            showMenu(nextRegisterFields);
            actionBtn.setText("register");
            actionBtn.setOnMouseClicked(this::register);
        }
    }
    @FXML void showStyleButtons(MouseEvent event){
        Text clicked = (Text) event.getSource();
        setStrikeThrough(clicked);

        resetFields();
        showMenu(styleButtons);
    }

    private void showMenu(Pane requestedMenu){
        if(requestedMenu == styleButtons){
            actionBtn.setOpacity(0);
            actionBtn.setDisable(true);
        }else{
            actionBtn.setOpacity(1);
            actionBtn.setDisable(false);

        }
        currentMenu.setOpacity(0);
        currentMenu.setDisable(true);

        requestedMenu.setOpacity(1);
        requestedMenu.setDisable(false);
        currentMenu = requestedMenu;
    }
    @FXML
    public void expandMenu(MouseEvent event){
        AnchorPane menu = (AnchorPane) event.getSource();
        TransitionResizeWidth resize = new TransitionResizeWidth(Duration.millis(500), menu, 165);
        resize.play();
        TranslateTransition translate = new TranslateTransition(Duration.millis(500), menu);
        translate.setToX(-120);
        translate.play();
    }

    @FXML
    public void reverseMenu(MouseEvent event){
        AnchorPane menu = (AnchorPane) event.getSource();
        TransitionResizeWidth resize = new TransitionResizeWidth(Duration.millis(500), menu, 37.3);
        resize.play();
        TranslateTransition translate = new TranslateTransition(Duration.millis(500), menu);
        translate.setToX(0);
        translate.play();
    }

    private void setStrikeThrough(Text clickedText) {
        clickedText.getStyleClass().add("strikethrough");
        if(currentText != null) {
            currentText.getStyleClass().remove("strikethrough");
        }
        currentText = clickedText;
    }

    @FXML
    public void showLoginSecondStyle(){

        LoginThirdStyle.stage.close();
        if(LoginSecondStyle.stage != null){
            LoginSecondStyle.stage.show();
        }else {
            try {
                LoginSecondStyle.displayLoginScene();
            } catch (Exception e) {
                LoginThirdStyle.stage.show();
                DialogPane dialogPane = LoginThirdStyle.alert.getDialogPane();
                dialogPane.setContentText(e.getMessage());
                LoginThirdStyle.alert.showAndWait();
            }
        }
    }

    @FXML
    public void showLoginFirstStyle(){
        LoginThirdStyle.stage.close();
        LoginFirstStyle.stage.show();
    }

    @FXML
    public void resetStage(){
        menu.setOpacity(1);
        menuLine.setOpacity(1);
        logo.setOpacity(1);

        contentRoot.setOpacity(1);
        contentRoot.setTranslateX(0);
        contentRoot.setTranslateY(0);
        contentRoot.setPrefWidth(296);
        contentRoot.setPrefHeight(452);
    }
    @FXML
    public void minimize(){
        LoginThirdStyle.stage.setIconified(true);
    }

    @FXML
    public void close(){
        LoginThirdStyle.stage.close();
    }
}
