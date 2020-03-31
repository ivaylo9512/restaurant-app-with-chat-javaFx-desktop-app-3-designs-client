package Helpers;

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
    public TaskRequest(Class<T> c) {
        this.c = c;
    }

    @Override
    protected T call() throws Exception {
        return executeTask();
    }

    private T executeTask() throws Exception{
        try {
            HttpRequestBase request = createRequest(c);
            String content = executeRequest(request);
            if(content.equals("Success") || content.equals("Time out.")){
                return null;
            }
            return mapper.readValue(content, c);

        }catch (HttpException e) {
            if(e.getMessage().equals("Jwt token has expired."))
            loginManager.logout();
            stageManager.showAlert("Session has expired.");

            throw new HttpException(e.getMessage());
        }catch (IOException e) {
            return executeTask();
        }
    }
}
