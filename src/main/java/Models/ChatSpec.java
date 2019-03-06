package Models;

import javafx.scene.image.Image;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class ChatSpec {
    private int userId;
    private int chatId;

    private boolean moreSessions = true;
    private int displayedSessions;

    private Image secondUserPicture;
    private LinkedHashMap<LocalDate, Session> sessions = new LinkedHashMap<>();

    public ChatSpec(int chatId, int userId, Image profilePicture) {
        this.chatId = chatId;
        this.userId = userId;
        this.secondUserPicture = profilePicture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatSpec)) return false;
        ChatSpec that = (ChatSpec) o;
        return Objects.equals(getUserId(), that.getUserId());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getUserId());
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

    public LinkedHashMap<LocalDate, Session> getSessions() {
        return sessions;
    }

    public void setSessions(LinkedHashMap<LocalDate, Session> sessions) {
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
}
