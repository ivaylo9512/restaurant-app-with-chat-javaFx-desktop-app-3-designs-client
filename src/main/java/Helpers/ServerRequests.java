package Helpers;

import Models.Chat;
import Models.Order;
import Models.Session;
import Models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.prefs.Preferences;

public class ServerRequests {
    static CloseableHttpClient httpClient = HttpClients.createDefault();
    static Preferences userPreference = Preferences.userRoot();
    public static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
    }

    public static List<Session> getNextSessions(int id, int page, int pageSize) {
        HttpGet get;
        List<Session> sessions = new LinkedList<>();
        try {

            URIBuilder builder = new URIBuilder("http://localhost:8080/api/auth/chat/nextSessions");
            builder
                    .setParameter("chatId", String.valueOf(id))
                    .setParameter("page", String.valueOf(page))
                    .setParameter("pageSize", String.valueOf(pageSize));
            get = new HttpGet(builder.build());
            get.setHeader("Authorization", userPreference.get("Token", null));

            try (CloseableHttpResponse response = httpClient.execute(get)) {

                int responseStatus = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity);

                if (responseStatus != 200) {
                    EntityUtils.consume(entity);
                    throw new HttpException("Invalid response code: " + responseStatus + ". With an error message: " + content);
                }

                sessions = mapper.readValue(content, new TypeReference<List<Session>>() {
                });
                EntityUtils.consume(entity);

            } catch (IOException | HttpException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return sessions;
    }

    public static Boolean sendOrder(Order order) {
        String orderJson;
        try {
            orderJson = mapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }

        StringEntity postEntity = new StringEntity(orderJson, "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost httpPost = new HttpPost("http://localhost:8080/api/auth/order/create");
        httpPost.setHeader("Authorization", userPreference.get("Token", null));
        httpPost.setEntity(postEntity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

            int responseStatus = response.getStatusLine().getStatusCode();
            HttpEntity receivedEntity = response.getEntity();
            String content = EntityUtils.toString(receivedEntity);

            if (responseStatus != 200) {
                EntityUtils.consume(receivedEntity);
                throw new HttpException(content);
            }
            EntityUtils.consume(receivedEntity);

        } catch (HttpException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static LocalDateTime getMostRecentOrderDate(int restaurantId) {
        HttpGet get = new HttpGet("http://localhost:8080/api/auth/order/getMostRecentDate/" + restaurantId);
        LocalDateTime localDateTime = LocalDateTime.now();
        get.setHeader("Authorization", userPreference.get("Token", null));

        try (CloseableHttpResponse response = httpClient.execute(get)) {

            int responseStatus = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);

            if (responseStatus != 200) {
                EntityUtils.consume(entity);
                throw new HttpException("Invalid response code: " + responseStatus + ". With an error message: " + content);
            }

            localDateTime = mapper.readValue(content, LocalDateTime.class);
            EntityUtils.consume(entity);

        } catch (HttpException | IOException e) {
            e.printStackTrace();
        }
        return localDateTime;

    }

    public static List<Chat> getChats() {
        List<Chat> chats = new ArrayList<>();
        HttpGet get = new HttpGet("http://localhost:8080/api/auth/chat/getChats");
        get.setHeader("Authorization", userPreference.get("Token", null));

        try (CloseableHttpResponse response = httpClient.execute(get)) {

            int responseStatus = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);

            if (responseStatus != 200) {
                EntityUtils.consume(entity);
                throw new HttpException("Invalid response code: " + responseStatus + ". With an error message: " + content);
            }

            chats = mapper.readValue(content, new TypeReference<List<Chat>>() {
            });
            EntityUtils.consume(entity);

        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }

        return chats;
    }

    public static User sendUserInfo(String firstName, String lastName, String age, String country) {
        User user = null;
        Map<String, Object> jsonValues = new HashMap<>();
        jsonValues.put("firstName", firstName);
        jsonValues.put("lastName", lastName);
        jsonValues.put("age", age);
        jsonValues.put("country", country);

        JSONObject jsonObject = new JSONObject(jsonValues);

        StringEntity postEntity = new StringEntity(jsonObject.toString(), "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost post = new HttpPost("http://localhost:8080/api/auth/users/changeUserInfo");
        post.setHeader("Authorization", userPreference.get("Token", null));
        post.setEntity(postEntity);

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();
            String content = EntityUtils.toString(responseEntity);

            if (responseCode != 200) {
                EntityUtils.consume(responseEntity);
                throw new HttpException(content);
            }

            user = mapper.readValue(content, User.class);
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
        return user;
    }


}
