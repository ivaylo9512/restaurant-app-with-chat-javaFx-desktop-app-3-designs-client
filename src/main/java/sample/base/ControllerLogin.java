package sample.base;

import javafx.fxml.FXML;

import static Application.RestaurantApplication.stageManager;

public class ControllerLogin {
    @FXML
    public void showLoginThirdStyle(){
        stageManager.changeStage(stageManager.thirdLoginStage);
    }
    @FXML
    public void showLoginFirstStyle(){
        stageManager.changeStage(stageManager.firstLoginStage);
    }

    @FXML
    public void showLoginSecondStyle(){
        stageManager.changeStage(stageManager.secondLoginStage);
    }

    @FXML
    public void close(){
        stageManager.currentStage.close();
    }
}
