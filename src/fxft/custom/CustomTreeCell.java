package fxft.custom;

import javafx.scene.control.TreeCell;
import fxft.data.NavigationTreeItemData;

public class CustomTreeCell extends TreeCell<NavigationTreeItemData> {

    @Override
    protected void updateItem(NavigationTreeItemData item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
        } else {
            setText(item.getName());
        }
    }
}
