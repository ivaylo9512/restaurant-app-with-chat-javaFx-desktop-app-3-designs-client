package Animations;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MoveStage {
    private static Double offsetX;
    private static Double offsetY;

    public static void moveStage(Pane root) {
        Stage stage = (Stage) root.getScene().getWindow();
        root.setOnMousePressed(event -> {
            offsetX = event.getSceneX();
            offsetY = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - offsetX);
            stage.setY(event.getScreenY() - offsetY);
        });
    }

}
