package animations;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.Collections;

public class ResizeMainChat {
    private static double mouseX;
    private static double mouseY;
    private static boolean move;
    private static double border = 3;
    private static double height;
    private static double layoutY;
    private static double layoutX;

    public static void addListeners(AnchorPane mainChat){
        height = mainChat.getPrefHeight();

        EventHandler<MouseEvent> mousePosition = event -> {
            EventType eventType = event.getEventType();
            if(MouseEvent.MOUSE_MOVED.equals(eventType) || MouseButton.PRIMARY.equals(event.getButton())) {
                if (event.getY() <= border) {
                    mainChat.setCursor(Cursor.N_RESIZE);
                } else if (event.getY() >= height - border) {
                    mainChat.setCursor(Cursor.S_RESIZE);
                } else {
                    mainChat.setCursor(Cursor.DEFAULT);
                }

                if(MouseEvent.MOUSE_RELEASED.equals(eventType)) {
                    height = mainChat.getPrefHeight();
                }
            }
        };

        mainChat.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseEntered -> ResizeRoot.resize = false);
        mainChat.addEventFilter(MouseEvent.MOUSE_EXITED, mouseExited -> ResizeRoot.resize = true);
        mainChat.addEventFilter(MouseEvent.MOUSE_MOVED, mousePosition);
        mainChat.addEventFilter(MouseEvent.MOUSE_RELEASED, mousePosition);
        mainChat.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton().name().equals("PRIMARY")) {
                layoutY = mainChat.getLayoutY();
                layoutX = mainChat.getLayoutX();
                mouseX = event.getScreenX();
                mouseY = event.getScreenY();
                height = mainChat.getPrefHeight();

                Node intersectedNode = event.getPickResult().getIntersectedNode();
                move = !intersectedNode.getTypeSelector().equals("TextAreaSkin$ContentView") &&
                        !intersectedNode.getStyleClass().equals(new ArrayList<>(Collections.singletonList("text"))) &&
                        !(intersectedNode instanceof Button) &&
                        !intersectedNode.getTypeSelector().equals("StackPane") &&
                        !intersectedNode.getTypeSelector().equals("ScrollBarSkin$1");
            }
        });

        mainChat.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            double moveY = mouseY - event.getScreenY();
            double moveX = mouseX - event.getScreenX();
            if(event.getButton().name().equals("PRIMARY")) {
                if (mainChat.getCursor().equals(Cursor.N_RESIZE)) {

                    if (height + moveY <= mainChat.getMinHeight()) {
                        mainChat.setLayoutY(mainChat.getLayoutY() - (mainChat.getMinHeight() - mainChat.getPrefHeight()));
                        mainChat.setPrefHeight(mainChat.getMinHeight());
                    } else if (height + moveY >= mainChat.getMaxHeight()) {
                        mainChat.setLayoutY(mainChat.getLayoutY() - (mainChat.getMaxHeight() - mainChat.getPrefHeight()));
                        mainChat.setPrefHeight(mainChat.getMaxHeight());
                    } else {
                        mainChat.setPrefHeight(height + moveY);
                        mainChat.setLayoutY(layoutY - moveY);
                    }

                } else if (mainChat.getCursor().equals(Cursor.S_RESIZE)) {

                    if (height - moveY <= mainChat.getMinHeight()) {
                        mainChat.setPrefHeight(mainChat.getMinHeight());
                    } else if (height - moveY >= mainChat.getMaxHeight()) {
                        mainChat.setPrefHeight(mainChat.getMaxHeight());
                    } else {
                        mainChat.setPrefHeight(height - moveY);
                    }

                } else if (move){
                    mainChat.setLayoutY(layoutY - moveY);
                    mainChat.setLayoutX(layoutX - moveX);
                }
            }
        });
    }
}
