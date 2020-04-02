package Helpers;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.Collection;

public class RequestService<T> extends Service<T> {

    private Class type;
    private Class<? extends Collection> collection;
    private RequestEnum requestType;
    public RequestService(Class type, Class<? extends Collection> collection, RequestEnum requestType){
        this.type = type;
        this.collection = collection;
        this.requestType = requestType;
    }

    @Override
    protected Task<T> createTask() {
        return new TaskRequest<>(type, collection, requestType);
    }
}
