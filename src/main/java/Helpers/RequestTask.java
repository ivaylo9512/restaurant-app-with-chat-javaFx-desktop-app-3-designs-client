package Helpers;

import Application.LoginManager;
import com.fasterxml.jackson.databind.JavaType;
import javafx.concurrent.Task;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.stageManager;
import static Application.ServerRequests.executeRequest;
import static Application.ServerRequests.mapper;

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
            if(content.equals("Success") || content.equals("Time out.")){
                return null;
            }
            return mapper.readValue(content, type);

        }catch (IOException e) {

            if(stageManager.currentController instanceof LoginManager){
                stageManager.showAlert("No connection to the server");
                throw e;
            }
            return executeTask();

        }catch (Exception e) {

            String message = e.getMessage();
            if(message.equals("Jwt token has expired.")) {
                loginManager.logout();
                message = "Session has expired.";
            }
            stageManager.showAlert(message);
            throw e;

        }
    }
}
