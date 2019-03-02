package Models;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Message {
    private int receiver;

    private LocalTime date;

    private String message;

    public Message() {
    }

    public Message(int receiver, LocalTime date, String message) {
        this.receiver = receiver;
        this.date = date;
        this.message = message;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public LocalTime getDate() {
        return date;
    }

    public void setDate(LocalTime date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
