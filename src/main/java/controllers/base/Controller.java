package controllers.base;

public interface Controller {
    void resetStage();

    void adjustStage() throws Exception;

    void setStage();
}
