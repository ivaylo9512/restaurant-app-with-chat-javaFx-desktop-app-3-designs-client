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
    private DoubleProperty fontPt = new SimpleDoubleProperty(Font.getDefault().getSize());
    private KeyCombination  combination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);

    public void setSliderBinding(Node root, Slider slider){
        root.setOnKeyPressed(event -> {
            if(combination.match(event)){
                slider.setValue(fontPt.get());
                slider.setDisable(false);
            }
        });
        root.setOnKeyReleased(event -> slider.setDisable(true));
        slider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            fontPt.set(newValue.intValue());
        }));
    }

    public DoubleProperty getFontPtProperty() {
        return fontPt;
    }

    public double getFontPt() {
        return fontPt.get();
    }

    public void setFontPt(double fontPt) {
        this.fontPt.setValue(fontPt);
    }
}
