package sample;

import Animations.ExpandOrderPane;
import Animations.ResizeMainChat;
import Helpers.OrderService;
import Helpers.Scrolls;
import Models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
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
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

import static Helpers.Reversed.reversed;

public class ControllerLoggedFirstStyle {
    @FXML Label firstName, lastName, country, age, role;
    @FXML FlowPane ordersFlow;
    @FXML Pane contentPane;
    @FXML VBox mainChatBlock, chatUsers;
    @FXML ScrollPane menuScroll, userInfoScroll, chatUsersScroll, ordersScroll, mainChatScroll;
    @FXML AnchorPane contentRoot, mainChat;
    @FXML ImageView roleImage;
    @FXML TextArea mainChatTextArea;

    public static ObjectMapper mapper = new ObjectMapper();
    public static Preferences userPreference = Preferences.userRoot();
    public static LocalDateTime mostRecentOrderDate;

    private User loggedUser;
    private HashMap<Integer, ChatValue> chatsMap = new HashMap<>();
    private CloseableHttpClient httpClient = LoginFirstStyle.httpClient;
    private Image userProfileImage;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
    private ChatValue mainChatValue;
    private int pageSize = 3;

    @FXML
    public void initialize() throws IOException {
        mapper.registerModule(new JavaTimeModule());
        String userJson = userPreference.get("user",null);
        loggedUser = mapper.readValue(userJson, User.class);
        InputStream in = new BufferedInputStream(new URL(loggedUser.getProfilePicture()).openStream());
        userProfileImage = new Image(in);
        in.close();

//        getChats();
        displayUserInfo();

//        List<Order> orders = getOrders();
//        reversed(orders).forEach(this::appendOrder);
//        mostRecentOrderDate = getMostRecentOrderDate();

        OrderService orderService = new OrderService();
        orderService.start();
        orderService.setOnSucceeded(event -> {
            List<Order> newOrders = ((List<Order>) orderService.getValue());
            if(newOrders.size() > 0){

                Order mostRecentNewOrder = newOrders.get(0);
                if(mostRecentNewOrder.getCreated().isAfter(mostRecentNewOrder.getUpdated())){
                    mostRecentOrderDate = mostRecentNewOrder.getCreated();
                }else{
                    mostRecentOrderDate = mostRecentNewOrder.getUpdated();
                }

            }
            newOrders.forEach(order -> {
                System.out.println(order.getId());

                AnchorPane orderPane = (AnchorPane) contentPane.lookup("#" + order.getId());

                if(orderPane != null){
                    order.getDishes().forEach(dish -> {

                        Label ready = (Label) contentPane.lookup("#dish" + dish.getId());
                        if(dish.getReady()){
                            ready.setText("O");
                        }else{
                            ready.setText("X");
                        }

                    });
                }else{
                    appendOrder(order);
                }
            });
            orderService.restart();
        });

        Scrolls scrolls = new Scrolls(menuScroll, userInfoScroll, chatUsersScroll, ordersScroll, mainChatScroll,mainChatTextArea);

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

        mainChatBlock.idProperty().addListener((observable1, oldValue1, newValue1) -> {
            if (newValue1.equals("append")){
                loadOlderHistory(mainChatValue, mainChatBlock);
            }
        });

        ResizeMainChat.addListeners(mainChat);
    }

