package animations;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ResizeRoot {
    private static double minWidth, minHeight, maxWidth, maxHeight, height, width;
    private static double offsetX, offsetY, mouseX, mouseY;

    private static Cursor cursor;
    private static int border = 5;
    public static boolean resize = true;

    public static void addListeners(AnchorPane root, Stage stage) {
        root.setOnMouseEntered(event -> {
            height = root.getPrefHeight();
            width = root.getPrefWidth();

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
                if (resize) {
                    if (mouseY <= border) {
                        root.setCursor(Cursor.N_RESIZE);
                    } else if (mouseY >= height - border) {
                        root.setCursor(Cursor.S_RESIZE);
                    } else if (mouseX <= border) {
                        root.setCursor(Cursor.W_RESIZE);
                    } else if (mouseX >= width - border) {
                        root.setCursor(Cursor.E_RESIZE);
                    } else {
                        root.setCursor(Cursor.DEFAULT);
                    }
                } else {
                    root.setCursor(Cursor.DEFAULT);
                }
                if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
                    height = root.getPrefHeight();
                    width = root.getPrefWidth();

                }
            }
        };

        root.addEventFilter(MouseEvent.MOUSE_MOVED, checkMousePosition);
        root.addEventFilter(MouseEvent.MOUSE_RELEASED, checkMousePosition);
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            cursor = root.getCursor();
            if (stage == null) {
                offsetX = root.getLayoutX();
                offsetY = root.getLayoutY();
            }else{
                offsetX = stage.getX();
                offsetY = stage.getY();
            }
        });
        root.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            mouseX = event.getScreenX();
            mouseY = event.getScreenY();
            if (stage == null) {
                resizeRoot(root);
            }else{
                resizeStage(root, stage);
            }
        });


    }

    private static void resizeStage(AnchorPane root, Stage stage) {
        double newHeight;
        double newWidth;
        if (Cursor.W_RESIZE.equals(cursor)) {
            newWidth = stage.getX() - mouseX + root.getPrefWidth();

            if (newWidth >= minWidth && newWidth <= maxWidth) {
                stage.setWidth(newWidth);
                stage.setX(mouseX);
            } else {
                newWidth = Math.min(Math.max(newWidth, minWidth), maxWidth);
                stage.setX(stage.getX() + root.getPrefWidth() - newWidth);
                stage.setWidth(newWidth);
            }
        } else if (Cursor.E_RESIZE.equals(cursor)) {
            newWidth = mouseX - offsetX;

            if (newWidth >= minWidth && newWidth <= maxWidth) {
                stage.setWidth(newWidth);
            } else {
                stage.setWidth(Math.min(Math.max(newWidth, minWidth), maxWidth));
            }
        } else if (Cursor.S_RESIZE.equals(cursor)) {
            newHeight = mouseY - offsetY;

            if (newHeight >= minHeight && newHeight <= maxHeight) {
                stage.setHeight(newHeight);
            } else {
                stage.setHeight(Math.min(Math.max(newHeight, minHeight), maxHeight));
            }
        } else if (Cursor.N_RESIZE.equals(cursor)) {
            newHeight = stage.getY() - mouseY + root.getPrefHeight();

            if (newHeight >= minHeight && newHeight <= maxHeight) {
                stage.setHeight(newHeight);
                stage.setY(mouseY);
            } else {
                newHeight = Math.min(Math.max(newHeight, minHeight), maxHeight);
                stage.setY(stage.getY() + root.getPrefHeight() - newHeight);
                stage.setHeight(newHeight);
            }
        }
    }

    private static void resizeRoot(AnchorPane root) {
        double newHeight;
        double newWidth;
        if (Cursor.W_RESIZE.equals(cursor)) {
            newWidth = root.getLayoutX() - mouseX + root.getPrefWidth();

            if (newWidth >= minWidth && newWidth <= maxWidth) {
                root.setPrefWidth(newWidth);
                root.setLayoutX(mouseX);
            } else {
                newWidth = Math.min(Math.max(newWidth, minWidth), maxWidth);
                root.setLayoutX(root.getLayoutX() + root.getPrefWidth() - newWidth);
                root.setPrefWidth(newWidth);
            }
        } else if (Cursor.E_RESIZE.equals(cursor)) {
            newWidth = mouseX - offsetX;

            if (newWidth >= minWidth && newWidth <= maxWidth) {
                root.setPrefWidth(newWidth);
            } else {
                root.setPrefWidth(Math.min(Math.max(newWidth, minWidth), maxWidth));
            }
        } else if (Cursor.S_RESIZE.equals(cursor)) {
            newHeight = mouseY - offsetY;

            if (newHeight >= minHeight && newHeight <= maxHeight) {
                root.setPrefHeight(newHeight);
            } else {
                root.setPrefHeight(Math.min(Math.max(newHeight, minHeight), maxHeight));
            }
        } else if (Cursor.N_RESIZE.equals(cursor)) {
            newHeight = root.getLayoutY() - mouseY + root.getPrefHeight();

            if (newHeight >= minHeight && newHeight <= maxHeight) {
                root.setPrefHeight(newHeight);
                root.setLayoutY(mouseY);
            } else {
                newHeight = Math.min(Math.max(newHeight, minHeight), maxHeight);
                root.setLayoutY(root.getLayoutY() + root.getPrefHeight() - newHeight);
                root.setPrefHeight(newHeight);
            }
        }
    }
}
