package Animations;

import javafx.animation.Transition;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class ResizeWidth extends Transition {

    private double width;
    private double widthDifference;
    private Region region;

    public ResizeWidth(Duration duration, Region region, double newWidth ) {
        setCycleDuration(duration);
        this.region = region;
        this.width = region.getWidth();
        this.widthDifference = newWidth - width;
    }

    @Override
    protected void interpolate(double frac) {
        region.setPrefWidth(width + (widthDifference * frac));
    }
}