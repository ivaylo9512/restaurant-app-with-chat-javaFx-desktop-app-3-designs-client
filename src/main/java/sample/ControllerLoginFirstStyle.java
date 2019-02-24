package sample;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

public class ControllerLoginFirstStyle {
    @FXML public Button login, register;
    @FXML public TextField username, password;
    @FXML public Pane loginPane, menuBar, mainPane;
    @FXML public Text errorField;
    @FXML public Pane root;
    private LoginService loginService;

    @FXML
    public void initialize(){
        loginService = new LoginService();
        loginService.usernameProperty().bind(username.textProperty());
        loginService.passwordProperty().bind(password.textProperty());

        root.cursorProperty().bind(
                Bindings.when(loginService.runningProperty())
                        .then(Cursor.WAIT)
                        .otherwise(Cursor.DEFAULT)
        );

        password.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                login();
            }
        });
        username.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                login();
            }
        });
    }
    @FXML
    public void login(){

        try {
            loginService.start();
        }catch (IllegalStateException e){
            System.out.println("request is executing");
        }

        loginService.setOnSucceeded(event -> changeScene());
        loginService.setOnFailed(event -> updateError());
    }

    private void updateError() {

        System.out.println(loginService.getException().getMessage());
        loginService.reset();
    }

    private void changeScene() {
        try {
            Stage stage = (Stage) loginPane.getScene().getWindow();
            stage.close();
            LoggedFirstStyle.displayLoggedScene();

        } catch (IOException e) {
            e.printStackTrace();
        }
        loginService.reset();
    }

}
