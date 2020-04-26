package helpers;

import com.fasterxml.jackson.databind.JavaType;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.http.client.methods.HttpRequestBase;
import controllers.base.ControllerLogged;
import controllers.base.ControllerLogin;

import java.io.IOException;
import static application.RestaurantApplication.loginManager;
import static application.RestaurantApplication.stageManager;
import static application.ServerRequests.executeRequest;
import static application.ServerRequests.mapper;

public class RequestTask<T> extends Task<T> {

    private HttpRequestBase request;
    private JavaType type;
    public RequestTask(JavaType type, HttpRequestBase request) {
        this.type = type;
        this.request = request;
    }

    @Override
    protected T call() throws Exception {
        return executeTask();
    }
    private T executeTask() throws Exception{
        try {

            String content = executeRequest(request);
            while (content.equals("Time out.")){
                content = executeRequest(request);
            }

            if(content.equals("Success")){
                return null;
            }
            return mapper.readValue(content, type);

        }catch (IOException e) {
            if(stageManager.currentController instanceof ControllerLogin){
                throw new IOException("No connection to the server");
            }
            return executeTask();
        }
    }

    @Override
    protected void failed() {
        super.failed();
        String message = getException().getMessage();
        if(message.equals("Jwt token has expired.")) {
            if(stageManager.currentController instanceof ControllerLogged){
                Platform.runLater(() -> loginManager.logout());
            }
            message = "Session has expired.";
        }
        showAlert(message);
    }

    private void showAlert(String message) {
        Platform.runLater(() -> stageManager.showAlert(message));
    }
}
