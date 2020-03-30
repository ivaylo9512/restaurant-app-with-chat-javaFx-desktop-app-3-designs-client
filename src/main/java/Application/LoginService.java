package Application;

import Helpers.ServerRequests;
import Models.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


import static Helpers.ServerRequests.*;
import java.util.HashMap;
import java.util.Map;

class LoginService extends Service<User> {
    final StringProperty username = new SimpleStringProperty(this, "username");
    final StringProperty password = new SimpleStringProperty(this, "password");

    @Override
    public void start() {
        if(!isRunning()) {
            reset();
            super.start();
        }
    }

    @Override
    protected Task<User> createTask() {
        return new Task<User>() {
            @Override
            protected User call() throws Exception {
                Map<String, Object> jsonValues = new HashMap<>();
                jsonValues.put("username", username.get());
                jsonValues.put("password", password.get());
                JSONObject json = new JSONObject(jsonValues);

                StringEntity postEntity = new StringEntity(json.toString(), "UTF8");
                postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                HttpPost httpPost = new HttpPost( base + "/api/users/login");
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

                    User user = mapper.readValue(content, User.class);
                    ServerRequests.userPreference.put(String.valueOf(user.getId().get()), jwtToken);

                    EntityUtils.consume(receivedEntity);

                    return mapper.readValue(content, User.class);
                }
            }
        };
    }
}
