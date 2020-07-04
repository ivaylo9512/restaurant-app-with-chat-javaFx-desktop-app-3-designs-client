package controllers.base;

import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import static application.RestaurantApplication.loginManager;
import static application.RestaurantApplication.stageManager;

public abstract class ControllerLogin extends ControllerAdjustable {
    @FXML
    protected HBox loadingPane;
    @FXML
    protected TextField username, password, regUsername, regPassword, regRepeatPassword;
    @FXML
    protected Pane loginPane, styleButtons, loginFields, registerFields, nextRegisterFields;

    protected Pane currentMenu;
    protected boolean loading;

    @FXML
    public void initialize(){
        loadingPane.opacityProperty().bind(Bindings.createDoubleBinding(()-> {
            if(loginManager.loading.get()){
                return 0.8;
            }
            return 0.0;
        }, loginManager.loading));
        loadingPane.disableProperty().bind(loginManager.loading.not());
        root.setStyle("-fx-font-size: " + fontPxProperty.get() + ";");
    }
    protected void resetFields() {
        username.setText(null);
        password.setText(null);
        regUsername.setText(null);
        regPassword.setText(null);
        regRepeatPassword.setText(null);
    }

    public void adjustStage(double height, double width) throws Exception{
        super.adjustStage(height, width);

        loginManager.bindLoginFields(username.textProperty(), password.textProperty());
        loginManager.bindRegisterFields(regUsername.textProperty(), regPassword.textProperty(), regRepeatPassword.textProperty());
    }

    @FXML
    public void login(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            disableFields(true);

            root.setCursor(Cursor.WAIT);
            loginManager.login();
        }
    }

    @FXML
    public void register(Event event){
        if(!KeyEvent.KEY_RELEASED.equals(event.getEventType()) || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            disableFields(false);

            root.setCursor(Cursor.WAIT);
            loginManager.register();
        }
    }

    protected void disableFields(boolean login){
        if(login)loginFields.setDisable(true);
        else nextRegisterFields.setDisable(true);
    }

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
}
