package Models;

import javafx.scene.image.Image;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.time.LocalDate;

public class ChatValue {
    private int userId;
    private int chatId;

    private boolean moreSessions = true;
    private int displayedSessions;

    private Image secondUserPicture;
    private User secondUser;
    private ListOrderedMap<LocalDate, Session> sessions = new ListOrderedMap<>();

    public ChatValue(int chatId, int userId, Image profilePicture, User secondUser) {
        this.chatId = chatId;
        this.userId = userId;
        this.secondUserPicture = profilePicture;
        this.secondUser = secondUser;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getChatId() {
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

    public ListOrderedMap<LocalDate, Session> getSessions() {
        return sessions;
    }

    public void setSessions(ListOrderedMap<LocalDate, Session> sessions) {
        this.sessions = sessions;
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