    private LocalDateTime getMostRecentOrderDate() {
        HttpGet get = new HttpGet("http://localhost:8080/api/auth/order/getMostRecentDate");
        LocalDateTime localDateTime = LocalDateTime.now();
        get.setHeader("Authorization", userPreference.get("token", null));

        try(CloseableHttpResponse response = httpClient.execute(get)) {

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

    private void loadOlderHistory(ChatValue chatValue, VBox chatBlock) {
        int displayedSessions = mainChatValue.getDisplayedSessions();
        int loadedSessions = mainChatValue.getSessions().size();
        int nextPage = loadedSessions / pageSize;

        HBox sessionInfo = (HBox) chatBlock.getChildren().get(0);
        Text info = (Text) sessionInfo.lookup("Text");

        LinkedHashMap<LocalDate, Session> sessionsMap = chatValue.getSessions();
        List<Session> chatSessions = new ArrayList<>(sessionsMap.values());
        List<Session> nextSessions;
        if(loadedSessions > displayedSessions){

            nextSessions = chatSessions.subList(displayedSessions,
                    Math.min(displayedSessions + pageSize, loadedSessions));

            if(displayedSessions + nextSessions.size() == loadedSessions && !mainChatValue.isMoreSessions()){
                info.setText("Beginning of the chat");
            }
            mainChatValue.setDisplayedSessions(displayedSessions + nextSessions.size());

            nextSessions.forEach(session -> {
                appendSession(session, mainChatBlock, mainChatValue);
        });

        }else if(mainChatValue.isMoreSessions()){
            nextSessions = getNextSessions(mainChatValue.getChatId(), nextPage, pageSize);
            if(nextSessions.size() < pageSize){
                mainChatValue.setMoreSessions(false);
                info.setText("Beginning of the chat");
            }
            mainChatValue.setDisplayedSessions(displayedSessions + nextSessions.size());
            nextSessions.forEach(session -> {

                if(!sessionsMap.containsKey(session.getDate())) {
                    sessionsMap.put(session.getDate(), session);

                    appendSession(session, mainChatBlock, mainChatValue);
                }

            });
            mainChatValue.setSessions(sessionsMap);
        }else{
            info.setText("Beginning of the chat");
        }
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
            EntityUtils.consume(entity);

            chats.forEach(chat -> {

                try {
                    InputStream in;
                    ChatValue chatValue;
                    Image profilePicture;
                    if(chat.getFirstUser().getId() == loggedUser.getId()){
                        in = new BufferedInputStream(
                        new URL(chat.getSecondUser().getProfilePicture()).openStream());

                        profilePicture = new Image(in);
                        chatValue = new ChatValue(chat.getId(), chat.getSecondUser().getId(), profilePicture);
                    }else{
                        in = new BufferedInputStream(
                                new URL(chat.getFirstUser().getProfilePicture()).openStream());
                        profilePicture = new Image(in);

                        chatValue = new ChatValue(chat.getId(), chat.getFirstUser().getId(), profilePicture);
                    }
                    in.close();

                    ImageView imageView = new ImageView(profilePicture);
                    imageView.setId(String.valueOf(chat.getId()));
                    imageView.setFitHeight(50);
                    imageView.setFitWidth(50);
                    imageView.setOnMouseClicked(this::setMainChat);

                    chatUsers.getChildren().add(imageView);
                    chatsMap.put(chat.getId(), chatValue);

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
        int page = 0;

        ChatValue chat = chatsMap.get(chatId);
        HBox sessionInfo = (HBox) mainChatBlock.getChildren().get(0);
        Text info = (Text) sessionInfo.lookup("Text");

        if(mainChatValue != null && chatId == mainChatValue.getChatId()){
            if(mainChat.isDisabled()){
                mainChat.setDisable(false);
                mainChat.setOpacity(1);
            }else{
                mainChat.setOpacity(0);
                mainChat.setDisable(true);
            }
        }else{
            mainChatBlock.setId("beginning");
            mainChatBlock.getChildren().remove(1,mainChatBlock.getChildren().size());
            mainChat.setDisable(false);
            mainChat.setOpacity(0);

            Timeline opacity = new Timeline(new KeyFrame(Duration.millis(200), event1 -> mainChat.setOpacity(1)));
            opacity.play();

            mainChatValue = chat;

            LinkedHashMap<LocalDate, Session> sessionsMap = mainChatValue.getSessions();
            List<Session> chatSessions = new ArrayList<>(sessionsMap.values());

            if(chatSessions.size() == 0) {

                List<Session> sessions = getNextSessions(chatId, page, pageSize);
                if(sessions.size() < pageSize){
                    mainChatValue.setMoreSessions(false);
                    mainChatValue.setDisplayedSessions(sessions.size());
                    info.setText("Beginning of chat");
                }else{
                    mainChatValue.setDisplayedSessions(pageSize);
                    info.setText("Scroll for more history");
                }

                sessions.forEach(session -> {
                    sessionsMap.put(session.getDate(), session);
                    appendSession(session, mainChatBlock, mainChatValue);

                });
                mainChatValue.setSessions(sessionsMap);
            }else{
                List<Session> lastSessions = chatSessions.subList(0, Math.min(pageSize, chatSessions.size()));

                if(lastSessions.size() == pageSize){
                    info.setText("Scroll for more history");
                    mainChatValue.setDisplayedSessions(pageSize);
                }else{
                    info.setText("Beginning of the chat");
                    mainChatValue.setDisplayedSessions(lastSessions.size());
                }

                lastSessions.forEach(session -> {
                    appendSession(session, mainChatBlock, mainChatValue);
                });
            }
        }
//        mainChatBlock.heightProperty().addListener((observable, oldValue, newValue) -> {
//            mainChatScroll.setVvalue(1);
//        });
    }

    private void appendSession(Session session, VBox chatBlock, ChatValue chatValue) {

        Text date = new Text(dateFormatter.format(session.getDate()));
        TextFlow dateFlow = new TextFlow(date);
        dateFlow.setTextAlignment(TextAlignment.CENTER);

        HBox sessionDate = new HBox(dateFlow);
        HBox.setHgrow(dateFlow, Priority.ALWAYS);
        sessionDate.getStyleClass().add("session-date");

        VBox sessionBlock = new VBox(sessionDate);
        sessionBlock.setId(session.getDate().toString());
        session.getMessages()
                .forEach(message -> appendMessage(message, chatValue, sessionBlock));
        chatBlock.getChildren().add(1, sessionBlock);
    }
//    private void openChat(MouseEvent event){
//        ImageView imageView = (ImageView) event.getSource();
//        int chatId = Integer.parseInt(imageView.getId());
//        int pageSize = 3;
//        ChatValue chat = chatsMap.get(chatId);
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
        List<Session> sessions = new LinkedList<>();
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
                EntityUtils.consume(entity);

            } catch (IOException | HttpException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return sessions;
    }

    private void appendMessage(Message message, ChatValue chat, VBox chatBlock){
        HBox hBox = new HBox();
        VBox newBlock = new VBox();
        Text text = new Text();
        Text time = new Text();
        ImageView imageView = new ImageView();
        TextFlow textFlow = new TextFlow();

        time.getStyleClass().add("time");
        text.getStyleClass().add("message");
        imageView.getStyleClass().add("shadow");
        newBlock.getStyleClass().add("chat-block");
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

        List<Node> messageBlocks = chatBlock.getChildren();
        if(messageBlocks.size() > 0 && messageBlocks.get(messageBlocks.size() - 1).getTypeSelector().equals("VBox")) {
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
                    chatBlock.getChildren().add(newBlock);

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
                    chatBlock.getChildren().add(newBlock);

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
            chatBlock.getChildren().add(newBlock);
        }
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

    private List<Order> getOrders(){
        List<Order> orders = new ArrayList<>();
        HttpGet httpGet = new HttpGet("http://localhost:8080/api/auth/order/findAllNotReady");
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

    private void appendOrder(Order order) {
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

            VBox dishesBox = new VBox();
            order.getDishes().forEach(dish -> {
                Label amount = new Label("3");
                amount.getStyleClass().add("amount");
                Label ready;
                if(dish.getReady()){
                    ready = new Label("O");
                }else{
                    ready = new Label("X");
                }

                ready.setId("dish" + dish.getId());
                ready.getStyleClass().add("ready");
                TextField name = new TextField(dish.getName());
                name.getStyleClass().add("name");
                HBox dishBox = new HBox(amount, name, ready);
                dishBox.getStyleClass().add("dish");

                amount.setViewOrder(1);
                name.setViewOrder(3);
                HBox.setHgrow(name, Priority.ALWAYS);
                dishesBox.getChildren().add(dishBox);
            });

            ScrollPane dishesScroll = new ScrollPane(dishesBox);
            dishesScroll.setMinHeight(0);
            dishesScroll.setMinWidth(0);
            dishesScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            AnchorPane dishesAnchor = new AnchorPane(dishesScroll);

            AnchorPane orderPane = new AnchorPane(dishesAnchor, button, label);
            orderPane.setId(String.valueOf(order.getId()));
            orderPane.setLayoutX(20.6);
            orderPane.setLayoutY(51.0);
            orderPane.getStyleClass().add("order");

            Pane orderContainer = new Pane(orderPane);
            orderContainer.getStyleClass().add("order-container");

            AnchorPane.setLeftAnchor(dishesScroll, 0.0);
            AnchorPane.setRightAnchor(dishesScroll, -14.4);
            AnchorPane.setBottomAnchor(dishesScroll, 0.0);
            AnchorPane.setTopAnchor(dishesScroll, 0.0);

            AnchorPane.setLeftAnchor(label, 28.0);
            AnchorPane.setRightAnchor(label, 28.0);
            AnchorPane.setLeftAnchor(dishesAnchor, 28.0);
            AnchorPane.setRightAnchor(dishesAnchor, 28.0);
            AnchorPane.setTopAnchor(dishesAnchor, 41.0);

            dishesAnchor.prefHeightProperty().bind(orderPane.prefHeightProperty().subtract(105));
            dishesBox.prefWidthProperty().bind(dishesScroll.widthProperty().subtract(15));


            dishesScroll.skinProperty().addListener((observable, oldValue, newValue) -> {
                ScrollBar bar = Scrolls.findVerticalScrollBar(dishesScroll);
                Objects.requireNonNull(bar).addEventFilter(MouseEvent.MOUSE_DRAGGED, Event::consume);
                Objects.requireNonNull(bar).addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
            });

            button.prefWidthProperty().bind(((orderPane.prefWidthProperty()
                    .subtract(81.6))
                    .divide(15))
                    .add(28));
            button.prefHeightProperty().bind(((orderPane.prefHeightProperty()
                    .subtract(81.6))
                    .divide(30))
                    .add(28));

            dishesAnchor.setDisable(true);
            ordersFlow.getChildren().add(0, orderContainer);
        }

    private EventHandler expandOrderHandler = (EventHandler<MouseEvent>) this::expandOrder;
    private EventHandler reverseOrderHandler = (EventHandler<MouseEvent>)e-> ExpandOrderPane.reverseOrder();

    @FXML
    public void expandOrder(MouseEvent event){
        Node intersectedNode = event.getPickResult().getIntersectedNode();
        if(!ExpandOrderPane.action && (intersectedNode.getTypeSelector().equals("Button")
                ||(!intersectedNode.getTypeSelector().equals("ScrollPaneSkin$6") && intersectedNode.getStyleClass().get(0).equals("order")))){

            ExpandOrderPane.setCurrentOrder(event);

        }
    }
}