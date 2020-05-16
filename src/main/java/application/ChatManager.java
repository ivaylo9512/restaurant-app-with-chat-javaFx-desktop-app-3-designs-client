package application;

import helpers.RequestTask;
import com.fasterxml.jackson.databind.JavaType;
import javafx.beans.Observable;
import models.Chat;
import models.ChatValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import models.Message;
import models.Session;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static application.RestaurantApplication.loginManager;
import static application.RestaurantApplication.stageManager;
import static application.ServerRequests.*;


public class ChatManager {

    private JavaType sessionType = mapper.getTypeFactory().constructCollectionType(List.class, Session.class);
    private JavaType messageType = mapper.getTypeFactory().constructType(Message.class);

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

    public void getNextSessions(ChatValue chat){
        int chatId = chat.getChatId();
        int nextPage = chat.getSessions().size() / pageSize;

        try {
            RequestTask<List<Session>> task = new RequestTask<>(sessionType, ServerRequests.getNextSessions(chatId, nextPage));
            task.setOnSucceeded(event -> {
                List<Session> nextSessions = task.getValue();
                if (nextSessions.size() < pageSize) chat.setMoreSessions(false);

                nextSessions.forEach(session -> {
                    ListOrderedMap<LocalDate, Session> sessions = chat.getSessions();
                    if (!sessions.containsKey(session.getDate())) {
                        sessions.put(session.getDate(), session);
                    }
                });
            });
        }catch (Exception e){
            stageManager.showAlert(e.getMessage());
        }
    }

    public void sendMessage(String messageText, int chatId, int receiverId){
        HttpRequestBase request = ServerRequests.sendMessage(messageText, chatId, receiverId);
        RequestTask task = new RequestTask(messageType, request);
        tasks.execute(task);
    }

    public void appendMessage(Message message){
        ChatValue chat = chats.get(message.getChatId());
        Session session = chat.getSessions().get(message.getSession());
        if(session == null){
            chat.getSessionsObservable().put(message.getSession(),
                    new Session(message.getSession(), message));
        }else{
            session.getMessages().add(message);
        }
    }

    public void resetChats(){
        chats.clear();
        chatsList.clear();
    }
}
