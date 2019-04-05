package Helpers.ListViews;

import Models.Chat;
import Models.Menu;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import sample.ControllerLoggedSecondStyle;

import java.io.IOException;

public class ChatsListViewCell extends ListCell<Chat> {
    @FXML
    private Label lastMessage;
    @FXML
    private Label name;
    @FXML
    private Pane profileImageClip;
    @FXML
    ImageView profileImage;
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
                mLLoader = new FXMLLoader(getClass().getResource("/FXML/chat-cell.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            name.setText("Ivaylo Aleksandrov");
            lastMessage.setText("Hello I am Ivaylo Aleksandrov");
            profileImage.setImage(ControllerLoggedSecondStyle.userProfileImage);
            Circle clip = new Circle(23.08, 23.08, 23.08);
            profileImageClip.setClip(clip);

            setText(null);
            setGraphic(grid);
        }

    }
}
