package sample;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.prefs.Preferences;

public class ControllerLoginFirstStyle {
    @FXML public Button login, register;
    @FXML
    public TextField username, password;
    @FXML public Pane loginPane, menuBar, mainPane;
    @FXML public Text errorField;
    LoginService loginService = new LoginService();
    private CloseableHttpClient httpClient = LoginFirstStyle.httpClient;
    boolean authenticated = false;
    @FXML
    public void login(){
        Stage stage = (Stage) loginPane.getScene().getWindow();
        loginService.setStage(stage);
        loginService.setUsername(username.getText());
        loginService.setPassword(password.getText());
        try {
            loginService.start();
        }catch (IllegalStateException e){
            System.out.println("request is executing");
        }
    }
    private void changeScene(){
        try {
            Stage window = (Stage)loginPane.getScene().getWindow();
            LoggedFirstStyle.displayLoggedScene();
            window.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
