package sample.base;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;

import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.stageManager;

public abstract class ControllerLogin {
    @FXML
    protected TextField username, password, regUsername, regPassword, regRepeatPassword;
    @FXML
    protected AnchorPane root, loginPane, loginFields, registerFields, nextRegisterFields, styleButtons;

    protected Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

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

    public void setStage(){
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        loginPane.setLayoutY((primaryScreenBounds.getHeight() - loginPane.getHeight()) / 2);
        loginPane.setLayoutX((primaryScreenBounds.getWidth() - loginPane.getWidth()) / 2);

        loginManager.bindLoginFields(username.textProperty(), password.textProperty());
        loginManager.bindRegisterFields(regUsername.textProperty(), regPassword.textProperty(), regRepeatPassword.textProperty());
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
    public void minimize(){
        stageManager.currentStage.setIconified(true);
    }

    @FXML
    public void close(){
        stageManager.currentStage.close();
    }
}
