package sample;

import Helpers.LoginService;
import Helpers.RegisterService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.IOException;
import java.net.ConnectException;

public class ControllerLoginFirstStyle {
    @FXML TextField username, password, regUsername, regPassword, regRepeatPassword;
    @FXML AnchorPane loginPane;
    @FXML Pane root, background, fieldsContainer;
    @FXML Button loginButton, registerButton, actionButton;
    @FXML Pane loginFields, registerFields;

    private LoginService loginService;
    private RegisterService registerService;

    @FXML
    public void initialize(){
        loginService = new LoginService();
        loginService.usernameProperty().bind(username.textProperty());
        loginService.passwordProperty().bind(password.textProperty());

        registerService = new RegisterService();
        registerService.usernameProperty().bind(regUsername.textProperty());
        registerService.passwordProperty().bind(regPassword.textProperty());
        registerService.repeatPasswordProperty().bind(regRepeatPassword.textProperty());

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        background.setPrefHeight(primaryScreenBounds.getHeight());
        background.setPrefWidth(primaryScreenBounds.getWidth());

        background.getParent().layoutXProperty().addListener((observable, oldValue, newValue) -> background.setLayoutX(0 - background.getParent().getLayoutX()));
        background.getParent().layoutYProperty().addListener((observable, oldValue, newValue) -> background.setLayoutY(0 - background.getParent().getLayoutY()));

        root.cursorProperty().bind(
                Bindings.when(loginService.runningProperty())
                        .then(Cursor.WAIT)
                        .otherwise(Cursor.DEFAULT)
        );

        actionButton.setOnMousePressed(this::login);

        loginButton.setOnAction(event -> {
            fieldsContainer.setOpacity(0);
            Timeline opacity = new Timeline(new KeyFrame(Duration.millis(100), event1 -> {
                fieldsContainer.setOpacity(1);
            }));
            opacity.play();

            actionButton.setText("LOGIN");
            actionButton.setOnMousePressed(this::login);

            loginFields.setDisable(false);
            loginFields.setOpacity(1);
            registerFields.setDisable(true);
            registerFields.setOpacity(0);
        });
        registerButton.setOnAction(event -> {
            fieldsContainer.setOpacity(0);
            Timeline opacity = new Timeline(new KeyFrame(Duration.millis(100), event1 -> {
                fieldsContainer.setOpacity(1);
            }));
            opacity.play();

            actionButton.setText("REGISTER");
            actionButton.setOnMousePressed(this::register);

            registerFields.setDisable(false);
            registerFields.setOpacity(1);
            loginFields.setDisable(true);
            loginFields.setOpacity(0);
        });
    }
    @FXML
    public void login(Event event){
        if(!event.getEventType().getName().equals("KEY_PRESSED") || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            try {
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
        if(!event.getEventType().getName().equals("KEY_PRESSED") || ((KeyEvent) event).getCode().equals(KeyCode.ENTER)) {
            try {
                registerService.start();
            } catch (IllegalStateException e) {
                System.out.println("request is executing");
            }
        }

        registerService.setOnSucceeded(eventSuccess -> changeScene(registerService));
        registerService.setOnFailed(eventFail -> updateError(registerService));
    }
    private void updateError(Service service) {
        Alert alert = LoginFirstStyle.alert;
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
        try {
            Stage stage = (Stage) loginPane.getScene().getWindow();
            stage.close();
            LoggedFirstStyle.displayLoggedScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
        service.reset();
    }

}
