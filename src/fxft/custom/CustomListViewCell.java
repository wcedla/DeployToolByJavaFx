package fxft.custom;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import fxft.data.ListViewCellData;

public class CustomListViewCell<T> extends ListCell<T> {

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        CheckBox checkBox = new CheckBox();
        if (item != null && !empty) {
            checkBox.setMinWidth(getParent().getScene().getWidth());
            ListViewCellData listViewCellData = (ListViewCellData) item;
            checkBox.setText(listViewCellData.getName());
            checkBox.setSelected(listViewCellData.isSelected());
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                listViewCellData.setSelected(newValue);
                CustomListViewCell.this.getListView().getSelectionModel().select(item);
                CustomListViewCell.this.getListView().requestFocus();
            });
            setGraphic(checkBox);
        }
    }
}
