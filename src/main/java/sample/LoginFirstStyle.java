package sample;
import Animations.MoveStage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class LoginFirstStyle extends Application {

    static CloseableHttpClient httpClient = HttpClients.createDefault();
    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = new FXMLLoader(getClass().getResource("/login.fxml")).load();
        Pane pane = new Pane();
        pane.setPrefHeight(50);
        pane.setPrefWidth(50);
        pane.setStyle("-fx-background-color: red");
        root.getChildren().add(pane);
        Scene scene1 = new Scene(root);
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene1);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        primaryStage.show();
        MoveStage.moveStage(root);
    }
}