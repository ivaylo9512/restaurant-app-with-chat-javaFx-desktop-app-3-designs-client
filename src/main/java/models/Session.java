package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Session{

    private LocalDate date;
    private List<Message> messages = new ArrayList<>();

    public Session() {
    }

    public Session(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
