package Helpers;

import Application.ServerRequests;
import com.fasterxml.jackson.databind.JavaType;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.client.methods.HttpRequestBase;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;

import static Application.ServerRequests.mapper;

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
            e.printStackTrace();
        }
    }

    @Override
    protected Task<T> createTask() {
        HttpRequestBase request = null;
        try {
            request = (HttpRequestBase) function.invokeExact();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return new RequestTask<>(t, request);
    }
}
