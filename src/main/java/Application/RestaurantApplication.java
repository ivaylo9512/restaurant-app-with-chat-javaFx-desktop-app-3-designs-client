package Application;

import javafx.application.Application;
import javafx.stage.Stage;

public class RestaurantApplication extends Application{


    public static void main(String[] args) {
        Application.launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        StageManager stageManager = new StageManager();
        stageManager.initializeStages(primaryStage);
    }
}
