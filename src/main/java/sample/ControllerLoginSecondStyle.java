package sample;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import sample.base.ControllerLogin;

public class ControllerLoginSecondStyle extends ControllerLogin {
    @FXML Button actionBtn;
    @FXML ImageView actionBtnImage;
    @FXML Image loginImage;

    @FXML
    public void initialize(){
        currentMenu = loginFields;
    }

    public void resetStage(){
        if(loading){
            username.setDisable(false);
            password.setDisable(false);
            regUsername.setDisable(false);
            regPassword.setDisable(false);
            regRepeatPassword.setDisable(false);
            root.setCursor(Cursor.DEFAULT);
        }else {
            loginFields.setDisable(false);
            root.setCursor(Cursor.DEFAULT);

            showMenu(loginFields);

            username.setDisable(false);
            password.setDisable(false);
            regUsername.setDisable(false);
            regPassword.setDisable(false);
            regRepeatPassword.setDisable(false);

            loading = false;
            resetFields();
        }
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

    @Override
    protected void disableFields(boolean login) {
        if(login){
            username.setDisable(true);
            password.setDisable(true);
        }else{
            regUsername.setDisable(true);
            regPassword.setDisable(true);
            regRepeatPassword.setDisable(true);
        }
    }
}
