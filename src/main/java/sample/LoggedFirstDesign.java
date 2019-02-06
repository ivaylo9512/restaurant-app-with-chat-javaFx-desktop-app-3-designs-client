package sample;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoggedFirstDesign extends LoginFirstDesign {
    private static Boolean update;
    public static void displayLoggedScene(Double stageX, Double stageY) throws IOException {
        Pane root = new FXMLLoader(LoggedFirstDesign.class.getResource("/logged-first.fxml")).load();
        Stage stage = new Stage();
        stage.setTitle("Restaurant");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setX(stageX);
        stage.setY(stageY);
        AnchorPane header = new AnchorPane();
        header.setMinHeight(100);
        header.setMinWidth(600);
        header.setStyle("-fx-background-color:#A92232");


//        root.setStyle("-fx-background-color: rgba(0, 100, 100, 0); -fx-background-radius: 10;");
//        layout5.setStyle("-fx-background-color: rgba(0, 100, 100, 0); -fx-background-radius: 10;");
        Scene scene2 = new Scene(root);
        scene2.setFill(Color.TRANSPARENT);
        String string = "/sample";
        scene2.getStylesheets().add(LoggedFirstDesign.class.getResource("/logged-first.css").toString());
        stage.setScene(scene2);
        stage.show();
//        MoveStage.moveStage(root,pane);

        ///
//        String Message = "password.";
//                HttpGet httpGet = new HttpGet("http://localhost:8080/api/order/findById/" + 2);
//                try(CloseableHttpResponse response1 = httpClient.execute(httpGet)){
//                    System.out.println(response1.getStatusLine());
//                    HttpEntity entity1 = response1.getEntity();
//                    String content = EntityUtils.toString(entity1);
//                    Gson gson = new Gson();
//                    Order order = gson.fromJson(content, Order.class);
//                    Double x = 200.00;
//                    for (Dish dish: order.getDishes()) {
//                        Button choseButton = new Button("Choose");
//                        choseButton.setLayoutX(x);
//                        x += 200;
//                        choseButton.setId(Integer.toString(dish.getId()));
//                        layout5.getChildren().add(choseButton);
//                        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
//                            @Override
//                            public void handle(MouseEvent event) {
//                                List<Dish> dishes = order.getDishes();
//                                dishes.stream()
//                                        .filter(dish -> dish.getId() == Integer.parseInt(choseButton.getId()))
//                                        .forEach(dish -> dish.setReady(true));
//                                order.setDishes(dishes);
//                                JSONObject json = new JSONObject(order);
//
//                                StringEntity entity = new StringEntity(json.toString(), "UTF8");
//                                entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//                                HttpPost httpPost = new HttpPost("http://localhost:8080/api/order/update");
//                                httpPost.setEntity(entity);
//                                try (CloseableHttpResponse response2 = httpClient.execute(httpPost)) {
//                                    choseButton.setStyle("-fx-background-color: green");
//                                    choseButton.removeEventFilter(MouseEvent.MOUSE_CLICKED, this);
//                                    System.out.println(choseButton.getId());
//                                    HttpEntity entity2 = response2.getEntity();
//                                    String content1 = EntityUtils.toString(entity2);
//                                    EntityUtils.consume(entity2);
//                                } catch (IOException exception) {
//                                    exception.printStackTrace();
//                                }
//                            }
//                        };
//                        choseButton.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);
//                    }
//                    EntityUtils.consume(entity1);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//        update = true;
//        new Thread(() -> {
//            Map<String, Object> jsonValues = new HashMap<String, Object>();
//            jsonValues.put("username", "ivailo");
//            jsonValues.put("password", "password");
//            Dish dish = new Dish();
//            dish.setId(5);
//            dish.setName("ivailo");
//            dish.setReady(true);
//            JSONObject json = new JSONObject(dish);
//
//            StringEntity entity = new StringEntity(json.toString(), "UTF8");
//            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//
//            HttpPost httpPost = new HttpPost("http://localhost:8080/api/dish/create");
//            httpPost.setEntity(entity);
//            while(update){
//                try(CloseableHttpResponse response2 = httpClient.execute(httpPost)) {
//                    HttpEntity entity2 = response2.getEntity();
//                    String content = EntityUtils.toString(entity2);
//                    System.out.println(content);
//                    // do something useful with the response body
//                    // and ensure it is fully consumed
//                    EntityUtils.consume(entity2);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
        ///
//        try {
//
//            long lStartTime = System.nanoTime();
//            String result = sendJSONData(Message);
//            long lEndTime = System.nanoTime();
//
//            long output = lEndTime - lStartTime;
//
//            System.out.println("Elapsed time in milliseconds: " + output / 1000000);
//            System.out.println(result);
////            Preferences userPreferences = Preferences.userNodeForPackage(LoggedFirstDesign.class);
//            Gson gson = new Gson();
//
//            JwtUser user = gson.fromJson(result, JwtUser.class);
//            Preferences userPreferences = Preferences.userRoot();
//            userPreferences.put("Authorization",user.getToken());
//            System.out.println(userPreferences.get("Authorization","DEFAULT"));
//        } catch (Exception E) {
//            System.out.println("Exception Occured. " + E.getMessage());
//        }
    }



}
