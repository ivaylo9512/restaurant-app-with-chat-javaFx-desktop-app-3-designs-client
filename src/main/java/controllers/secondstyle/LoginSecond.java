package controllers.secondstyle;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import controllers.base.ControllerLogin;

import static application.RestaurantApplication.loginManager;
import static javafx.concurrent.Worker.State.FAILED;

public class LoginSecond extends ControllerLogin {
    @FXML Button actionBtn;
    @FXML ImageView actionBtnImage;
    @FXML Image loginImage;

    @Override
    public void initialize(){
        super.initialize();
        currentMenu = loginFields;
        fontPxProperty.addListener((observable, oldValue, newValue) -> {
            root.setStyle("-fx-font-size: " + fontPxProperty.get() + ";");
        });
    }

    public void resetStage(){
        if(loginManager.currentService != null && loginManager.currentService.getState() == FAILED){
            currentMenu.setDisable(false);
        }else {
            loginFields.setDisable(false);

            showMenu(loginFields);
            resetFields();
        }
        root.setCursor(Cursor.DEFAULT);
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
    @FXML
    void showStyleButtons(){
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
}
