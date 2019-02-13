package Animations;


import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class ExpandOrderPane {
    private static Pane currentPane;
    private static Button button;
    private static Label label;

    private static double width;
    private static double height;
    private static double buttonX;
    private static double buttonY;
    private static double mouseY;
    private static double mouseX;
    private static double xButtonRation;

    public static Boolean buttonExpanded = false;
    public static String currentStyle;

    public static void expandPane(Pane pane, MouseEvent event){
        if(!buttonExpanded) {
            currentPane = pane;
            button = (Button) currentPane.getChildren().get(0);
            label = (Label) currentPane.getChildren().get(1);

            mouseX = event.getScreenX();
            mouseY = event.getScreenY();
            buttonX = button.getLayoutX();
            buttonY = button.getLayoutY();

            width = currentPane.getWidth();
            height = currentPane.getHeight();

            xButtonRation = currentPane.getPrefWidth() / (button.getLayoutX() + button.getPrefWidth() / 2);
        }
        currentPane.setOnMousePressed(eventPress -> {
            mouseX = eventPress.getScreenX();
            mouseY = eventPress.getScreenY();
        });

        currentPane.setOnMouseDragged(eventDrag -> {
            currentPane.setTranslateX(currentPane.getTranslateX() + (eventDrag.getScreenX() - mouseX));
            currentPane.setTranslateY(currentPane.getTranslateY() + (eventDrag.getScreenY() - mouseY));

            double expandX = mouseX - eventDrag.getScreenX();
            double expandY = mouseY - eventDrag.getScreenY();
            double expand;

            if (expandY > 0) {
                expand = Math.max(expandX, expandY);
                expand += 1.3;
            } else {
                expand = Math.min(expandX, expandY);
                expand -= 1.3;
            }

            if(!buttonExpanded && currentPane.getPrefWidth() - expand > width ) {

                button.setPrefWidth(button.getPrefWidth() - expand / 15);
                button.setPrefHeight(button.getPrefHeight() - expand / 15);
                currentPane.setPrefWidth(currentPane.getPrefWidth() - expand);
                currentPane.setPrefHeight(currentPane.getPrefHeight() - expand);

                double translateY = currentPane.getPrefHeight() - button.getPrefHeight() - 10.5 - buttonY;
                double translateX = (currentPane.getPrefWidth() - button.getWidth()) / xButtonRation - buttonX;
                button.setTranslateX(translateX);
                button.setTranslateY(translateY);
                label.setPrefWidth(label.getPrefWidth() - expand);

                if (currentPane.getPrefWidth() > 330) {
                    buttonExpanded = true;
                }
            }

            mouseX = eventDrag.getScreenX();
            mouseY = eventDrag.getScreenY();
        });

        currentPane.setOnMouseReleased(eventRelease -> {
            if(!buttonExpanded){
                TranslateTransition transitionPane = new TranslateTransition(Duration.millis(500),currentPane);
                transitionPane.setToX(0);
                transitionPane.setToY(0);
                transitionPane.play();

                TranslateTransition transitionButton = new TranslateTransition(Duration.millis(500),button);
                transitionButton.setToX(0);
                transitionButton.setToY(0);
                transitionButton.play();

                ResizeHeight heightPane = new ResizeHeight(Duration.millis(500),currentPane,height);
                heightPane.play();
                ResizeWidth widthPane = new ResizeWidth(Duration.millis(500),currentPane,width);
                widthPane.play();

                ResizeHeight heightButton = new ResizeHeight(Duration.millis(500),button,28);
                heightButton.play();
                ResizeWidth widthButton = new ResizeWidth(Duration.millis(500),button,28);
                widthButton.play();

                ResizeWidth widthLabel = new ResizeWidth(Duration.millis(500),label,30);
                widthLabel.play();
            }
        });
    }
}
