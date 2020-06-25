package application;

import helpers.FontIndicator;
import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;


public class RestaurantApplication extends Application{
    public static NotificationManager notificationManager = new NotificationManager();
    public static OrderManager orderManager = new OrderManager();
    public static LoginManager loginManager = new LoginManager();
    public static ChatManager chatManager = new ChatManager();
    public static AlertManager alertManager = new AlertManager();
    public static StageManager stageManager = new StageManager();
    public static FontIndicator fontIndicator = new FontIndicator();

    public static MediaPlayer notificationSound;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Media sound = new Media(getClass()
                .getResource("/notification.mp3")
                .toExternalForm());
        notificationSound = new MediaPlayer(sound);
        notificationSound.setOnEndOfMedia(() -> notificationSound.stop());

        stageManager.initializeStages(primaryStage);
        loginManager.checkIfLogged();
    }

    public FontIndicator getFontIndicator(){
        return fontIndicator;
    }

}
