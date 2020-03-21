package Application;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

import static Helpers.ServerRequests.httpClientLongPolling;

public class RestaurantApplication extends Application{

    public static void main(String[] args) {
        Application.launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        StageManager stageManager = new StageManager();
        stageManager.initializeStages(primaryStage);
    }

    public static void logout(){
        try {
            httpClientLongPolling.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientLongPolling = HttpClients.createDefault();

        StageManager.currentStage.close();
        StageManager.currentStage  = (Stage)StageManager.secondLoginStage.getOwner();
        StageManager.currentStage.show();


    }
}
