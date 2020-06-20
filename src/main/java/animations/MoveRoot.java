package animations;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MoveRoot {
    private static Double offsetX;
    private static Double offsetY;

    public static void move(Node moveNode, AnchorPane root) {
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if(moveNode.getId().equals("moveBar") || moveNode.getId().equals("menuButton")
                    || moveNode.getId().equals("moveBarMenu")){
                offsetX = event.getX();
                offsetY = event.getY();
            }else{
                AnchorPane parent = (AnchorPane) root.getParent();
                offsetX = parent.getLayoutX() + event.getX();
                offsetY = parent.getLayoutY() + event.getY();
            }
        });

        moveNode.setOnMouseDragged(event -> {
            if(Cursor.DEFAULT.equals(root.getCursor())) {
                root.setLayoutX(event.getScreenX() - offsetX);
                root.setLayoutY(event.getScreenY() - offsetY);
            }
        });
    }

    public static void moveStage(Node moveNode, Stage stage) {
        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            offsetX = event.getX();
            offsetY = event.getY();
        });

        moveNode.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - offsetX);
                stage.setY(event.getScreenY() - offsetY);
        });
    }

}
