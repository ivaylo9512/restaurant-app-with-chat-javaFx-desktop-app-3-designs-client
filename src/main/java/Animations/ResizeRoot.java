package Animations;

import Helpers.Scrolls;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ResizeRoot {
    private static double minWidth;
    private static double minHeight;
    private static double maxWidth;
    private static double maxHeight;

    private static Cursor cursor;
    private static int border = 5;

    private static double height;
    private static double width;

    private static double offsetX;
    private static double offsetY;

    public static boolean resize = true;

    public static void addListeners(AnchorPane root){
        root.setOnMouseEntered(event -> {
            height = root.getPrefHeight();
            width = root.getPrefWidth();

            minWidth = root.getMinWidth();
            minHeight = root.getMinHeight();
            maxWidth = root.getMaxWidth();
            maxHeight = root.getMaxHeight();
        });

        EventHandler<MouseEvent> mousePosition = event -> {
            EventType eventType = event.getEventType();
            if(MouseEvent.MOUSE_MOVED.equals(eventType) || MouseButton.PRIMARY.equals(event.getButton())) {
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
                if(MouseEvent.MOUSE_RELEASED.equals(eventType)) {
                    System.out.println(root.getPrefWidth());
                    height = root.getPrefHeight();
                    width = root.getPrefWidth();

                }
            }
        };

        root.addEventFilter(MouseEvent.MOUSE_MOVED, mousePosition);
        root.addEventFilter(MouseEvent.MOUSE_RELEASED, mousePosition);
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            cursor = root.getCursor();
            offsetX = root.getLayoutX();
            offsetY = root.getLayoutY();
        });
        root.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            double mouseX = event.getScreenX();
            double mouseY = event.getScreenY();
            double newHeight;
            double newWidth;
            if(Cursor.W_RESIZE.equals(cursor)) {
                newWidth = root.getLayoutX() - event.getScreenX() + root.getPrefWidth();

                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    root.setPrefWidth(newWidth);
                    root.setLayoutX(event.getScreenX());
                } else {
                    newWidth = Math.min(Math.max(newWidth, minWidth), maxWidth);
                    root.setLayoutX(root.getLayoutX() + root.getPrefWidth() - newWidth);
                    root.setPrefWidth(newWidth);
                }
            }else if(Cursor.E_RESIZE.equals(cursor)){
                newWidth = event.getScreenX() - offsetX;

                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    root.setPrefWidth(newWidth);
                } else {
                    root.setPrefWidth(Math.min(Math.max(newWidth, minWidth), maxWidth));
                }
            }else if(Cursor.S_RESIZE.equals(cursor)){
                newHeight = event.getScreenY() - offsetY;

                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    root.setPrefHeight(newHeight);
                } else {
                    root.setPrefHeight(Math.min(Math.max(newHeight, minHeight), maxHeight));
                }
            }else if(Cursor.N_RESIZE.equals(cursor)){
                newHeight = root.getLayoutY() - event.getScreenY() + root.getPrefHeight();

                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    root.setPrefHeight(newHeight);
                    root.setLayoutY(event.getScreenY());
                } else {
                    newHeight = Math.min(Math.max(newHeight, minHeight), maxHeight);
                    root.setLayoutY(root.getLayoutY() + root.getPrefHeight() - newHeight);
                    root.setPrefHeight(newHeight);
                }
            }

        });



    }
}
