package helpers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class FontIndicator {
    public static DoubleProperty fontPx = new SimpleDoubleProperty(12.5);
    private static KeyCombination combination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
    public static void setSliderBinding(Node root, Slider slider, Stage stage){
        root.setOnKeyPressed(event -> {
            if(combination.match(event)){
                slider.setDisable(false);
                slider.setManaged(true);
                slider.setOpacity(1);
            }
        });
        root.setOnKeyReleased(event -> {
            slider.setDisable(true);
            slider.setManaged(false);
            slider.setOpacity(0);
        });

        slider.setValue(fontPx.get());
        slider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            double maxFont = 24;
            double minFont = 6;
            newValue = Math.max(Math.min(newValue.doubleValue(), maxFont), minFont);
            double diff = newValue.intValue() / oldValue.doubleValue();
            fontPx.setValue(newValue.intValue());
            stage.setWidth(stage.getWidth() * diff);
            stage.setHeight(stage.getHeight() * diff);
        }));
    }

    public DoubleProperty fontPxProperty() {
        return fontPx;
    }

    public double getFontPx() {
        return fontPx.getValue();
    }

    public void setFontPx(double px) {
        fontPx.setValue(px);
    }
}
