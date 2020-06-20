package controllers.base;

import javafx.stage.Stage;

public abstract class Controller {
    public Stage stage;

    public final void setStage(Stage stage){
        this.stage = stage;
    }
}
