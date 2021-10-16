package helpers;

import application.ServerRequests;
import com.fasterxml.jackson.databind.JavaType;
import controllers.base.ControllerLogin;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.client.methods.HttpRequestBase;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;

import static application.RestaurantApplication.alertManager;
import static application.RestaurantApplication.stageManager;
import static application.ServerRequests.mapper;

public class RequestService<T> extends Service<T> {
    private JavaType t;
    private MethodHandle function;
    public RequestService(Class<T> type, RequestEnum requestType){
        t = mapper.getTypeFactory().constructType(type);

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
        HttpRequestBase request;
        try {
            request = (HttpRequestBase) function.invokeExact();
        } catch (Throwable throwable) {
            showAlert(throwable.getMessage());
            return null;
        }
        return new RequestTask<>(t, request);
    }

    private void showAlert(String message) {
        if (stageManager.currentController instanceof ControllerLogin) {
            alertManager.addLoginAlert(message);
        } else {
            alertManager.addLoggedAlert(message);
        }
    }
}
