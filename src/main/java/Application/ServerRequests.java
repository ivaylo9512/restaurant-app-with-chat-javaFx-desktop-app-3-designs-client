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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import static Application.RestaurantApplication.loginManager;
import static Application.RestaurantApplication.orderManager;

public class ServerRequests {
    public static CloseableHttpClient httpClient = HttpClients.createDefault();
    public static CloseableHttpClient httpClientLongPolling = HttpClients.createDefault();
    public static Preferences userPreference = Preferences.userRoot();
    public static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    public static int pageSize = 3;
    public static String base = "http://localhost:8080";

    static ExecutorService tasks = Executors.newFixedThreadPool(10);

    public static List<Session> getNextSessions(int id, int page) {
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
//            handleException(e.getMessage());
        }

        return sessions;
    }

    public static HttpRequestBase sendOrder() throws Exception {
        String orderJson = mapper.writeValueAsString(orderManager.newOrder);

        StringEntity postEntity = new StringEntity(orderJson, "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost httpPost = new HttpPost(base + "/api/order/auth/create");
        httpPost.setHeader("Authorization", userPreference.get("jwt", null));
        httpPost.setEntity(postEntity);

        return httpPost;
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

    public static HttpRequestBase getLoggedUser(){
        HttpGet httpGet = new HttpGet( base + "/api/users/auth/getLoggedUser");
        httpGet.setHeader("Authorization", userPreference.get("jwt", null));
        return httpGet;
    }
    public static HttpRequestBase waitOrders() throws Exception{
        List<Order> orders = new ArrayList<>();
        String mostRecentDate = mapper.writeValueAsString(orderManager.mostRecentOrderDate);
        String restaurantId = String.valueOf(orderManager.userRestaurant.getId());

        StringEntity postEntity = new StringEntity(mostRecentDate, "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        URIBuilder builder = new URIBuilder(base + "/api/order/auth/getUpdates");
        builder.setParameter("restaurantId", restaurantId);

        HttpPatch httpPatch = new HttpPatch(builder.build());
        httpPatch.setHeader("Authorization", userPreference.get("jwt", null));
        httpPatch.setEntity(postEntity);

        return httpPatch;
    }
    public static HttpRequestBase register(){
        Map<String, Object> jsonValues = new HashMap<>();
        jsonValues.put("username", loginManager.regUsername.get());
        jsonValues.put("password", loginManager.regPassword.get());
        jsonValues.put("repeatPassword", loginManager.repeatPassword.get());
        JSONObject json = new JSONObject(jsonValues);

        StringEntity postEntity = new StringEntity(json.toString(), "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost httpPost = new HttpPost(base + "/api/users/register");
        httpPost.setEntity(postEntity);

        return httpPost;
    }

    public static HttpRequestBase sendUserInfo() {
        Map<String, Object> jsonValues = new HashMap<>();
        jsonValues.put("firstName", loginManager.loggedUser.getFirstName().get());
        jsonValues.put("lastName", loginManager.loggedUser.getLastName().get());
        jsonValues.put("age", loginManager.loggedUser.getAge().get());
        jsonValues.put("country", loginManager.loggedUser.getCountry().get());

        JSONObject jsonObject = new JSONObject(jsonValues);

        StringEntity postEntity = new StringEntity(jsonObject.toString(), "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost post = new HttpPost(base + "/api/users/auth/changeUserInfo");
        post.setHeader("Authorization", userPreference.get("jwt", null));
        post.setEntity(postEntity);

        return post;
    }

    public static HttpRequestBase sendMessage(){
        Message message = null;
        Map<String, Object> jsonValues = new HashMap<>();
//        jsonValues.put("message", messageText);
//        jsonValues.put("chatId", chatId);
//        jsonValues.put("receiverId", receiverId);

        JSONObject jsonObject = new JSONObject(jsonValues);
        StringEntity postEntity = new StringEntity(jsonObject.toString(), "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost post = new HttpPost(base + "/api/chat/auth/newMessage");
        post.setHeader("Authorization", userPreference.get("jwt", null));
        post.setEntity(postEntity);

        return post;
    }
    public static HttpRequestBase updateDishState(int orderId, int dishId) {
        HttpPatch patch = new HttpPatch(String.format(base + "/api/order/auth/update/%d/%d", orderId, dishId));
        patch.setHeader("Authorization", userPreference.get("jwt", null));

        return patch;
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
}
