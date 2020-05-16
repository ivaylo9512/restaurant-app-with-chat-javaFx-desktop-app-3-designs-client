package helpers;

import application.ServerRequests;
import com.fasterxml.jackson.databind.JavaType;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.client.methods.HttpRequestBase;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;

import static application.RestaurantApplication.stageManager;
import static application.ServerRequests.mapper;

public class RequestService<T> extends Service<T> {

    private JavaType t;
    private MethodHandle function;
    public RequestService(Class type, Class<? extends Collection> collection, RequestEnum requestType){
        if (collection == null) {
            t = mapper.getTypeFactory().constructType(type);
        } else {
            t = mapper.getTypeFactory().
                    constructCollectionType(collection, type);
        }

        try {
            MethodType methodType = MethodType.methodType(HttpRequestBase.class);
            function = MethodHandles.lookup()
                    .findStatic(ServerRequests.class, requestType.name(), methodType);
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    @Override
    protected Task<T> createTask() {
        HttpRequestBase request = null;
        try {
            request = (HttpRequestBase) function.invokeExact();
        } catch (Throwable throwable) {
            showAlert(throwable.getMessage());
            return null;
        }
        return new RequestTask<>(t, request);
    }

    private void showAlert(String message) {
        Platform.runLater(() -> stageManager.showAlert(message));
    }
}
