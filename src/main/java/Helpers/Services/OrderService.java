package Helpers.Services;

import Models.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static Helpers.ServerRequests.*;
import static Helpers.ServerRequests.userPreference;

public class OrderService extends Service {

    public static LocalDateTime mostRecentOrderDate;
    @Override
    protected Task createTask() {
        return new Task<List<Order>>() {
            @Override
            protected List<Order> call() throws Exception {
                List<Order> orders = new ArrayList<>();
                String mostRecentDate = mapper.writeValueAsString(mostRecentOrderDate);
                String restaurantId = String.valueOf(loggedUser.getRestaurant().getId());

                StringEntity postEntity = new StringEntity(mostRecentDate, "UTF8");
                postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                URIBuilder builder = new URIBuilder("http://localhost:8080/api/auth/order/getUpdates");
                builder.setParameter("restaurantId", restaurantId);

                HttpPatch httpPatch = new HttpPatch(builder.build());
                httpPatch.setHeader("Authorization", userPreference.get("Token", null));
                httpPatch.setEntity(postEntity);

                try (CloseableHttpResponse response = httpClientLongPolling.execute(httpPatch)) {
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
        };
    }
}
