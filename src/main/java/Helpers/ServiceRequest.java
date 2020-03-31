package Helpers;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ServiceRequest<T> extends Service<T> {

    private Class<T>  c;
    ServiceRequest(Class<T> c){
        this.c = c;
    }

    @Override
    protected Task<T> createTask() {
        return new TaskRequest<>(c);
    }
}
