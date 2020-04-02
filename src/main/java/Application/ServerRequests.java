package Application;

import Models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.*;
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

import static Application.MessageService.lastMessageCheck;
import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.stageManager;

public class ServerRequests {
    public static CloseableHttpClient httpClient = HttpClients.createDefault();
    public static CloseableHttpClient httpClientLongPolling = HttpClients.createDefault();
    public static Preferences userPreference = Preferences.userRoot();
    public static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    public static int pageSize = 3;
    public static String base = "http://localhost:8080";

    public static List<Session> getNextSessions(int id, int page, int pageSize) {
        List<Session> sessions = new LinkedList<>();
        try {

            URIBuilder builder = new URIBuilder(base + "/api/chat/auth/nextSessions");
            builder
                    .setParameter("chatId", String.valueOf(id))
                    .setParameter("page", String.valueOf(page))
                    .setParameter("pageSize", String.valueOf(pageSize));
            HttpGet get = new HttpGet(builder.build());
            get.setHeader("Authorization", userPreference.get("jwt", null));

            sessions = mapper.readValue(executeRequest(get), new TypeReference<List<Session>>() {});

        } catch (IOException | HttpException | URISyntaxException e) {
            handleException(e.getMessage());
        }

        return sessions;
    }

    public static boolean sendOrder(Order order) {
        try {
            String orderJson;
            orderJson = mapper.writeValueAsString(order);

            StringEntity postEntity = new StringEntity(orderJson, "UTF8");
            postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            HttpPost httpPost = new HttpPost(base + "/api/order/auth/create");
            httpPost.setHeader("Authorization", userPreference.get("jwt", null));
            httpPost.setEntity(postEntity);

            executeRequest(httpPost);

            return true;
        }catch (IOException | HttpException e) {
            handleException(e.getMessage());
        }
        return false;
    }

    public static LocalDateTime getMostRecentOrderDate(int restaurantId) {
        LocalDateTime localDateTime = null;

        HttpGet get = new HttpGet(base + "/api/order/auth/getMostRecentDate/" + restaurantId);
        get.setHeader("Authorization", userPreference.get("jwt", null));

        try{
            localDateTime = mapper.readValue(executeRequest(get), LocalDateTime.class);
        }catch (IOException | HttpException e) {
            handleException(e.getMessage());
        }

        return localDateTime;
    }

    public static List<Chat> getChats() {
        List<Chat> chats = null;
        try{

            URIBuilder builder = new URIBuilder(base + "/api/chat/auth/getChats");
            builder.setParameter("pageSize", String.valueOf(pageSize));

            HttpGet get = new HttpGet(builder.build());
            get.setHeader("Authorization", userPreference.get("jwt", null));

            chats = mapper.readValue(executeRequest(get), new TypeReference<List<Chat>>() {});
            lastMessageCheck = LocalDateTime.now();

        }catch (IOException | HttpException | URISyntaxException e) {
            handleException(e.getMessage());
        }

        return chats;
    }

    public static HttpRequestBase login(){
        Map<String, Object> jsonValues = new HashMap<>();
        jsonValues.put("username", loginManager.username.get());
        jsonValues.put("password", loginManager.password.get());
        JSONObject json = new JSONObject(jsonValues);

        StringEntity postEntity = new StringEntity(json.toString(), "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost httpPost = new HttpPost( base + "/api/users/login");
        httpPost.setEntity(postEntity);
        return httpPost;
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

        HttpPost post = new HttpPost(base + "/api/users/auth/changeUserInfo");
        post.setHeader("Authorization", userPreference.get("jwt", null));
        post.setEntity(postEntity);

        try{
            user = mapper.readValue(executeRequest(post), User.class);
        } catch (IOException | HttpException e) {
            handleException(e.getMessage());
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

        HttpPost post = new HttpPost(base + "/api/chat/auth/newMessage");
        post.setHeader("Authorization", userPreference.get("jwt", null));
        post.setEntity(postEntity);

        try{
            message = mapper.readValue(executeRequest(post), Message.class);
        }catch (IOException | HttpException e) {
            handleException(e.getMessage());
        }

        return message;
    }
    public static boolean updateDishState(int orderId, int dishId) {
        HttpPatch patch = new HttpPatch(String.format(base + "/api/order/auth/update/%d/%d", orderId, dishId));
        patch.setHeader("Authorization", userPreference.get("jwt", null));

        try{
            executeRequest(patch);
            return true;
        } catch (IOException | HttpException e) {
            handleException(e.getMessage());
        }

        return false;
    }

    public static String executeRequest(HttpRequestBase request) throws HttpException, IOException{
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int responseCode = response.getStatusLine().getStatusCode();

            HttpEntity responseEntity = response.getEntity();
            String content = EntityUtils.toString(responseEntity);
            EntityUtils.consume(responseEntity);

            if (responseCode != 200) {
                throw new HttpException(content);
            }

            Header[] authHeaders = response.getHeaders("Authorization");
            if(authHeaders.length > 0){
                userPreference.put("jwt", authHeaders[0].getValue());
            }

            return content;
        }
    }

    private static void handleException(String exception) {
        if (exception.equals("Jwt token has expired.")) {
            loginManager.logout();
        }
        stageManager.showAlert("Session has expired.");
    }
}
