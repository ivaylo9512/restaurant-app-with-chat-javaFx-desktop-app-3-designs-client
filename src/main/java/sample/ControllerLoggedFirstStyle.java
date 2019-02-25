package sample;

import Animations.ExpandOrderPane;
import Models.Order;
import Models.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;


public class ControllerLoggedFirstStyle {
    @FXML Label firstName, lastName, country, age, role;
    @FXML ScrollPane scroll;
    @FXML FlowPane flowPane;
    @FXML Pane contentPain;
    @FXML VBox vbox;
    private CloseableHttpClient httpClient = LoginFirstStyle.httpClient;
    private Preferences userPreference = Preferences.userRoot();
    @FXML
    public void initialize(){
        Gson gson = new Gson();
        String userJson = userPreference.get("user",null);
        User user = gson.fromJson(userJson, User.class);

        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        country.setText(user.getCountry());
        age.setText(String.valueOf(user.getAge()));
        role.setText(user.getRole());

        List<Order> orders = getOrders();
        appendOrders(orders);

        scroll.setOnScroll(event -> {
            if(event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                FlowPane pane = (FlowPane) scroll.getContent();
                scroll.setHvalue(scroll.getHvalue() - event.getDeltaY() / pane.getWidth());
                System.out.println((pane.getWidth() - scroll.getWidth()) * scroll.getHvalue());
            }
        });
    }
    private void appendMessages(){
        HBox hBox = new HBox();
        hBox.getStyleClass().add("user-message");
        TextFlow textFlow = new TextFlow();
        Text text = new Text();
        Text time = new Text();
        text.setText("Hello2.0");
        time.setText("12:00");
        textFlow.getChildren().addAll(time, text);
        ImageView imageView = new ImageView();
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
            System.out.println(content);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            orders = mapper.readValue(content, new TypeReference<List<Order>>(){});
            EntityUtils.consume(receivedEntity);
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private void appendOrders(List<Order> orders) {
        orders.forEach(order -> {
            Pane orderContainer = new Pane();
            orderContainer.getStyleClass().add("order-container");

            Pane orderPane = new Pane();
            orderPane.setLayoutX(20.6);
            orderPane.setLayoutY(51.0);
            orderPane.getStyleClass().add("order");
            orderContainer.getChildren().add(orderPane);

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
            orderPane.getChildren().add(button);

            Label label = new Label(String.valueOf(order.getId()));
            label.setLayoutX(28);
            label.setLayoutY(11);
            orderPane.getChildren().add(label);

            flowPane.getChildren().add(orderContainer);


        });
//            LocalDate localDate = LocalDate.from(orderSerilized[0].getCreated());
    }

    @FXML
    public void expandOrder(MouseEvent event){
        Node intersectedNode = event.getPickResult().getIntersectedNode();

        if(intersectedNode.getTypeSelector().equals("Pane") && intersectedNode.getStyleClass().get(0).equals("order")) {

            Pane order = (Pane) intersectedNode;
            Pane orderContainer = (Pane) event.getPickResult().getIntersectedNode().getParent();
            FlowPane pane = (FlowPane) scroll.getContent();

            double translateX = order.getLayoutX() + order.getParent().getLayoutX();
            double translateY = order.getLayoutX() + order.getParent().getLayoutY();
            double scrolledAmount = (pane.getWidth() - scroll.getWidth()) * scroll.getHvalue();

            order.setStyle("-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,1.6) , 4, 0.0 , 0 , 0 )");
            contentPain.getChildren().add(order);

            ExpandOrderPane.expandPane(order, orderContainer, scrolledAmount, event, translateX, translateY, scroll);

        }
    }
}
