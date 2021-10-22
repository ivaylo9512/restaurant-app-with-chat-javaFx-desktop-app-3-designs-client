package models;

import helpers.ObservableOrderedMap;
import javafx.scene.image.Image;
import java.time.LocalDate;

public class ChatValue {
    private long userId;
    private long chatId;

    private boolean moreSessions = true;
    private int displayedSessions;

    private Image secondUserPicture;
    private User secondUser;
    private ObservableOrderedMap<LocalDate, Session> sessionsObservable = new ObservableOrderedMap<>();

    public ChatValue(long chatId, long userId, Image profilePicture, User secondUser) {
        this.chatId = chatId;
        this.userId = userId;
        this.secondUserPicture = profilePicture;
        this.secondUser = secondUser;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public Image getSecondUserPicture() {
        return secondUserPicture;
    }

    public void setSecondUserPicture(Image profilePicture) {
        this.secondUserPicture = profilePicture;
    }

    public ObservableOrderedMap<LocalDate, Session> getSessions() {
        return sessionsObservable;
    }

    public void setSessions(ObservableOrderedMap<LocalDate, Session> sessions) {
        this.sessionsObservable = sessions;
    }

    public boolean isMoreSessions() {
        return moreSessions;
    }

    public void setMoreSessions(boolean moreSessions) {
        this.moreSessions = moreSessions;
    }

    public int getDisplayedSessions() {
        return displayedSessions;
    }

    public void setDisplayedSessions(int displayedSessions) {
        this.displayedSessions = displayedSessions;
    }

    public User getSecondUser() {
        return secondUser;
    }

    public void setSecondUser(User secondUser) {
        this.secondUser = secondUser;
    }
}
