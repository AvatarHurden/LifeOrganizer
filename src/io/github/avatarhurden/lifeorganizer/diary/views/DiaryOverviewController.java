package io.github.avatarhurden.lifeorganizer.diary.views;

import io.github.avatarhurden.lifeorganizer.diary.managers.EntryManager;
import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class DiaryOverviewController {

	@FXML
	private ListView<DayOneEntry> entryList;
	@FXML
	private AnchorPane contentPane;

    @FXML
    private Label uuidLabel;
    @FXML
    private ImageView imageView;

    private MarkdownEditor editor;
    
    private EntryManager manager;
    
    @FXML
    private void initialize() {
    	entryList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue == null)
    			return;
    		uuidLabel.setText(newValue.getUUID());
    		editor.setText(newValue.getEntryText());
    		imageView.setImage(newValue.getImage());
    	});
    	
    	editor = new MarkdownEditor();
    	
    	AnchorPane.setTopAnchor(editor, 100d);
		AnchorPane.setBottomAnchor(editor, 0d);
		AnchorPane.setLeftAnchor(editor, 0d);
		AnchorPane.setRightAnchor(editor, 0d);
		
		contentPane.getChildren().add(editor);
    }

	public void setDiaryManager(EntryManager manager) {
		this.manager = manager;
		SortedList<DayOneEntry> sorted = new SortedList<DayOneEntry>(manager.getEntries());
		// Compares opposite so that later entries are on top
		sorted.setComparator((entry1, entry2) -> entry2.compareTo(entry1));
		entryList.setItems(sorted);
	
		entryList.setCellFactory(table -> new EntryCell());
	}
	
	@FXML
	private void createEntry() {
		manager.addEntry();
	}
	
	private class EntryCell extends ListCell<DayOneEntry> {
		
		@Override public void updateItem(DayOneEntry item, boolean empty) {
	        super.updateItem(item, empty);
	 
	        if (empty) {
	            setText(null);
	            setGraphic(null);
	        } else {
	            setText(item == null ? "null" : item.getUUID());
	            setGraphic(null);
	        }
	    }
		
	}
	
}
