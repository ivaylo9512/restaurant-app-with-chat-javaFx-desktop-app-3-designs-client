package Animations;

import javafx.animation.Transition;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class TransitionResizeHeight extends Transition {

    private double Height;
    private double heightDifference;
    private Region region;

    public TransitionResizeHeight(Duration duration, Region region, double newHeight ) {
        setCycleDuration(duration);
        this.region = region;
        this.Height = region.getHeight();
        this.heightDifference = newHeight - Height;
    }

    @Override
    protected void interpolate(double frac) {
        region.setPrefHeight(Height + (heightDifference * frac));
    }
}