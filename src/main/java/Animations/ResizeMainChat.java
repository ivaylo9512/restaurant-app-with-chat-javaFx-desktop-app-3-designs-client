package Animations;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import javax.xml.stream.EventFilter;
import java.sql.SQLOutput;

public class ResizeMainChat {
    private static double mouseX;
    private static double mouseY;
    private static boolean resize;
    private static double border = 3;
    private static double height;
    private static double layoutY;
    private static double layoutX;

    public static void addListeners(AnchorPane mainChat){
        height = mainChat.getPrefHeight();

        mainChat.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (event.getY() <= border){
                mainChat.setCursor(Cursor.N_RESIZE);
            } else if (event.getY() >= height - border) {
                mainChat.setCursor(Cursor.S_RESIZE);
            } else{
                mainChat.setCursor(Cursor.DEFAULT);
            }
        });

        mainChat.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            layoutY = mainChat.getLayoutY();
            layoutX = mainChat.getLayoutX();
            mouseX = event.getScreenX();
            mouseY = event.getScreenY();
            height = mainChat.getPrefHeight();
            resize = !event.getPickResult().getIntersectedNode().getTypeSelector().equals("TextAreaSkin$ContentView") &&
                    !event.getPickResult().getIntersectedNode().getTypeSelector().equals("Text") &&
                    !event.getPickResult().getIntersectedNode().getTypeSelector().equals("Button") &&
                    !event.getPickResult().getIntersectedNode().getTypeSelector().equals("ScrollBarSkin$1");
        });

        mainChat.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            double moveY = mouseY - event.getScreenY();
            double moveX = mouseX - event.getScreenX();
            if(resize) {
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

                } else {
                    mainChat.setLayoutY(layoutY - moveY);
                    mainChat.setLayoutX(layoutX - moveX);
                }
            }
        });

        mainChat.setOnMouseReleased(event -> height = mainChat.getHeight());
    }
}
