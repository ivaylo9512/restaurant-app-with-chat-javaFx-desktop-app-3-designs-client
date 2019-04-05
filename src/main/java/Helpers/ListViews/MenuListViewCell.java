package Helpers.ListViews;

import Models.Menu;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class MenuListViewCell extends ListCell<Menu> {
    @FXML
    private Label price;
    @FXML
    private Label name;

    @FXML
    private GridPane grid;

    private FXMLLoader mLLoader;

    @Override
    protected void updateItem(Menu menu, boolean empty) {
        super.updateItem(menu, empty);

        if(empty || menu == null) {

            setText(null);
            setGraphic(null);

        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("/FXML/menu-cell.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            price.setText(String.valueOf(menu.getId()));
            name.setText(menu.getName());
            grid.prefWidthProperty().bind(widthProperty().subtract(13));
            setText(null);
            setGraphic(grid);
        }

    }
}
