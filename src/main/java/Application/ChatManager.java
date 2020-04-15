package Application;

public class ChatManager {

    private ChatManager() {
    }

    static ChatManager initialize(){
        return new ChatManager();
    }
}
