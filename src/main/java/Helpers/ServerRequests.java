package Helpers;

import Models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
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

import static Helpers.Services.MessageService.lastMessageCheck;

public class ServerRequests {
    public static CloseableHttpClient httpClient = HttpClients.createDefault();
    public static CloseableHttpClient httpClientLongPolling = HttpClients.createDefault();
    public static Preferences userPreference = Preferences.userRoot();
    public static ObjectMapper mapper = new ObjectMapper();
    public static int pageSize = 3;
    public static User loggedUser;

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
                EntityUtils.consume(entity);

                if (responseStatus != 200) {
                    throw new HttpException("Invalid response code: " + responseStatus + ". With an error message: " + content);
                }

                sessions = mapper.readValue(content, new TypeReference<List<Session>>() {});
            } catch (IOException | HttpException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return sessions;
    }

    public static void sendOrder(Order order) throws Exception {
        String orderJson;
        orderJson = mapper.writeValueAsString(order);

        StringEntity postEntity = new StringEntity(orderJson, "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost httpPost = new HttpPost("http://localhost:8080/api/auth/order/create");
        httpPost.setHeader("Authorization", userPreference.get("Token", null));
        httpPost.setEntity(postEntity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

            int responseStatus = response.getStatusLine().getStatusCode();

            HttpEntity receivedEntity = response.getEntity();
            String content = EntityUtils.toString(receivedEntity);
            EntityUtils.consume(receivedEntity);

            if (responseStatus != 200) {
                throw new HttpException(content);
            }
        }
    }

    public static LocalDateTime getMostRecentOrderDate(int restaurantId) throws Exception{
        HttpGet get = new HttpGet("http://localhost:8080/api/auth/order/getMostRecentDate/" + restaurantId);
        LocalDateTime localDateTime;
        get.setHeader("Authorization", userPreference.get("Token", null));

        try (CloseableHttpResponse response = httpClient.execute(get)) {

            int responseStatus = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            if (responseStatus != 200) {
                throw new HttpException("Invalid response code: " + responseStatus + ". With an error message: " + content);
            }

            localDateTime = mapper.readValue(content, LocalDateTime.class);
        }
        return localDateTime;

    }

    public static List<Chat> getChats() throws Exception{
        List<Chat> chats = new ArrayList<>();
        URIBuilder builder = new URIBuilder("http://localhost:8080/api/auth/chat/getChats");
        builder.setParameter("pageSize", String.valueOf(pageSize));

        HttpGet get = new HttpGet(builder.build());
        get.setHeader("Authorization", userPreference.get("Token", null));

        try (CloseableHttpResponse response = httpClient.execute(get)) {

            int responseStatus = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            if (responseStatus != 200) {
                throw new HttpException(content);
            }

            chats = mapper.readValue(content, new TypeReference<List<Chat>>(){});
            lastMessageCheck = LocalDateTime.now();
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
            EntityUtils.consume(responseEntity);

            if (responseCode != 200) {
                throw new HttpException(content);
            }

            user = mapper.readValue(content, User.class);
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static Message sendMessage(String messageText, int chatId, int receiverId){
        Message message = null;
        Map<String, Object> jsonValues = new HashMap<>();
        jsonValues.put("message", messageText);
        jsonValues.put("chatId", chatId);
        jsonValues.put("receiverId", receiverId);

        JSONObject jsonObject = new JSONObject(jsonValues);
        StringEntity postEntity = new StringEntity(jsonObject.toString(), "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost post = new HttpPost("http://localhost:8080/api/auth/chat/newMessage");
        post.setHeader("Authorization", userPreference.get("Token", null));
        post.setEntity(postEntity);

        try(CloseableHttpResponse response = httpClient.execute(post)){
            int statusCode = response.getStatusLine().getStatusCode();

            HttpEntity responseEntity = response.getEntity();
            String content = EntityUtils.toString(responseEntity);
            EntityUtils.consume(responseEntity);

            if(statusCode != 200){
                throw new HttpException(content);
            }

            message = mapper.readValue(content, Message.class);
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
        return message;
    }
    public static void updateDishState(int orderId, int dishId) throws Exception{
        HttpPatch httpPatch = new HttpPatch(String.format("http://localhost:8080/api/auth/order/update/%d/%d", orderId, dishId));
        httpPatch.setHeader("Authorization", userPreference.get("Token", null));

        try(CloseableHttpResponse response = httpClient.execute(httpPatch)){
            int statusCode = response.getStatusLine().getStatusCode();

            HttpEntity responseEntity = response.getEntity();
            String content = EntityUtils.toString(responseEntity);
            EntityUtils.consume(responseEntity);

            if(statusCode != 200){
                throw new HttpException(content);
            }

        }
    }



}
