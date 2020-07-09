package animations;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ResizeStage {
    private static double minWidth, minHeight, maxWidth, maxHeight, height, width;
    private static double offsetX, offsetY;

    private static Cursor cursor;
    private static int border = 7;

    public static void addListeners(Pane root, Pane contentRoot, Stage stage) {
        contentRoot.setOnMouseEntered(event -> {
            height = contentRoot.getHeight();
            width = contentRoot.getWidth();

            minWidth = root.getMinWidth();
            minHeight = root.getMinHeight();
            maxWidth = root.getMaxWidth();
            maxHeight = root.getMaxHeight();
        });

        EventHandler<MouseEvent> checkMousePosition = event -> {
            EventType eventType = event.getEventType();
            if (MouseEvent.MOUSE_MOVED.equals(eventType) || MouseButton.PRIMARY.equals(event.getButton())) {
                double mouseX = event.getX();
                double mouseY = event.getY();
                if (mouseY <= border) {
                    contentRoot.setCursor(Cursor.N_RESIZE);
                } else if (mouseY >= height - border) {
                    contentRoot.setCursor(Cursor.S_RESIZE);
                } else if (mouseX <= border) {
                    contentRoot.setCursor(Cursor.W_RESIZE);
                } else if (mouseX >= width - border) {
                    contentRoot.setCursor(Cursor.E_RESIZE);
                } else {
                    contentRoot.setCursor(Cursor.DEFAULT);
                }
                if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
                    height = contentRoot.getPrefHeight();
                    width = contentRoot.getPrefWidth();

                }
            }
        };

        contentRoot.addEventFilter(MouseEvent.MOUSE_MOVED, checkMousePosition);
        contentRoot.addEventFilter(MouseEvent.MOUSE_RELEASED, checkMousePosition);
        contentRoot.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            cursor = contentRoot.getCursor();
            offsetX = stage.getX();
            offsetY = stage.getY();
        });
        root.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            double mouseX = event.getScreenX();
            double mouseY = event.getScreenY();
            if (Cursor.W_RESIZE.equals(cursor)) {
                double newWidth = stage.getX() - mouseX + root.getPrefWidth();

                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    stage.setWidth(newWidth);
                    stage.setX(mouseX);
                } else {
                    newWidth = Math.min(Math.max(newWidth, minWidth), maxWidth);
                    stage.setX(stage.getX() + root.getPrefWidth() - newWidth);
                    stage.setWidth(newWidth);
                }
            } else if (Cursor.E_RESIZE.equals(cursor)) {
                double newWidth = mouseX - offsetX;

                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    stage.setWidth(newWidth);
                } else {
                    stage.setWidth(Math.min(Math.max(newWidth, minWidth), maxWidth));
                }
            } else if (Cursor.S_RESIZE.equals(cursor)) {
                double newHeight = mouseY - offsetY;

                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    stage.setHeight(newHeight);
                } else {
                    stage.setHeight(Math.min(Math.max(newHeight, minHeight), maxHeight));

                }
            } else if (Cursor.N_RESIZE.equals(cursor)) {
                double newHeight = stage.getY() - mouseY + root.getPrefHeight();

                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    stage.setHeight(newHeight);
                    stage.setY(mouseY);
                } else {
                    newHeight = Math.min(Math.max(newHeight, minHeight), maxHeight);
                    stage.setY(stage.getY() + root.getPrefHeight() - newHeight);
                    stage.setHeight(newHeight);
                }
            }
        });
    }
}
