package animations;

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class TransitionResizeHeight extends Transition {

    private double height, newHeight, heightDifference;
    private Region region;

    public TransitionResizeHeight(Duration duration, Region region, double newHeight ) {
        this.region = region;
        this.height = heightDifference;
        setCycleDuration(duration);
    }
    public TransitionResizeHeight(Region region){
        this.region = region;
    }

    public void setDuration(Duration duration){
        setCycleDuration(duration);
    }
    public void setToHeight(double height) {
        this.newHeight = height;
    }
    @Override
    public void play() {
        this.height = region.getPrefHeight();
        this.heightDifference = newHeight - height;
        super.play();
    }

    @Override
    protected void interpolate(double frac) {
        region.setPrefHeight(height + (heightDifference * frac));
    }
}