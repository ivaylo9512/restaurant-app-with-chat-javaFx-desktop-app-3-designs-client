package helpers.listviews;

import javafx.scene.layout.StackPane;
import models.Menu;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class MenuListViewCell extends ListCell<Menu> {
    @FXML
    private Label price;
    @FXML
    private Label name;

    @FXML
    private StackPane stackPane;

    private FXMLLoader fxmlLoader;

    @Override
    protected void updateItem(Menu menu, boolean empty) {
        super.updateItem(menu, empty);

        if(empty || menu == null) {

            setText(null);
            setGraphic(null);

        } else {

            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/cells/menu-cell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            price.setText(String.valueOf(menu.getId()));
            name.setText(menu.getName());

            setText(null);
            setGraphic(stackPane);
        }

    }
}
