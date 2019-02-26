package sample;

import Animations.ExpandOrderPane;
import Animations.ResizeHeight;
import Animations.ResizeWidth;
import Models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.*;
import javafx.animation.*;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;


public class ControllerLoggedFirstStyle {
    @FXML Label firstName, lastName, country, age, role;
    @FXML FlowPane ordersFlow;
    @FXML Pane contentPain;
    @FXML VBox vbox, chatUsers;
    @FXML ScrollPane menuScroll, userInfoScroll, chatUsersScroll, ordersScroll;
    @FXML Button chatButton;
    @FXML ScrollPane scroll2;
    @FXML AnchorPane pane1;

    private User loggedUser;
    private ObjectMapper mapper = new ObjectMapper();
    private HashMap<ChatKey, List<Session>> chatsMap = new HashMap<>();
    private CloseableHttpClient httpClient = LoginFirstStyle.httpClient;
    private Preferences userPreference = Preferences.userRoot();
    @FXML
    public void initialize() throws IOException {
        mapper.registerModule(new JavaTimeModule());
        String userJson = userPreference.get("user",null);
        loggedUser = mapper.readValue(userJson, User.class);
        displayUserInfo();

        List<Order> orders = getOrders();
        appendOrders(orders);

        getChats();
        appendMessages();

        manageSceneScrolls();

        scroll2.setOnMouseClicked(event -> {
            AnchorPane pane = (AnchorPane) scroll2.getParent();
            VBox vbox = (VBox) scroll2.getContent();
            pane.setLayoutY(pane.getLayoutY() -3);
            scroll2.setPrefHeight(scroll2.getPrefHeight() + 3);
            scroll2.maxHeightProperty().bind(vbox.heightProperty());
        });
    }

