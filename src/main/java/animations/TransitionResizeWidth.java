package animations;

import javafx.animation.Transition;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class TransitionResizeWidth extends Transition {

    private double width, newWidth, widthDifference;
    private Region region;

    public TransitionResizeWidth(Duration duration, Region region, double newWidth) {
        this.region = region;
        this.newWidth = newWidth;
        setCycleDuration(duration);
    }
    public TransitionResizeWidth(Region region){
        this.region = region;
    }

    public void setDuration(Duration duration){
        setCycleDuration(duration);
    }
    public void setToWidth(double width) {
        this.newWidth = width;
    }
    @Override
    public void play() {
        this.width = region.getWidth();
        this.widthDifference = newWidth - width;
        super.play();
    }
    @Override
    protected void interpolate(double frac) {
        region.setPrefWidth(width + (widthDifference * frac));
    }
}