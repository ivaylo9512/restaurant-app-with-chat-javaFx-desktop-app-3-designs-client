package sample;
import Animations.MoveStage;
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
        Scene scene2 = new Scene(root);
        scene2.setFill(Color.TRANSPARENT);
        String string = "/sample";
        scene2.getStylesheets().add(LoggedFirstDesign.class.getResource("/logged-first.css").toString());
        stage.setScene(scene2);
        stage.show();
        MoveStage.moveStage(root);

    }



}
