package Application;

import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class RestaurantApplication extends Application{
    public static LoginManager loginManager;
    public static StageManager stageManager;
    public static OrderManager orderManager;
    public static MessageManager messageManager;

    public static MediaPlayer notificationSound;


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loginManager = LoginManager.initialize();
        stageManager = StageManager.initialize(primaryStage);
        orderManager = OrderManager.initialize();
        messageManager = MessageManager.initialize();

        Media sound = new Media(getClass()
                .getResource("/notification.mp3")
                .toExternalForm());
        notificationSound = new MediaPlayer(sound);
        notificationSound.setOnEndOfMedia(() -> notificationSound.stop());
    }
}