    private void fixBlurryContent(ScrollPane scrollPane){
        scrollPane.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                scrollPane.getChildrenUnmodifiable().get(0).setCache(false);
            }
        });
    }

    private void manageSceneScrolls() {
        fixBlurryContent(menuScroll);
        fixBlurryContent(userInfoScroll);
        fixBlurryContent(chatUsersScroll);
        fixBlurryContent(scroll2);
        fixBlurryContent(ordersScroll);

        ordersScroll.setOnScroll(event -> {
            if(event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                FlowPane pane = (FlowPane) ordersScroll.getContent();
                ordersScroll.setHvalue(ordersScroll.getHvalue() - event.getDeltaY() / pane.getWidth());
                System.out.println((pane.getWidth() - ordersScroll.getWidth()) * ordersScroll.getHvalue());
            }
        });

        AnchorPane anchorPane = (AnchorPane) menuScroll.getContent();
//        chatUsersScroll.setDisable(true);
        menuScroll.addEventFilter(ScrollEvent.SCROLL, event -> {
//            System.out.println(menuScroll.getVvalue());
//            if(menuScroll.getVvalue() == 1){
//                chatUsersScroll.setDisable(false);
//                userInfoScroll.setDisable(true);
//            }
//            if(menuScroll.getVvalue() < 1){
//                chatUsersScroll.setDisable(true);
//                userInfoScroll.setDisable(false);
//            }
//            if(anchorPane.getHeight() <= menuScroll.getHeight()){
//                chatUsersScroll.setDisable(false);
//            }
            if (event.getDeltaY() != 0) {
                FlowPane pane = (FlowPane) userInfoScroll.getContent();
                userInfoScroll.setVvalue(userInfoScroll.getVvalue() - event.getDeltaY() / pane.getHeight());
                event.consume();
            }
        });
    }

    private void displayUserInfo(){
        firstName.setText(loggedUser.getFirstName());
        lastName.setText(loggedUser.getLastName());
        country.setText(loggedUser.getCountry());
        age.setText(String.valueOf(loggedUser.getAge()));
        role.setText(loggedUser.getRole());
    }
    private void getChats(){
        HttpGet get = new HttpGet("http://localhost:8080/api/auth/chat/getChats");
        get.setHeader("Authorization", userPreference.get("token", null));

        try(CloseableHttpResponse response = httpClient.execute(get)) {

            int responseStatus = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);

            if(responseStatus != 200){
                EntityUtils.consume(entity);
                throw new HttpException("Invalid response code: " + responseStatus + ". With an error message: " + content);
            }

            List<Chat> chats = mapper.readValue(content, new TypeReference<List<Chat>>(){});

            chats.forEach(chat -> {

                try {
                    InputStream in;
                    ChatKey chatKey;
                    Image profilePicture;
                    if(chat.getFirstUser().getId() == loggedUser.getId()){
                        in = new BufferedInputStream(
                        new URL(chat.getSecondUser().getProfilePicture()).openStream());
                        profilePicture = new Image(in);
                        chatKey = new ChatKey(chat.getId(), chat.getSecondUser().getId(), profilePicture);
                    }else{
                        in = new BufferedInputStream(
                                new URL(chat.getFirstUser().getProfilePicture()).openStream());
                        profilePicture = new Image(in);

                        chatKey = new ChatKey(chat.getId(), chat.getFirstUser().getId(), profilePicture);
                    }
                    in.close();
                    ImageView imageView = new ImageView(profilePicture);
                    imageView.setFitHeight(50);
                    imageView.setFitWidth(50);
                    imageView.getStyleClass().add("shadow");
                    chatUsers.getChildren().add(imageView);
                    chatsMap.put(chatKey, new ArrayList<>());

                }catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void scrollToChats(MouseEvent e){
        Animation animation = new Timeline(
            new KeyFrame(Duration.millis(1000), new KeyValue(
                    menuScroll.vvalueProperty(), 1)));
        animation.play();
    }

    private void appendMessages(){
        HBox hBox = new HBox();
        hBox.getStyleClass().add("user-message");
        TextFlow textFlow = new TextFlow();
        Text text = new Text();
        Text time = new Text();
        text.setText("Hello2.0");
        text.getStyleClass().add("message");
        time.getStyleClass().add("time");
        time.setText("12:00  ");
        textFlow.getChildren().addAll(time, text);
        InputStream in = null;
        try {
            in = new BufferedInputStream(new URL("http://localhost:8080/images/download/user_2.png").openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageView imageView = new ImageView(new Image(in));

        imageView.getStyleClass().add("shadow");
        HBox.setMargin(imageView,new Insets(-20,0,0,0));
        hBox.getChildren().addAll(textFlow, imageView);
        hBox.setAlignment(Pos.TOP_RIGHT);
        vbox.getChildren().add(hBox);

    }
    private List<Order> getOrders(){
        List<Order> orders = new ArrayList<>();
        HttpGet httpGet = new HttpGet("http://localhost:8080/api/auth/order/findAll");
        httpGet.setHeader("Authorization", userPreference.get("token", null));
        try(CloseableHttpResponse response = httpClient.execute(httpGet)) {

            int responseStatus = response.getStatusLine().getStatusCode();
            HttpEntity receivedEntity = response.getEntity();
            String content = EntityUtils.toString(receivedEntity);

            if(responseStatus != 200){
                EntityUtils.consume(receivedEntity);
                throw new HttpException("Invalid response code: " + responseStatus + ". With an error message: " + content);
            }

            orders = mapper.readValue(content, new TypeReference<List<Order>>(){});

            EntityUtils.consume(receivedEntity);
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private void appendOrders(List<Order> orders) {
        orders.forEach(order -> {

            Pane orderPane = new Pane();
            orderPane.setLayoutX(20.6);
            orderPane.setLayoutY(51.0);
            orderPane.getStyleClass().add("order");

            Image clout = new Image(getClass().getResourceAsStream("/cloud-down.png"));
            ImageView imageView = new ImageView(clout);
            imageView.setFitWidth(15);
            imageView.setFitHeight(15);
            imageView.fitWidthProperty().setValue(15);
            imageView.fitHeightProperty().setValue(15);

            Button button = new Button("", imageView);
            button.setLayoutX(29);
            button.setLayoutY(48);
            button.setTranslateX(0);
            button.setTranslateY(0);
            button.setPrefWidth(28);
            button.setPrefHeight(28);
            button.setMinWidth(28);
            button.setMinHeight(28);
            button.addEventFilter(MouseEvent.MOUSE_CLICKED, this::expandOrder);

            Label label = new Label(String.valueOf(order.getId()));
            label.setLayoutX(28);
            label.setLayoutY(11);

            Pane orderContainer = new Pane();
            orderContainer.getStyleClass().add("order-container");
            orderContainer.getChildren().add(orderPane);
            orderPane.getChildren().add(button);
            orderPane.getChildren().add(label);

            ordersFlow.getChildren().add(orderContainer);
        });
//            LocalDate localDate = LocalDate.from(orderSerilized[0].getCreated());
    }

    @FXML
    public void expandOrder(MouseEvent event){
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        System.out.println(intersectedNode.getTypeSelector());
        System.out.println(intersectedNode.getTypeSelector());

        if(intersectedNode.getTypeSelector().equals("Pane") &&
                intersectedNode.getStyleClass().get(0).equals("order") && !ExpandOrderPane.action) {
            Pane order = (Pane) intersectedNode;
            Pane orderContainer = (Pane) intersectedNode.getParent();

            double translateX = order.getLayoutX() + order.getParent().getLayoutX();
            double translateY = order.getLayoutX() + order.getParent().getLayoutY();
            double scrolledAmount = (ordersFlow.getWidth() - ordersScroll.getWidth()) * ordersScroll.getHvalue();

            order.setStyle("-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,1.6) , 4, 0.0 , 0 , 0 )");
            contentPain.getChildren().add(order);

            ExpandOrderPane.action = true;
            ExpandOrderPane.expandPane(order, orderContainer, scrolledAmount, event,
                    translateX, translateY,intersectedNode, ordersScroll);
        }

        if(intersectedNode.getTypeSelector().equals("Button") && !ExpandOrderPane.action){
            Pane order = (Pane) intersectedNode.getParent();
            Pane orderContainer = (Pane) intersectedNode.getParent();
            double translateX = order.getLayoutX() + order.getParent().getLayoutX();
            double translateY = order.getLayoutX() + order.getParent().getLayoutY();
            double scrolledAmount = (ordersFlow.getWidth() - ordersScroll.getWidth()) * ordersScroll.getHvalue();

            order.setStyle("-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,1.6) , 4, 0.0 , 0 , 0 )");
            order.setLayoutX(translateX- scrolledAmount);
            contentPain.getChildren().add(order);

            ExpandOrderPane.action = true;
            ExpandOrderPane.expandPane(order, orderContainer, scrolledAmount, event,
                    translateX, translateY,intersectedNode, ordersScroll);

        }
    }
}
