package Application;

import javafx.application.Application;
import javafx.stage.Stage;

public class RestaurantApplication extends Application{
    public static LoginManager loginManager;
    public static StageManager stageManager;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loginManager = LoginManager.initialize();
        stageManager = StageManager.initialize(primaryStage);
    }
}
