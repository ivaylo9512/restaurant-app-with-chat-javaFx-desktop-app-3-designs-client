package Application;

import Models.Chat;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static Application.RestaurantApplication.loginManager;

public class ChatManager {

    HashMap<Integer, Chat> chats = new HashMap<>();
    private ChatManager() {
    }

    static ChatManager initialize(){
        return new ChatManager();
    }

    void setChats(List<Chat> chats){
        this.chats = chats.stream().collect(Collectors.toMap(chat -> {
            if(chat.getFirstUser().getId().get() == loginManager.userId.get()){
                return chat.getSecondUser().getId().get();
            }
            return chat.getFirstUser().getId().get();
        }, chat -> chat));
    }
}
