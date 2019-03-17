package Models;


import java.util.List;

public class Chat {

    private int id;

    private User firstUser;

    private User secondUser;

    private List<Session> sessions;

    public Chat() {
    }

    public Chat(User firstUser, User secondUser, List<Session> sessions) {
        this.firstUser = firstUser;
        this.secondUser = secondUser;
        this.sessions = sessions;
    }

    public User getFirstUser() {
        return firstUser;
    }

    public void setFirstUser(User firstUser) {
        this.firstUser = firstUser;
    }

    public User getSecondUser() {
        return secondUser;
    }

    public void setSecondUser(User secondUser) {
        this.secondUser = secondUser;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }
}
