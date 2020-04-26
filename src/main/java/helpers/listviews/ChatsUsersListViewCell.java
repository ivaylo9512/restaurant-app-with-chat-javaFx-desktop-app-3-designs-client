package helpers.listviews;

import models.ChatValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class ChatsUsersListViewCell extends ListCell<ChatValue> {

    @Override
    protected void updateItem(ChatValue chat, boolean empty) {
        super.updateItem(chat, empty);
        if(empty || chat == null){
            setGraphic(null);
            setText(null);
        }else{
            ImageView imageView = new ImageView(chat.getSecondUserPicture());
            imageView.setFitHeight(44);
            imageView.setFitWidth(44);
            imageView.setLayoutX(3);
            imageView.setLayoutY(6);

            Pane imageContainer = new Pane(imageView);
            Circle clip = new Circle(25, 25, 25);
            imageContainer.setClip(clip);
            imageContainer.setMinHeight(50);
            imageContainer.setMinWidth(50);
            imageContainer.setMaxHeight(50);
            imageContainer.setMaxWidth(50);


            Pane shadow = new Pane(imageContainer);
            shadow.setMinHeight(50);
            shadow.setMinWidth(50);
            shadow.setMaxHeight(50);
            shadow.setMaxWidth(50);
            shadow.getStyleClass().add("imageShadow");
            shadow.setId(String.valueOf(chat.getChatId()));

            HBox container = new HBox(shadow);
            container.setAlignment(Pos.CENTER);
            container.setPadding(new Insets(5, 0,5,0));

            setText(null);
            setGraphic(container);
        }
    }
}
