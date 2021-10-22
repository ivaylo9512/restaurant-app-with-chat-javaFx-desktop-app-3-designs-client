package helpers;

import com.fasterxml.jackson.databind.JavaType;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.http.client.methods.HttpRequestBase;
import controllers.base.ControllerLogged;

import static application.RestaurantApplication.*;
import static application.ServerRequests.executeRequest;
import static application.ServerRequests.mapper;

public class RequestTask<T> extends Task<T> {
    private final HttpRequestBase request;
    private final JavaType type;
    private String errorMessage;

    public RequestTask(JavaType type, HttpRequestBase request) {
        this.type = type;
        this.request = request;
    }

    public RequestTask(Class<T> t, HttpRequestBase request) {
        this.type = mapper.constructType(t);
        this.request = request;
    }

    @Override
    protected T call() throws Exception {
        return executeTask();
    }

    private T executeTask() throws Exception{
        String content = executeRequest(request);
        while (content.equals("Time out.")){
            content = executeRequest(request);
        }

        if(content.equals("Success")){
            return null;
        }
        return mapper.readValue(content, mapper.constructType(type));
    }

    @Override
    protected void failed() {
        super.failed();
        errorMessage = getException().getMessage();

        if(errorMessage.equals("Jwt token has expired.")) {
            if(stageManager.currentController instanceof ControllerLogged){
                Platform.runLater(() -> loginManager.logout());
            }

            errorMessage = "Session has expired.";
        }else if(errorMessage.contains("Connection refused")){
            errorMessage = "No connection to the server.";
        }

        Platform.runLater(() -> alertManager.addAlert(errorMessage));
    }
}
