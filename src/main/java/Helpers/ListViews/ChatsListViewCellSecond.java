package Helpers.ListViews;

import Animations.TransitionResizeWidth;
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
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import sample.LoggedThirdStyle;

import java.io.IOException;
import java.util.List;

import static Helpers.ServerRequests.loggedUser;

public class ChatsListViewCellSecond extends ListCell<Chat> {
    @FXML
    private Label name;
    @FXML
    private Pane profileImageClip;
    @FXML
    private ImageView profileImage;
    @FXML
    private GridPane grid;
    @FXML
    private VBox nameContainer;

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

            widthAnimation();

            grid.setOnMouseClicked(event -> LoggedThirdStyle.controller.setChat(event));
            grid.setId(String.valueOf(chat.getId()));

            name.setText(user.getFirstName() + " " + user.getLastName());

            profileImage.setImage(user.getImage());
            Circle clip = new Circle(23.5, 23.5, 23.5);
            profileImageClip.setClip(clip);

            setText(null);
            setGraphic(grid);
        }

    }

    private void widthAnimation() {
        grid.setOnMouseEntered(event -> {
            if(nameContainer.getPrefWidth() >= 100) {
                TransitionResizeWidth resizeWidth = new TransitionResizeWidth(Duration.millis(250), nameContainer, 100);
                resizeWidth.play();
            }
        });
        grid.setOnMouseExited(event -> {
            if(nameContainer.getPrefWidth() >= 100) {
                TransitionResizeWidth resizeWidth = new TransitionResizeWidth(Duration.millis(250), nameContainer, 133);
                resizeWidth.play();
            }
        });
    }
}
