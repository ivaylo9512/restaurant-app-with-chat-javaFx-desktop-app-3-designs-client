package Animations;

import javafx.scene.Cursor;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MoveRoot {
    private static Double offsetX;
    private static Double offsetY;

    public static void move(Pane moveBar, AnchorPane root) {
        root.setOnMousePressed(event -> {
            offsetX = event.getX();
            offsetY = event.getY();
        });

        moveBar.setOnMouseDragged(event -> {
            if(Cursor.DEFAULT.equals(root.getCursor())) {
                root.setLayoutX(event.getScreenX() - offsetX);
                root.setLayoutY(event.getScreenY() - offsetY);
            }
        });
    }

}
