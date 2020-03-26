package sample.base;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.stageManager;

public abstract class ControllerLogin {
    @FXML
    protected TextField username, password, regUsername, regPassword, regRepeatPassword;

    @FXML
    protected AnchorPane root, loginFields, registerFields, nextRegisterFields, styleButtons;

    protected Pane currentMenu;
    protected boolean loading;

    protected void resetFields() {
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
    public void login(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            disableFields(true);

            root.setCursor(Cursor.WAIT);
            loading = true;
            loginManager.login();
        }
    }

    @FXML
    public void register(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            disableFields(false);

            root.setCursor(Cursor.WAIT);
            loading = true;
            loginManager.register();
        }
    }

    protected abstract void disableFields(boolean login);

    @FXML
    public void showLoginThirdStyle(){
        stageManager.changeStage(stageManager.thirdLoginStage);
    }
    @FXML
    public void showLoginFirstStyle(){
        stageManager.changeStage(stageManager.firstLoginStage);
    }

    @FXML
    public void showLoginSecondStyle(){
        stageManager.changeStage(stageManager.secondLoginStage);
    }

    @FXML
    public void close(){
        stageManager.currentStage.close();
    }
}
