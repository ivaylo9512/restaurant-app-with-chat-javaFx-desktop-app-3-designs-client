package sample;

import javafx.application.Platform;
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
import java.util.concurrent.CountDownLatch;
import java.util.prefs.Preferences;

public class LoginService extends Service {
    public Stage stage;
    public String username;
    public String password;
    private CloseableHttpClient httpClient = LoginFirstStyle.httpClient;
    private boolean authenticated;
    LoginService(){
    }
    @Override
    protected Task createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Map<String, Object> jsonValues = new HashMap<>();
                jsonValues.put("username", username);
                jsonValues.put("password", password);
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
                        throw new HttpException("Invalid response code: " + responseStatus + ". With an error message: " + content);
                    }

                    authenticated = true;
                    String jwtToken = response.getHeaders("Authorization")[0].getValue();
                    String userJson = response.getHeaders("user")[0].getValue();

                    Preferences userPreference = Preferences.userRoot();
                    userPreference.put("user", userJson);
                    userPreference.put("token", jwtToken);

                    EntityUtils.consume(receivedEntity);
                } catch (IOException | HttpException e) {
                    e.printStackTrace();
                    reset();
                }
                final CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        if (authenticated) {
                            try {
                                LoggedFirstStyle.displayLoggedScene();
                                stage.close();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
                latch.await();
                return null;
            }
        };
    }
    @Override
    protected void succeeded() {
        reset();
    }

    @Override
    protected void failed() {
        reset();
    }

    private void changeScene(){
        try {
            LoggedFirstStyle.displayLoggedScene();
            stage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
