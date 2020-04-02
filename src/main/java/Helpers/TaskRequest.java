package Helpers;

import Application.LoginManager;
import Application.ServerRequests;
import com.fasterxml.jackson.databind.JavaType;
import javafx.concurrent.Task;
import org.apache.http.HttpException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.lang.invoke.*;
import java.util.Collection;
import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.stageManager;
import static Application.ServerRequests.executeRequest;
import static Application.ServerRequests.mapper;

public class TaskRequest<T> extends Task<T> {

    private HttpRequestBase request;
    private JavaType type;
    private MethodHandle function;
    public TaskRequest(JavaType type, MethodHandle function) {
        this.type = type;
        this.function = function;
    }

    @Override
    protected T call() throws Exception {
        try {
            request = (HttpRequestBase) function.invokeExact();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return executeTask();
    }

    private T executeTask() throws Exception{
        try {
            String content = executeRequest(request);
            if(content.equals("Success") || content.equals("Time out.")){
                return null;
            }
            return mapper.readValue(content, type);

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
