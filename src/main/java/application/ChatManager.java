package application;

import helpers.RequestEnum;
import helpers.RequestTask;
import com.fasterxml.jackson.databind.JavaType;
import models.Chat;
import models.ChatValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import models.Session;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static application.RestaurantApplication.loginManager;
import static application.ServerRequests.mapper;
import static application.ServerRequests.pageSize;


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

    JavaType sessionType = mapper.getTypeFactory().
    constructCollectionType(List.class, Session.class);
    public void getNextSessions(ChatValue chat){
        int chatId = chat.getChatId();
        int nextPage = chat.getSessions().size() / pageSize;

        RequestTask task = new RequestTask(sessionType, ServerRequests.getNextSessions(chatId, nextPage));
        task.setOnSucceeded(event -> {
            if (nextSessions.size() < pageSize) chat.setMoreSessions(false);

        });
    }

    public void resetChats(){
        chats.clear();
        chatsList.clear();
    }
}
