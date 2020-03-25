package Application;

import Helpers.Services.LoginService;
import Helpers.Services.RegisterService;

public class LoginManager {
    private LoginService loginService = new LoginService();
    private RegisterService registerService = new RegisterService();

    private LoginManager(){
    }
    static LoginManager initialize(){
        return new LoginManager();
    }
}
