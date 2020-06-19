package controllers.base;

public interface ControllerAdjustable extends Controller{
    void resetStage();

    void adjustStage(double height, double width) throws Exception;
}
