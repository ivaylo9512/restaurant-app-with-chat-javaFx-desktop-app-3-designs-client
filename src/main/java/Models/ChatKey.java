package Models;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Objects;

public class ChatKey {
    private int userId;
    private int chatId;
    private boolean moreSessions = true;
    private Image profilePicture;
    public ChatKey(int chatId, int userId, Image profilePicture) {
        this.chatId = chatId;
        this.userId = userId;
        this.profilePicture = profilePicture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatKey)) return false;
        ChatKey that = (ChatKey) o;
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

    public Image getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Image profilePicture) {
        this.profilePicture = profilePicture;
    }
}
