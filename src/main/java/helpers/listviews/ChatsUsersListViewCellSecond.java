package helpers.listviews;

import controllers.base.ControllerLogged;
import controllers.firststyle.LoggedFirst;
import controllers.secondstyle.LoggedSecond;
import models.ChatValue;
import models.Message;
import models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import java.io.IOException;
import java.util.List;

import static application.RestaurantApplication.stageManager;


public class ChatsUsersListViewCellSecond extends ListCell<ChatValue> {
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

    private Circle clip = new Circle(23.5, 23.5, 23.5);
    private FXMLLoader fxmlLoader;

    @Override
    protected void updateItem(ChatValue chat, boolean empty) {
        super.updateItem(chat, empty);

        if(empty || chat == null) {

            setText(null);
            setGraphic(null);

        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/cells/chat-user-cell-second.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            if(chat.getSessions().size() > 0) {
                List<Message> lastSessionMessages = chat.getSessions().getValue(0).getMessages();
                if (lastSessionMessages.size() > 0) {
                    Message last = lastSessionMessages.get(lastSessionMessages.size() - 1);
                    lastMessage.setText(last.getMessage());
                }
            }

            User user = chat.getSecondUser();
            lastMessage.setId("lastMessage"+ chat.getChatId());
            name.setText(user.getFirstName().get() + " " + user.getLastName().get());

            profileImage.setImage(chat.getSecondUserPicture());
            profileImageClip.setClip(clip);

            setText(null);
            setGraphic(grid);
        }
    }

    {
        this.selectedProperty().addListener(observable -> {
            if(stageManager.currentController instanceof LoggedSecond && stageManager.secondLoggedStage.isShowing()){
                ((ControllerLogged)stageManager.secondLoggedController).setMainChat(getItem());
            }
        });
    }
}
