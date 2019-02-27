package sample;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class LoginService extends Service {
    private final StringProperty username = new SimpleStringProperty(this, "username");
    private final StringProperty password = new SimpleStringProperty(this, "username");
    public final StringProperty usernameProperty() { return username; }
    public final StringProperty passwordProperty() { return password; }

    private CloseableHttpClient httpClient = LoginFirstStyle.httpClient;
    @Override
    protected Task createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Map<String, Object> jsonValues = new HashMap<>();
                jsonValues.put("username", username.get());
                jsonValues.put("password", password.get());
                JSONObject json = new JSONObject(jsonValues);

                StringEntity postEntity = new StringEntity(json.toString(), "UTF8");
                postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                HttpPost httpPost = new HttpPost("http://localhost:8080/login");
                httpPost.setEntity(postEntity);

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

                    int responseStatus = response.getStatusLine().getStatusCode();
                    HttpEntity receivedEntity = response.getEntity();
                    String content = EntityUtils.toString(receivedEntity);

                    if (responseStatus != 200) {
                        EntityUtils.consume(receivedEntity);
                        throw new HttpException(content);
                    }

                    String jwtToken = response.getHeaders("Authorization")[0].getValue();
                    String userJson = response.getHeaders("user")[0].getValue();

                    Preferences userPreference = Preferences.userRoot();
                    userPreference.put("user", userJson);
                    userPreference.put("token", jwtToken);

                    EntityUtils.consume(receivedEntity);
                    return true;
                }
            }
        };
    }
}
