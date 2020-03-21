package Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import sample.LoggedSecondStyle;
import sample.LoginFirstStyle;
import Application.StageManager;
import java.io.IOException;

public class RestaurantApplication{


    public static void main(String[] args) {
        Application.launch(StageManager.class, args);

    }
}
