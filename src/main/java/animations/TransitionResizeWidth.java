package animations;

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class TransitionResizeWidth extends Transition {

    private double width, widthDifference;
    private Region region;
    private DoubleProperty toWidth = new SimpleDoubleProperty();
    private DoubleProperty fromWidth = new SimpleDoubleProperty();
    private boolean reverse;

    public TransitionResizeWidth(Duration duration, Region region, double newWidth) {
        this.region = region;
        this.toWidth.set(newWidth);
        setCycleDuration(duration);
    }
    public TransitionResizeWidth(Duration duration, Region region){
        this.region = region;
        setCycleDuration(duration);
    }
    @Override
    public void play() {
        this.width = region.getPrefWidth();
        this.widthDifference = toWidth.get() - width;
        if(reverse){
            widthDifference = fromWidth.get() - width;
        }
        super.play();
    }
    @Override
    protected void interpolate(double frac) {
        region.setPrefWidth(width + (widthDifference * frac));
    }
    public void setDuration(Duration duration){
        setCycleDuration(duration);
    }
    public void setToWidth(double toWidth){
        this.toWidth.set(toWidth);
    }
    public DoubleProperty toWidthProperty(){
        return toWidth;
    }
    public void setFromWidth(double fromWidth){
        this.fromWidth.set(fromWidth);
    }
    public DoubleProperty fromWidthProperty(){
        return fromWidth;
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