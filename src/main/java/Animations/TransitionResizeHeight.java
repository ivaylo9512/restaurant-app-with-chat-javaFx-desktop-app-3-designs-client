package Animations;

import javafx.animation.Transition;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class TransitionResizeHeight extends Transition {

    private double height;
    private double heightDifference;
    private Region region;

    public TransitionResizeHeight(Duration duration, Region region, double newHeight ) {
        setCycleDuration(duration);
        this.region = region;
        this.height = region.getHeight();
        this.heightDifference = newHeight - height;
    }

    @Override
    protected void interpolate(double frac) {
        region.setPrefHeight(height + (heightDifference * frac));
    }
}