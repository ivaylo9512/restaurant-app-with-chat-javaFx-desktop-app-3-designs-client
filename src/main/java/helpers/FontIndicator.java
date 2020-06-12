package helpers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.Font;

public class FontIndicator {
    private DoubleProperty fontPt = new SimpleDoubleProperty(Font.getDefault().getSize());

    public double getFontPt() {
        return fontPt.get();
    }

    public void setFontPt(double fontPt) {
        this.fontPt.setValue(fontPt);
    }

    public  DoubleProperty getFontProperty(){
        return fontPt;
    }
}
