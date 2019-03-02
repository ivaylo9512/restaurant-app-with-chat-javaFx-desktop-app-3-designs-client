package sample;

import Animations.ExpandOrderPane;
import Animations.ResizeMainChat;
import Models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;


public class ControllerLoggedFirstStyle {
    @FXML Label firstName, lastName, country, age, role;
    @FXML FlowPane ordersFlow;
    @FXML Pane contentPane;
    @FXML VBox mainChatBlock, chatUsers;
    @FXML ScrollPane menuScroll, userInfoScroll, chatUsersScroll, ordersScroll, mainChatScroll;
    @FXML AnchorPane contentRoot, mainChat;
    @FXML ImageView roleImage;
    @FXML TextArea mainChatTextArea;
    private User loggedUser;
    private ObjectMapper mapper = new ObjectMapper();
    private HashMap<Integer, ChatSpec> chatsMap = new HashMap<>();
    private CloseableHttpClient httpClient = LoginFirstStyle.httpClient;
    private Preferences userPreference = Preferences.userRoot();
    private Image userProfileImage;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private ChatSpec mainChatSpec;
    @FXML
    public void initialize() throws IOException {
        mapper.registerModule(new JavaTimeModule());
        String userJson = userPreference.get("user",null);
        loggedUser = mapper.readValue(userJson, User.class);
        InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
        userProfileImage = new Image(in);
        in.close();

        displayUserInfo();

        List<Order> orders = getOrders();
        appendOrders(orders);

        getChats();
//        appendMessages();

        manageSceneScrolls();

        ExpandOrderPane.scrollPane = ordersScroll;
        ExpandOrderPane.contentPane = contentPane;
        ExpandOrderPane.buttonExpandedProperty().addListener((observable, oldValue, newValue) -> {
            Button currentButton = ExpandOrderPane.button;
            if(newValue){
                currentButton.removeEventFilter(MouseEvent.MOUSE_CLICKED, expandOrderHandler);
                currentButton.addEventFilter(MouseEvent.MOUSE_CLICKED, reverseOrderHandler);
            }else{
                currentButton.removeEventFilter(MouseEvent.MOUSE_CLICKED, reverseOrderHandler);
                currentButton.addEventFilter(MouseEvent.MOUSE_CLICKED, expandOrderHandler);
            }
        });

        ResizeMainChat.resize(mainChat);
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
        fixBlurryContent(mainChatScroll);
        fixBlurryContent(ordersScroll);
        mainChatTextArea.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TextAreaSkin textAreaSkin= (TextAreaSkin) newValue;
                ScrollPane textAreaScroll = (ScrollPane) textAreaSkin.getChildren().get(0);
                fixBlurryContent(textAreaScroll);

            }
        });
        ordersScroll.setOnScroll(event -> {
            if(event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                FlowPane pane = (FlowPane) ordersScroll.getContent();
                ordersScroll.setHvalue(ordersScroll.getHvalue() - event.getDeltaY() / pane.getWidth());
            }
        });

        AnchorPane anchorPane = (AnchorPane) menuScroll.getContent();
        menuScroll.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {

                if (menuScroll.getHeight() <= 211) {
                    ScrollPane scrollPane;
                    if (menuScroll.getVvalue() == 0) {
                        scrollPane = userInfoScroll;
                    }else {
                        scrollPane = chatUsersScroll;
                    }

                    Pane content = (Pane) scrollPane.getContent();
                    scrollPane.setVvalue(scrollPane.getVvalue() - event.getDeltaY() / content.getHeight());
                    event.consume();
                }else{
                    chatUsersScroll.setDisable(true);
                    userInfoScroll.setDisable(false);

                    if(anchorPane.getHeight() <= menuScroll.getHeight()){
                        chatUsersScroll.setDisable(false);
                    }else if(menuScroll.getVvalue() == 1){
                        chatUsersScroll.setDisable(false);
                        userInfoScroll.setDisable(true);
                    }
                }

            }

        });
    }

    private void displayUserInfo(){
        firstName.setText(loggedUser.getFirstName());
        lastName.setText(loggedUser.getLastName());
        country.setText(loggedUser.getCountry());
        age.setText(String.valueOf(loggedUser.getAge()));
        role.setText(loggedUser.getRole());

        if (loggedUser.getRole().equals("chef")) {
            roleImage.setImage(new Image(getClass().getResourceAsStream("/chef-second.png")));
        }else{
            roleImage.setImage(new Image(getClass().getResourceAsStream("/waiter-second.png")));
        }
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
                    ChatSpec chatSpec;
                    Image profilePicture;
                    if(chat.getFirstUser().getId() == loggedUser.getId()){
                        in = new BufferedInputStream(
                        new URL(chat.getSecondUser().getProfilePicture()).openStream());
                        profilePicture = new Image(in);
                        chatSpec = new ChatSpec(chat.getId(), chat.getSecondUser().getId(), profilePicture);
                    }else{
                        in = new BufferedInputStream(
                                new URL(chat.getFirstUser().getProfilePicture()).openStream());
                        profilePicture = new Image(in);

                        chatSpec = new ChatSpec(chat.getId(), chat.getFirstUser().getId(), profilePicture);
                    }
                    in.close();
                    ImageView imageView = new ImageView(profilePicture);
                    imageView.setId(String.valueOf(chat.getId()));
                    imageView.setFitHeight(50);
                    imageView.setFitWidth(50);
                    imageView.setOnMouseClicked(this::setMainChat);
                    chatUsers.getChildren().add(imageView);
                    chatsMap.put(chat.getId(), chatSpec);

                }catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
    }
    private void setMainChat(MouseEvent event){
        ImageView imageView = (ImageView) event.getSource();
        int chatId = Integer.parseInt(imageView.getId());
        int pageSize = 3;
        ChatSpec chat = chatsMap.get(chatId);
//        int page = chat.getSessions().size() / perPage;
        System.out.println(imageView.getId());
        mainChatSpec = chat;
        List<Session> sessions = getNextSessions(chatId,0,pageSize);
        sessions.forEach(session -> {
            session.getMessages().forEach(message -> {
              appendMessage(message, chat);
            });
        });
    }
