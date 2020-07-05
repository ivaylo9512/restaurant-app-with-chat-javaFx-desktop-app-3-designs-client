package animations;

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class TransitionResizeHeight extends Transition {

    private double height, heightDifference;
    private Region region;
    private DoubleProperty toHeight = new SimpleDoubleProperty();
    private DoubleProperty fromHeight = new SimpleDoubleProperty();
    private boolean reverse;

    public TransitionResizeHeight(Duration duration, Region region, double toHeight) {
        this.region = region;
        this.toHeight.set(toHeight);
        setCycleDuration(duration);
    }
    public TransitionResizeHeight(Duration duration, Region region){
        this.region = region;
        setCycleDuration(duration);
    }
    public TransitionResizeHeight(Duration duration){
        setCycleDuration(duration);
    }
    @Override
    public void play() {
        this.height = region.getHeight();
        this.heightDifference = toHeight.get() - height;
        if(reverse){
            heightDifference = fromHeight.get() - height;
        }
        super.play();
    }
    @Override
    protected void interpolate(double frac) {
        region.setPrefHeight(height + (heightDifference * frac));
    }
    public void setDuration(Duration duration){
        setCycleDuration(duration);
    }
    public void setToHeight(double toHeight) {
        this.toHeight.set(toHeight);
    }
    public DoubleProperty toHeightProperty() {
        return toHeight;
    }
    public void setFromHeight(double fromHeight) {
        this.fromHeight.set(fromHeight);
    }
    public DoubleProperty fromHeightProperty() {
        return fromHeight;
    }
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
    public boolean getReverse() {
        return reverse;
    }
    public Region getRegion() {
        return region;
    }
    public void setRegion(Region region) {
        this.region = region;
    }
}