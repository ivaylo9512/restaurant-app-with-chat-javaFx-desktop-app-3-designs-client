package Helpers;

import Application.LoginManager;
import javafx.concurrent.Task;
import org.apache.http.HttpException;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;

import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.stageManager;
import static Helpers.ServerRequests.executeRequest;
import static Helpers.ServerRequests.mapper;

public class TaskRequest<T> extends Task<T> {

    private Class<T> c;
    HttpRequestBase request;
    public TaskRequest(Class<T> c) {
        this.c = c;
    }

    @Override
    protected T call() throws Exception {
        request = createRequest(c);
        return executeTask();
    }

    private T executeTask() throws Exception{
        try {
            String content = executeRequest(request);
            if(content.equals("Success") || content.equals("Time out.")){
                return null;
            }
            return mapper.readValue(content, c);

        }catch (HttpException e) {
            String message = e.getMessage();
            if(message.equals("Jwt token has expired.")) {
                loginManager.logout();
                message = "Session has expired.";
            }
            stageManager.showAlert(message);
            throw e;
        }catch (IOException e) {
            if(stageManager.currentController instanceof LoginManager){
                stageManager.showAlert("No connection to the server");
                throw e;
            }
            return executeTask();
        }
    }
}
