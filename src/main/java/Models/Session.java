package Models;

import java.time.LocalDate;
import java.util.List;

public class Session{

    private LocalDate date;
    private List<String> messages;
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
}
