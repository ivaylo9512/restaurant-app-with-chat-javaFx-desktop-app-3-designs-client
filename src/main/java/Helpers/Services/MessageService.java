package Helpers.Services;

import Models.Message;
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
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import sample.LoggedFirstStyle;
import sample.LoginFirstStyle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static Helpers.ServerRequests.*;


public class MessageService extends Service {

    public static LocalDateTime lastMessageCheck;
    @Override
    protected Task createTask() {
        return new Task<List<Message>>() {
            @Override
            protected List<Message> call() throws Exception {
                List<Message> messages = new ArrayList<>();

                String mostRecentDate = mapper.writeValueAsString(lastMessageCheck);

                StringEntity patchEntity = new StringEntity(mostRecentDate, "UTF8");
                patchEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                HttpPatch httpPatch = new HttpPatch(base + "/api/auth/chat/getChatUpdates");
                httpPatch.setHeader("Authorization", userPreference.get(String.valueOf(loggedUser.getId()), null));
                httpPatch.setEntity(patchEntity);
                try (CloseableHttpResponse response = httpClientLongPolling.execute(httpPatch)) {

                    int responseStatus = response.getStatusLine().getStatusCode();
                    HttpEntity receivedEntity = response.getEntity();
                    String content = EntityUtils.toString(receivedEntity);

                    if (responseStatus != 200) {
                        EntityUtils.consume(receivedEntity);
                        throw new HttpException(content);
                    }

                    if (!content.equals("Time out.")) {
                        messages = mapper.readValue(content, new TypeReference<List<Message>>() {
                        });
                    }

                    EntityUtils.consume(receivedEntity);
                }
                return messages;

            }
        };
    }
}
