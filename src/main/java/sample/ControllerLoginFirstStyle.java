package sample;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import sample.base.ControllerLogin;

import static Application.RestaurantApplication.loginManager;
import static javafx.concurrent.Worker.State.FAILED;

public class ControllerLoginFirstStyle extends ControllerLogin {
    @FXML Pane background, menu;
    @FXML Button loginButton, registerButton, actionButton;

    private ParallelTransition expand, reverse;
    private SequentialTransition changeTransition;

    @Override
    public void initialize(){
        super.initialize();

        TranslateTransition translateMenu = new TranslateTransition(Duration.millis(1300), menu);
        translateMenu.setToX(-417);
        TranslateTransition translateRoot = new TranslateTransition(Duration.millis(1300), loginPane);
        translateRoot.setToX(209);

        TranslateTransition reverseMenu = new TranslateTransition(Duration.millis(800), menu);
        reverseMenu.setFromX(-417);
        reverseMenu.setToX(0);
        TranslateTransition reverseRoot = new TranslateTransition(Duration.millis(800), loginPane);
        reverseRoot.setFromX(209);
        reverseRoot.setToX(0);

        reverse = new ParallelTransition(reverseMenu, reverseRoot);
        expand = new ParallelTransition(translateMenu, translateRoot);
        changeTransition = new SequentialTransition(
                new ParallelTransition(reverseMenu, reverseRoot),
                new ParallelTransition(translateMenu, translateRoot));
    }

    @Override
    public void setStage() {
        super.setStage();

        background.setPrefHeight(primaryScreenBounds.getHeight());
        background.setPrefWidth(primaryScreenBounds.getWidth());
    }

    public void resetStage(){
        if(loginManager.currentService != null && loginManager.currentService.getState() == FAILED){
            currentMenu.setDisable(false);
            expand.play();
        }else {
            menu.setTranslateX(0);
            loginPane.setTranslateX(0);
            if (currentMenu != null) {
                currentMenu.setOpacity(0);
                currentMenu.setDisable(true);
                currentMenu = null;
            }
            resetFields();
        }
        root.setCursor(Cursor.DEFAULT);
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

            } else if (loginPane.getTranslateX() > 0) {
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

    @Override
    protected void disableFields(boolean login) {
        super.disableFields(login);
        reverse.play();
    }

}
