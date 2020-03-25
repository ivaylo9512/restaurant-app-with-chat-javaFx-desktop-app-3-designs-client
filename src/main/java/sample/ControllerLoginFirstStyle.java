package sample;

import javafx.animation.*;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.util.Duration;

import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.stageManager;

public class ControllerLoginFirstStyle implements  Controller{
    @FXML TextField username, password, regUsername, regPassword, regRepeatPassword;
    @FXML AnchorPane contentRoot;
    @FXML Pane root, background, menu, loginFields, registerFields, nextRegisterFields, styleButtons;
    @FXML Button loginButton, registerButton, actionButton;

    private Pane currentMenu;
    private boolean loading;

    private ParallelTransition expand, reverse;
    private SequentialTransition changeTransition;

    @FXML
    public void initialize(){
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
            reverse.play();
            loginFields.setDisable(true);
            root.setCursor(Cursor.WAIT);
            loading = true;

            loginManager.login();
        }
    }

    @FXML
    public void register(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            //Todo
            loading = true;
            loginManager.register();
        }
    }

    public void setStage(){
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        background.setPrefHeight(primaryScreenBounds.getHeight());
        background.setPrefWidth(primaryScreenBounds.getWidth());

        contentRoot.setLayoutY((primaryScreenBounds.getHeight() - contentRoot.getHeight()) / 2);
        contentRoot.setLayoutX((primaryScreenBounds.getWidth() - contentRoot.getWidth()) / 2);

        loginManager.bindLoginFields(username.textProperty(), password.textProperty());
        loginManager.bindRegisterFields(regUsername.textProperty(), regPassword.textProperty(), regRepeatPassword.textProperty());
    }

    public void resetStage(){
        if(loading){
            username.setDisable(false);
            password.setDisable(false);
            root.setCursor(Cursor.DEFAULT);
            expand.play();
        }else {
            root.setCursor(Cursor.DEFAULT);
            menu.setTranslateX(0);
            contentRoot.setTranslateX(0);
            if (currentMenu != null) {
                currentMenu.setOpacity(0);
                currentMenu.setDisable(true);
                currentMenu = null;
            }
            loading = false;
            resetFields();
        }
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
        stageManager.changeStage(stageManager.thirdLoginStage);
    }
    @FXML
    public void showLoginSecondStyle(){
        stageManager.changeStage(stageManager.secondLoginStage);

    }

    @FXML
    public void minimize(){
        stageManager.currentStage.setIconified(true);
    }

    @FXML
    public void close(){
        stageManager.currentStage.close();
    }

}
