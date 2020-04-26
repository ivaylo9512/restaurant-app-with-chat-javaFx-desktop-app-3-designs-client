package controllers.thirdstyle;

import animations.TransitionResizeHeight;
import animations.TransitionResizeWidth;
import javafx.animation.*;
import javafx.concurrent.Service;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;
import controllers.base.ControllerLogin;

public class LoginThird extends ControllerLogin {
    @FXML AnchorPane contentRoot, menu;
    @FXML Button actionBtn;
    @FXML Text loginBtn;
    @FXML Line menuLine;
    @FXML StackPane logo;

    private Text currentText;

    @FXML
    public void initialize(){
        currentMenu = loginFields;
        currentText = loginBtn;
    }

    private void changeScene(Service service) {
        loginAnimation();
    }

    private void loginAnimation() {
        menu.setOpacity(0);
        menuLine.setOpacity(0);
        logo.setOpacity(0);
        loginFields.setOpacity(0);
        actionBtn.setOpacity(0);

        TransitionResizeWidth resizeWidth = new TransitionResizeWidth(Duration.millis(1200), contentRoot, 161);
        TransitionResizeHeight resizeHeight = new TransitionResizeHeight(Duration.millis(1200), contentRoot, 627);

        TranslateTransition translate = new TranslateTransition(Duration.millis(1200), contentRoot);
        translate.setToX(67.5);
        translate.setToY(-87.5);

        ParallelTransition parallelTransition = new ParallelTransition(resizeHeight,resizeWidth, translate);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(700), contentRoot);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.millis(200));

        //TODO
//        Timeline closeStage = new Timeline(new KeyFrame(Duration.millis(500), event ->
//                LoginThirdStyle.stage.close()));

//        SequentialTransition sequentialTransition = new SequentialTransition(parallelTransition, fadeOut,closeStage);
//        sequentialTransition.play();
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
    public void resetStage(){
        if(loading){
            username.setDisable(false);
            password.setDisable(false);
            regUsername.setDisable(false);
            regPassword.setDisable(false);
            regRepeatPassword.setDisable(false);
            root.setCursor(Cursor.DEFAULT);
        }else {
            root.setCursor(Cursor.DEFAULT);
            username.setDisable(false);
            password.setDisable(false);

            menu.setOpacity(1);
            menuLine.setOpacity(1);
            logo.setOpacity(1);
            actionBtn.setOpacity(1);

            contentRoot.setOpacity(1);
            contentRoot.setTranslateX(0);
            contentRoot.setTranslateY(0);
            contentRoot.setPrefWidth(296);
            contentRoot.setPrefHeight(452);

            resetFields();

            setStrikeThrough(loginBtn);
            showMenu(loginFields);

            actionBtn.setText("login");
            actionBtn.setOnMouseClicked(this::login);
        }
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
