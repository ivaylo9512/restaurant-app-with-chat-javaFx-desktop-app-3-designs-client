package Animations;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MoveRoot {
    private static Double offsetX;
    private static Double offsetY;

    public static void moveStage(Pane moveBar, Pane root) {
        root.setOnMousePressed(event -> {
            offsetX = event.getX();
            offsetY = event.getY();
        });

        moveBar.setOnMouseDragged(event -> {
            root.setLayoutX(event.getScreenX() - offsetX);
            root.setLayoutY(event.getScreenY() - offsetY);
        });
    }

}
