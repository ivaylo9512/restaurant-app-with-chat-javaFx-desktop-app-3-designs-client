package models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Session{

    private LocalDate date;
    private ObservableList<Message> messages = FXCollections.observableArrayList();

    public Session() {
    }

    public Session(LocalDate date) {
        this.date = date;
    }
    public Session(LocalDate date, Message message) {
        this.date = date;
        messages.add(message);
    }
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ObservableList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages.addAll(messages);
    }
}
