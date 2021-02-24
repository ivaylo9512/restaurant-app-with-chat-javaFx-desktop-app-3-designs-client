package models;

import controllers.base.Controller;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

class StageView{
    private Stage login, loggedMenuStage;
    private Controller controller;
    private Alert alert;

    public StageView(Stage login, Stage loggedMenuStage, Controller controller, Alert alert) {
        this.login = login;
        this.loggedMenuStage = loggedMenuStage;
        this.controller = controller;
        this.alert = alert;
    }

    public Stage getLogin() {
        return login;
    }

    public void setLogin(Stage login) {
        this.login = login;
    }

    public Stage getLoggedMenuStage() {
        return loggedMenuStage;
    }

    public void setLoggedMenuStage(Stage loggedMenuStage) {
        this.loggedMenuStage = loggedMenuStage;
    }

    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }
}

