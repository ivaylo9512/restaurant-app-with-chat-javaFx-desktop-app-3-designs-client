package controllers.base;

public interface ControllerAdjustable extends Controller{
    void resetStage();

    void adjustStage() throws Exception;
}
