package Application;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.client.methods.HttpRequestBase;

import static Helpers.ServerRequests.*;

public class ServiceRequest<T> extends Service<T> {

    private Class<T>  c;
    ServiceRequest(Class<T> c){
        this.c = c;
    }

    @Override
    protected Task<T> createTask() {
        return new Task<T>() {
            @Override
            protected T call() throws Exception {
                HttpRequestBase request = createRequest(c);

                String content = executeRequest(request);
                if(content.equals("Success") || content.equals("Time out.")){
                    return null;
                }

                return mapper.readValue(content, c);
            }
        };
    }
}
