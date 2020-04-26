package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Message {
    private int receiverId;

    private int senderId;

    private LocalTime time;

    private String message;

    private int chatId;

    private LocalDate session;

    public Message() {
    }

    public Message(int receiver, LocalTime time, String message) {
        this.receiverId = receiver;
        this.time = time;
        this.message = message;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public LocalDate getSession() {
        return session;
    }

    public void setSession(LocalDate session) {
        this.session = session;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
}
