package application;

import models.Chat;
import models.ChatValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static application.RestaurantApplication.loginManager;


public class ChatManager {

    public Map<Integer, ChatValue> chats = new HashMap<>();
    public ObservableList<ChatValue> chatsList = FXCollections.observableArrayList();
    private ChatManager() {
    }

    static ChatManager initialize(){
        return new ChatManager();
    }

    void setChats(List<Chat> chatsList){
        chatsList.forEach(chat -> {
            Image profilePicture;
            if(chat.getSecondUser().getId().get() == loginManager.userId.get()){
                chat.setSecondUser(chat.getFirstUser());
            }

            int userId = chat.getSecondUser().getId().get();
            try(InputStream in = new BufferedInputStream(
                    new URL(chat.getSecondUser().getProfilePicture().get()).openStream())) {

                profilePicture = new Image(in);
            }catch(Exception e){
                profilePicture = new Image(getClass().getResourceAsStream("/images/default-picture.png"));
            }
            ChatValue chatValue = new ChatValue(chat.getId(), userId, profilePicture, chat.getSecondUser());
            chat.getSessions().forEach(session -> chatValue.getSessions().put(session.getDate(), session));

            this.chatsList.add(chatValue);
            chats.put(chat.getId(), chatValue);
        });
    }
}
