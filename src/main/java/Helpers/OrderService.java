package Helpers;

import Models.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.util.Duration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import sample.LoggedFirstStyle;
import sample.LoginFirstStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Helpers.ServerRequests.mapper;
import static sample.ControllerLoggedFirstStyle.*;

public class OrderService extends Service {
    private CloseableHttpClient httpClient = ServerRequests.httpClient;

    @Override
    protected Task createTask() {
        return new Task<List<Order>>() {
            @Override
            protected List<Order> call() throws Exception {
                List<Order> orders = new ArrayList<>();

                String mostRecentDate = mapper.writeValueAsString(mostRecentOrderDate);
                String restaurantId = String.valueOf(loggedUser.getRestaurant().getId());

                Map<String, Object> jsonValues = new HashMap<>();
                jsonValues.put("lastUpdate", mostRecentDate);
                JSONObject json = new JSONObject(jsonValues);

                StringEntity postEntity = new StringEntity(mostRecentDate, "UTF8");
                postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                URIBuilder builder = new URIBuilder("http://localhost:8080/api/auth/order/getUpdates");
                builder.setParameter("restaurantId", restaurantId);

                HttpPatch httpPatch = new HttpPatch(builder.build());
                httpPatch.setHeader("Authorization", userPreference.get("Token", null));
                httpPatch.setEntity(postEntity);

                try (CloseableHttpResponse response = httpClient.execute(httpPatch)) {

                    int responseStatus = response.getStatusLine().getStatusCode();
                    HttpEntity receivedEntity = response.getEntity();
                    String content = EntityUtils.toString(receivedEntity);

                    if (responseStatus != 200) {
                        EntityUtils.consume(receivedEntity);
                        throw new HttpException(content);
                    }

                    if (!content.equals("Time out.")) {
                        orders = mapper.readValue(content, new TypeReference<List<Order>>() {
                        });
                    }

                    EntityUtils.consume(receivedEntity);
                }
                return orders;

            }

            @Override
            protected void failed() {
                if (getException().getMessage().equals("Jwt token has expired.")) {
                    if (LoggedFirstStyle.stage != null) {
                        LoggedFirstStyle.stage.close();
                        LoginFirstStyle.stage.show();

                        Alert alert = LoginFirstStyle.alert;
                        DialogPane dialog = alert.getDialogPane();
                        dialog.setContentText("Session has expired.");
                        alert.showAndWait();

                        reset();
                    }
                } else {
                    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(3000), event -> {
                        restart();
                    }));
                    timeline.play();
                }
            }
        };
    }
}
