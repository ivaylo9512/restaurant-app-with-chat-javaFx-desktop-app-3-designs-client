package Helpers.ListViews;

import Models.Chat;
import Models.Message;
import Models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.List;

import static Helpers.ServerRequests.loggedUser;

public class ChatsListViewCellSecond extends ListCell<Chat> {
    @FXML
    private Label lastMessage;
    @FXML
    private Label name;
    @FXML
    private Pane profileImageClip;
    @FXML
    private ImageView profileImage;
    @FXML
    private GridPane grid;

    private FXMLLoader mLLoader;

    @Override
    protected void updateItem(Chat chat, boolean empty) {
        super.updateItem(chat, empty);

        if(empty || chat == null) {

            setText(null);
            setGraphic(null);

        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("/FXML/cells/chat-cell-second.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            User user;
            if(chat.getFirstUser().getId() == loggedUser.getId()){
                user = chat.getSecondUser();
            }else{
                user = chat.getFirstUser();
            }


            if(chat.getSessions().size() > 0) {
                List<Message> lastSessionMessages = chat.getSessions().get(0).getMessages();
                if (lastSessionMessages.size() > 0) {
                    Message last = lastSessionMessages.get(lastSessionMessages.size() - 1);
                    lastMessage.setText(last.getMessage());
                }
            }
            lastMessage.setId("lastMessage"+ chat.getId());
            name.setText(user.getFirstName() + " " + user.getLastName());

            profileImage.setImage(user.getImage());
            Rectangle clip = new Rectangle(23.5, 23.5);
            profileImageClip.setClip(clip);

            setText(null);
            setGraphic(grid);
        }

    }
}
