package animations;

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
    public TransitionResizeHeight(Region region){
        this.region = region;
    }
    public void setAndPlay(Duration duration, double newHeight){
        setCycleDuration(duration);
        this.height = region.getPrefHeight();
        this.heightDifference = newHeight - height;
        play();
    }
    @Override
    protected void interpolate(double frac) {
        region.setPrefHeight(height + (heightDifference * frac));
    }
}