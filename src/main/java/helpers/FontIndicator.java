package helpers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Font;

public class FontIndicator {
    private DoubleProperty fontPx = new SimpleDoubleProperty(16);
    private KeyCombination  combination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);

    public void setSliderBinding(Node root, Slider slider){
        root.setOnKeyPressed(event -> {
            if(combination.match(event)){
                slider.setValue(fontPx.get());
                slider.setDisable(false);
            }
        });
        root.setOnKeyReleased(event -> slider.setDisable(true));
        slider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            fontPx.set(newValue.intValue());
        }));
    }

    public DoubleProperty getFontPxProperty() {
        return fontPx;
    }

    public double getFontPx() {
        return fontPx.get();
    }

    public void setFontPx(double fontPx) {
        this.fontPx.setValue(fontPx);
    }
}
