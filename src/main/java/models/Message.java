package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Message {
    private long receiverId;
    private long senderId;
    private LocalTime time;
    private String message;
    private long chatId;
    private LocalDate session;

    public Message() {
    }

    public Message(long receiverId, LocalTime time,LocalDate session, String message, long chatId) {
        this.receiverId = receiverId;
        this.time = time;
        this.session = session;
        this.message = message;
        this.chatId = chatId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(long receiverId) {
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

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public LocalDate getSession() {
        return session;
    }

    public void setSession(LocalDate session) {
        this.session = session;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
}
