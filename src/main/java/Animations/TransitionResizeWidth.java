package Animations;

import javafx.animation.Transition;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class TransitionResizeWidth extends Transition {

    private double width;
    private double widthDifference;
    private Region region;

    public TransitionResizeWidth(Duration duration, Region region, double newWidth ) {
        setCycleDuration(duration);
        this.region = region;
        this.width = region.getWidth();
        this.widthDifference = newWidth - width;
    }
    public TransitionResizeWidth(Region region){
        this.region = region;
    }
    public void setAndPlay(Duration duration, double newWidth){
        setCycleDuration(duration);
        this.width = region.getPrefWidth();
        this.widthDifference = newWidth - width;
        play();
    }
    @Override
    protected void interpolate(double frac) {
        region.setPrefWidth(width + (widthDifference * frac));
    }
}