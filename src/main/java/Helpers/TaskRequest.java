package Helpers;

import javafx.concurrent.Task;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class TaskRequest<T> extends Task<T> {

    HttpRequestBase request;
    Class<T> c;
    public TaskRequest(HttpRequestBase request, Class<T> c) {
        this.request = request;
        this.c = c;
    }

    @Override
    protected T call() throws Exception {
        return executeTask();
    }

    private T executeTask() {
        T t;
        try{
            t = ServerRequests.executeRequest(request, c);
        }catch (IOException e){
            return executeTask();
        }
        return t;
    }
}