//    private void openChat(MouseEvent event){
//        ImageView imageView = (ImageView) event.getSource();
//        int chatId = Integer.parseInt(imageView.getId());
//        int pageSize = 3;
//        ChatSpec chat = chatsMap.get(chatId);
////        int page = chat.getSessions().size() / perPage;
//        System.out.println(imageView.getId());
//
//        List<Session> sessions = getNextSessions(chatId,0,pageSize);
//        sessions.forEach(session -> {
//            session.getMessages().forEach(this::appendMessage);
//        });
//    }

    private List<Session> getNextSessions(int id, int page, int pageSize){
        HttpGet get;
        List<Session> sessions = new ArrayList<>();
        try {

            URIBuilder builder = new URIBuilder("http://localhost:8080/api/auth/chat/nextSessions");
            builder
                    .setParameter("chatId", String.valueOf(id))
                    .setParameter("page", String.valueOf(page))
                    .setParameter("pageSize", String.valueOf(pageSize));
            get = new HttpGet(builder.build());
            get.setHeader("Authorization", userPreference.get("token", null));

            try(CloseableHttpResponse response = httpClient.execute(get)){

                int responseStatus = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity);

                if(responseStatus != 200){
                    EntityUtils.consume(entity);
                    throw new HttpException("Invalid response code: " + responseStatus  + ". With an error message: " + content);
                }
                sessions = mapper.readValue(content, new TypeReference<List<Session>>(){});

            } catch (IOException | HttpException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return sessions;
    }
    private void appendMessage(Message message, ChatSpec chat){
        HBox hBox = new HBox();
        VBox newBlock = new VBox();

        Text text = new Text();
        Text time = new Text();
        ImageView imageView = new ImageView();
        TextFlow textFlow = new TextFlow();

        time.getStyleClass().add("time");
        text.getStyleClass().add("message");
        imageView.getStyleClass().add("shadow");
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);
        HBox.setMargin(imageView,new Insets(-20,0,0,0));

        if(message.getReceiver() == loggedUser.getId()){
            imageView.setImage(chat.getSecondUserPicture());
            text.setText(message.getMessage());
            time.setText("  " + timeFormatter.format(message.getDate()));
            textFlow.getChildren().addAll(text, time);
            hBox.setAlignment(Pos.TOP_LEFT);

        }else{
            imageView.setImage(userProfileImage);
            text.setText(message.getMessage());
            time.setText(timeFormatter.format(message.getDate()) + "  ");
            textFlow.getChildren().addAll(time, text);
            hBox.setAlignment(Pos.TOP_RIGHT);

        }

        boolean timeElapsed;
        int timeToElapse = 10;

        List<Node> messageBlocks = mainChatBlock.getChildren();
        if(messageBlocks.size() > 0) {

            VBox lastBlock = (VBox) messageBlocks.get(messageBlocks.size() - 1);
            HBox lastMessage = (HBox) lastBlock.getChildren().get(lastBlock.getChildren().size() - 1);
            LocalTime lastBlockStartedDate;
            TextFlow firstTextFlow = (TextFlow)lastMessage.lookup("TextFlow");
            Text lastBlockStartedText = (Text)firstTextFlow.lookup(".time");
            lastBlockStartedDate = LocalTime.parse(lastBlockStartedText.getText().replaceAll("\\s+",""));

            timeElapsed = java.time.Duration.between(lastBlockStartedDate, message.getDate()).toMinutes() > timeToElapse;

            if(message.getReceiver() == loggedUser.getId()){
                if(!timeElapsed && lastMessage.getStyleClass().get(0).startsWith("second-user-message")){

                    hBox.getStyleClass().add("second-user-message");
                    hBox.getChildren().addAll(textFlow);
                    lastBlock.getChildren().add(hBox);

                }else{

                    hBox.getStyleClass().add("second-user-message-first");
                    hBox.getChildren().addAll(imageView, textFlow);
                    newBlock.getChildren().add(hBox);
                    mainChatBlock.getChildren().add(newBlock);

                }
            }else{
                if(!timeElapsed && lastMessage.getStyleClass().get(0).startsWith("user-message")){

                    hBox.getStyleClass().add("user-message");
                    hBox.getChildren().addAll(textFlow);
                    lastBlock.getChildren().add(hBox);

                }else{

                    hBox.getStyleClass().add("user-message-first");
                    hBox.getChildren().addAll(textFlow, imageView);
                    newBlock.getChildren().add(hBox);
                    mainChatBlock.getChildren().add(newBlock);

                }
            }
        }else{

            if(message.getReceiver() == loggedUser.getId()){
                hBox.getStyleClass().add("second-user-message-first");
                hBox.getChildren().addAll(imageView, textFlow);
                newBlock.getChildren().add(hBox);
            }else{
                hBox.getStyleClass().add("user-message-first");
                hBox.getChildren().addAll(textFlow, imageView);
                newBlock.getChildren().add(hBox);
            }
            mainChatBlock.getChildren().add(newBlock);
        }
//
//        if(message.getReceiver() == loggedUser.getId()){
//            text.setText(message.getMessage());
//            time.setText("  " + timeFormatter.format(message.getDate()));
//            textFlow.getChildren().addAll(text, time);
//
//            ImageView imageView2 = new ImageView(userProfileImage);
//            imageView2.getStyleClass().add("shadow");
//            imageView2.setFitWidth(40);
//            imageView2.setFitHeight(40);
//
//            hBox.getStyleClass().add("other-user-message-first");
//            HBox.setMargin(imageView2,new Insets(-20,0,0,0));
//            hBox.getChildren().addAll(imageView2, textFlow);
//            hBox.setAlignment(Pos.TOP_LEFT);
//
//        }else{
//            text.setText(message.getMessage());
//            time.setText(timeFormatter.format(message.getDate()) + "  ");
//            textFlow.getChildren().addAll(time, text);
//
//            ImageView imageView1 = new ImageView(userProfileImage);
//            imageView1.getStyleClass().add("shadow");
//            imageView1.setFitWidth(40);
//            imageView1.setFitHeight(40);
//
//            hBox.getStyleClass().add("user-message-first");
//            HBox.setMargin(imageView1,new Insets(-20,0,0,0));
//            hBox.getChildren().addAll(textFlow, imageView1);
//            hBox.setAlignment(Pos.TOP_RIGHT);
//        }
//        mainChatBlock.getChildren().addAll(hBox);

    }
    @FXML
    private void scrollToChats(){
        Animation animation = new Timeline(
            new KeyFrame(Duration.millis(1000), new KeyValue(
                    menuScroll.vvalueProperty(), 1)));
        animation.play();
    }
    @FXML
    private void scrollToProfile(){
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(1000), new KeyValue(
                        menuScroll.vvalueProperty(), 0)));
        animation.play();
    }

    private void appendMessages(){
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
        ImageView imageView = new ImageView(userProfileImage);
        imageView.getStyleClass().add("shadow");
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);

        HBox hBox = new HBox();
        hBox.getStyleClass().add("user-message-first");
        HBox.setMargin(imageView,new Insets(-20,0,0,0));
        hBox.getChildren().addAll(textFlow, imageView);
        hBox.setAlignment(Pos.TOP_RIGHT);

        TextFlow textFlow2 = new TextFlow();
        Text text2 = new Text();
        Text time2 = new Text();
        text2.setText("Hello2.0");
        text2.getStyleClass().add("message");
        time2.getStyleClass().add("time");
        time2.setText("12:00  ");
        textFlow2.getChildren().addAll(time2, text2);

        ImageView imageView1 = new ImageView(userProfileImage);
        imageView1.getStyleClass().add("shadow");
        imageView1.setFitWidth(40);
        imageView1.setFitHeight(40);

        HBox hBox2 = new HBox();
        hBox2.getStyleClass().add("user-message-first");
        HBox.setMargin(imageView1,new Insets(-20,0,0,0));
        hBox2.getChildren().addAll(textFlow2, imageView1);
        hBox2.setAlignment(Pos.TOP_RIGHT);

        TextFlow textFlow1 = new TextFlow();
        Text text1 = new Text();
        Text time1 = new Text();
        text1.setText("Hello2.0");
        text1.getStyleClass().add("message");
        time1.getStyleClass().add("time");
        time1.setText("12:00  ");
        textFlow1.getChildren().addAll(time1, text1);

        HBox hBox1 = new HBox();
        hBox1.getStyleClass().add("user-message");
        hBox1.getChildren().addAll(textFlow1);
        hBox1.setAlignment(Pos.TOP_RIGHT);

        TextFlow textFlow3 = new TextFlow();
        Text text3 = new Text();
        Text time3 = new Text();
        text3.setText("Hello2.0");
        text3.getStyleClass().add("message");
        time3.getStyleClass().add("time");
        time3.setText("13:10  ");
        textFlow3.getChildren().addAll(text3, time3);

        ImageView imageView2 = new ImageView(userProfileImage);
        imageView2.getStyleClass().add("shadow");
        imageView2.setFitWidth(40);
        imageView2.setFitHeight(40);

        HBox hBox3 = new HBox();
        hBox3.getStyleClass().add("other-user-message-first");
        HBox.setMargin(imageView2,new Insets(-20,0,0,0));
        hBox3.getChildren().addAll(imageView2, textFlow3);
        hBox3.setAlignment(Pos.TOP_LEFT);

        TextFlow textFlow4 = new TextFlow();
        Text text4 = new Text();
        Text time4 = new Text();
        text4.setText("Hello2.0");
        text4.getStyleClass().add("message");
        time4.getStyleClass().add("time");
        time4.setText("13:40  ");
        textFlow4.getChildren().addAll(text4, time4);
        HBox hBox4 = new HBox();
        hBox4.getStyleClass().add("other-user-message");
        hBox4.getChildren().addAll(textFlow4);
        hBox4.setAlignment(Pos.TOP_LEFT);

        VBox lastVBox = new VBox(hBox, hBox1, hBox2);
        VBox currentVBox = new VBox(hBox3,hBox4);
        mainChatBlock.getChildren().addAll(lastVBox, currentVBox);
        System.out.println(mainChatBlock.getChildren().size());
        VBox lastMessageBlock = (VBox) mainChatBlock.getChildren().get(mainChatBlock.getChildren().size() - 1);
        HBox firstMessageBox = (HBox) lastMessageBlock.getChildren().get(0);
        TextFlow firstTextFlow = (TextFlow) firstMessageBox.getChildren().get(1);
        Text firstTime = (Text) firstTextFlow.getChildren().get(1);
        System.out.println(firstTime.getText());

        LocalTime localTime = LocalTime.of(13,10);
        LocalTime localTime1 = LocalTime.of(13, 30);
        System.out.println(localTime1.compareTo(localTime));
        System.out.println(java.time.Duration.between(localTime,localTime1).toMinutes());

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
            button.addEventFilter(MouseEvent.MOUSE_CLICKED, expandOrderHandler);

            Label label = new Label(String.valueOf(order.getId()));
            label.setLayoutX(28);
            label.setLayoutY(11);
            label.setDisable(true);

            Pane orderContainer = new Pane();
            orderContainer.getStyleClass().add("order-container");
            orderContainer.getChildren().add(orderPane);
            orderPane.getChildren().add(button);
            orderPane.getChildren().add(label);

            ordersFlow.getChildren().add(orderContainer);
        });
    }

    private EventHandler expandOrderHandler = (EventHandler<MouseEvent>) this::expandOrder;
    private EventHandler reverseOrderHandler = (EventHandler<MouseEvent>)e-> ExpandOrderPane.reverseOrder();

    @FXML
    public void expandOrder(MouseEvent event){
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        if(!ExpandOrderPane.action && (intersectedNode.getTypeSelector().equals("Button")
                ||intersectedNode.getStyleClass().get(0).equals("order"))){

            ExpandOrderPane.setCurrentOrder(event);

        }
    }
}
