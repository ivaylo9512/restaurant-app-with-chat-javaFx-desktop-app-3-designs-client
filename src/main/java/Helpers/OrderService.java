package Helpers;

import Models.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import sample.LoggedFirstStyle;
import sample.LoginFirstStyle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static sample.ControllerLoggedFirstStyle.*;
public class OrderService extends Service {
    private CloseableHttpClient httpClient = LoginFirstStyle.httpClient;
    @Override
    protected Task createTask() {
        return new Task<List<Order>>() {
            @Override
            protected List<Order> call() throws Exception {
                List<Order> orders = new ArrayList<>();

                StringEntity postEntity = new StringEntity(mapper.writeValueAsString(mostRecentOrderDate));
                postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                HttpPatch httpPatch = new HttpPatch("http://localhost:8080/api/auth/order/getUpdates");
                httpPatch.setHeader("Authorization", userPreference.get("token", null));
                httpPatch.setEntity(postEntity);
                try(CloseableHttpResponse response = httpClient.execute(httpPatch)) {

                    int responseStatus = response.getStatusLine().getStatusCode();
                    HttpEntity receivedEntity = response.getEntity();
                    String content = EntityUtils.toString(receivedEntity);

                    if (responseStatus != 200) {
                        EntityUtils.consume(receivedEntity);
                        throw new HttpException(content);
                    }

                    if(!content.equals("Time out.")){
                        orders = mapper.readValue(content, new TypeReference<List<Order>>(){});
                    }

                    EntityUtils.consume(receivedEntity);
                }
                return orders;

            }

            @Override
            protected void failed() {
                if(getException().getMessage().equals("Jwt token has expired.")) {
                    if (LoggedFirstStyle.stage != null) {
                        LoggedFirstStyle.stage.close();
                        LoginFirstStyle.stage.show();
                        reset();
                    }
                }
                System.out.println("hey");
                    restart();
            }
        };
    }
}
