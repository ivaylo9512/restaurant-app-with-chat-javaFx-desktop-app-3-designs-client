package application;

import helpers.ObservableOrderedMap;
import helpers.RequestTask;
import com.fasterxml.jackson.databind.JavaType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import org.apache.http.client.methods.HttpRequestBase;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static application.RestaurantApplication.*;
import static application.ServerRequests.*;


public class ChatManager {
    public Map<Integer, ChatValue> chats = new HashMap<>();
    public ObservableList<ChatValue> chatsList = FXCollections.observableArrayList();
    public ObjectProperty<ChatValue> mainChatValue = new SimpleObjectProperty<>();
    public ObjectProperty<ChatValue> secondChatValue = new SimpleObjectProperty<>();

    ChatManager() {
    }

    void setChats(Map<Integer, Chat> chatsList){
        chatsList.values().forEach(chat -> {
            Image profilePicture;
            int userId = chat.getSecondUser().getId().get();
            try(InputStream in = new BufferedInputStream(
                    new URL(chat.getSecondUser().getProfilePicture().get()).openStream())) {

                profilePicture = new Image(in);
            }catch(Exception e){
                profilePicture = new Image(getClass().getResourceAsStream("/images/default-picture.png"));
            }
            ChatValue chatValue = new ChatValue(chat.getId(), userId, profilePicture, chat.getSecondUser());
            chat.getSessions().forEach(session -> chatValue.getSessions().put(0, session.getDate(), session));

            this.chatsList.add(chatValue);
            chats.put(chat.getId(), chatValue);
        });
    }

    public void getNextSessions(ChatValue chat){
        int chatId = chat.getChatId();
        int nextPage = chat.getSessions().size() / pageSize;

        try {
            RequestTask<List<Session>> task = new RequestTask<>(mapper.getTypeFactory().constructCollectionType(List.class, Session.class), ServerRequests.getNextSessions(chatId, nextPage));
            tasks.execute(task);
            task.setOnSucceeded(event -> {
                List<Session> nextSessions = task.getValue();
                if (nextSessions.size() < pageSize) chat.setMoreSessions(false);

                nextSessions.forEach(session -> {
                    ObservableOrderedMap<LocalDate, Session> sessions = chat.getSessions();
                    if (!sessions.containsKey(session.getDate())) {
                       sessions.put(0, session.getDate(), session);
                    }
                });
            });
        }catch (Exception e){
            alertManager.addLoggedAlert(e.getMessage());
        }
    }

    public void sendMessage(String messageText, int chatId, int receiverId){
        HttpRequestBase request = ServerRequests.sendMessage(messageText, chatId, receiverId);
        RequestTask<Message> task = new RequestTask<>(Message.class, request);
        tasks.execute(task);
    }

    public void appendMessage(Message message){
        ChatValue chat = chats.get(message.getChatId());
        Session session = chat.getSessions().get(message.getSession());
        if(session == null){
            chat.getSessions().put(0, message.getSession(),
                    new Session(message.getSession(), message));
        }else{
            session.getMessages().add(message);
        }
    }

    public void resetChats(){
        chats.clear();
        chatsList.clear();

        mainChatValue.set(null);
        secondChatValue.set(null);
    }
}
