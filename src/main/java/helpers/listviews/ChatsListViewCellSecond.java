package helpers.listviews;

import animations.TransitionResizeWidth;
import controllers.base.ControllerLogged;
import controllers.secondstyle.LoggedSecond;
import controllers.thirdstyle.LoggedThird;
import models.Chat;
import models.ChatValue;
import models.User;
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

import java.io.IOException;

import static application.RestaurantApplication.stageManager;


public class ChatsListViewCellSecond extends ListCell<ChatValue> {
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
                fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/cells/chat-cell-second.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            User user = chat.getSecondUser();

            grid.setId(String.valueOf(chat.getChatId()));
            name.setText(user.getFirstName() + " " + user.getLastName());
            profileImage.setImage(user.getImage().get());
            profileImageClip.setClip(clip);

            setText(null);
            setGraphic(grid);
        }

    }

    @FXML
    public void expandWidth(){
        if(nameContainer.getPrefWidth() >= 100) {
            TransitionResizeWidth resize = new TransitionResizeWidth(Duration.millis(250), nameContainer, 100);
            resize.play();
        }
    }
    @FXML
    private void reverseWidth() {
        if(nameContainer.getPrefWidth() >= 100) {
            TransitionResizeWidth reverse = new TransitionResizeWidth(Duration.millis(250), nameContainer, 133);
            reverse.play();
        }
    }

    {
        this.selectedProperty().addListener(observable -> {
            if(stageManager.currentController instanceof LoggedThird && stageManager.thirdLoggedStage.isShowing()){
                ((ControllerLogged)stageManager.thirdLoggedController).setMainChat(getItem());
            }
        });
    }
}
