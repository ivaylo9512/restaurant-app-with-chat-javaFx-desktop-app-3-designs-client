package application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import exceptions.UnprocessableEntityException;
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
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import static application.RestaurantApplication.loginManager;
import static application.RestaurantApplication.orderManager;

public class ServerRequests {
    public static CloseableHttpClient httpClient = HttpClients.createDefault();
    public static Preferences userPreference = Preferences.userRoot();
    public static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    public static int pageSize = 3;
    public static int userPageSize = 20;
    public static String base = "http://localhost:8095";

    static ExecutorService tasks = Executors.newFixedThreadPool(10);

    public static HttpRequestBase getNextSessions(long id, LocalDate lastDate) throws Exception{
        URIBuilder builder = new URIBuilder(String.format("%s/api/chats/auth/findNextSessions/%s/%s", base, id, lastDate));
        HttpGet get = new HttpGet(builder.build());
        get.setHeader("Authorization", userPreference.get("jwt", null));

        return get;
    }

    public static HttpRequestBase sendOrder() throws Exception{
        String orderJson = mapper.writeValueAsString(orderManager.newOrder);

        StringEntity postEntity = new StringEntity(orderJson, "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost httpPost = new HttpPost(base + "/api/order/auth/create");
        httpPost.setHeader("Authorization", userPreference.get("jwt", null));
        httpPost.setEntity(postEntity);

        return httpPost;
    }

    public static HttpRequestBase login() throws URISyntaxException{
        Map<String, Object> jsonValues = new HashMap<>();
        jsonValues.put("username", loginManager.username.get());
        jsonValues.put("password", loginManager.password.get());
        JSONObject json = new JSONObject(jsonValues);

        StringEntity postEntity = new StringEntity(json.toString(), "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        URIBuilder builder = new URIBuilder(base + "/api/users/polling/login/" + pageSize);

        HttpPost httpPost = new HttpPost(builder.build());
        httpPost.setEntity(postEntity);

        return httpPost;
    }

    public static HttpRequestBase getLoggedUser() throws URISyntaxException{
        URIBuilder builder = new URIBuilder(base + "/api/users/polling/auth/getLoggedUser/" + pageSize);

        HttpGet httpGet = new HttpGet(builder.build());
        httpGet.setHeader("Authorization", userPreference.get("jwt", null));

        return httpGet;
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

    public static HttpRequestBase sendUserInfo(){
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

    public static HttpRequestBase sendMessage(String messageText, long chatId, long receiverId){
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

        return post;
    }

    public static HttpRequestBase longPollingRequest() throws Exception{
        String timeJson = mapper.writeValueAsString(loginManager.loggedUser.getLastCheck());

        StringEntity postEntity = new StringEntity(timeJson, "UTF8");
        postEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        HttpPost httpPost = new HttpPost( base + "/api/users/polling/auth/waitData");
        httpPost.setHeader("Authorization", userPreference.get("jwt", null));
        httpPost.setEntity(postEntity);

        return httpPost;
    }

    public static HttpRequestBase updateDishState(long orderId, long dishId){
        HttpPatch patch = new HttpPatch(String.format(base + "/api/order/auth/update/%d/%d", orderId, dishId));
        patch.setHeader("Authorization", userPreference.get("jwt", null));

        return patch;
    }

    public static String executeRequest(HttpRequestBase request) throws Exception{
        try(CloseableHttpResponse response = httpClient.execute(request)) {
            int responseCode = response.getStatusLine().getStatusCode();

            HttpEntity responseEntity = response.getEntity();
            String content = EntityUtils.toString(responseEntity);
            EntityUtils.consume(responseEntity);

            if (responseCode != 200) {
                if(responseCode == 422){
                    throw new UnprocessableEntityException(content);
                }

                throw new HttpException(content);
            }

            Header authHeader = response.getFirstHeader("Authorization");
            if(authHeader != null){
                userPreference.put("jwt", authHeader.getValue());
            }

            return content;
        }
    }
}
