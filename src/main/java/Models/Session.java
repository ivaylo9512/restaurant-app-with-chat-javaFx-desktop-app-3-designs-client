package Models;

import java.time.LocalDate;
import java.util.List;

public class Session{

    private LocalDate date;
    private List<Message> messages;
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