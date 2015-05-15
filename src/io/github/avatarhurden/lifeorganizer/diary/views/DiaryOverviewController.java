package io.github.avatarhurden.lifeorganizer.diary.views;

import io.github.avatarhurden.lifeorganizer.diary.managers.EntryManager;
import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;
import io.github.avatarhurden.lifeorganizer.diary.views.EntryCellController;

import java.io.IOException;

import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import org.controlsfx.control.textfield.TextFields;

public class DiaryOverviewController {

	@FXML
	private ListView<DayOneEntry> entryList;
	@FXML
	private AnchorPane contentPane;

    @FXML
    private Label uuidLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private TextField searchField;
    
    private MarkdownEditor editor;
    
    private EntryManager manager;
    
    @FXML
    private void initialize() {
    	entryList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue == null)
    			return;
    		uuidLabel.setText(newValue.getUUID());
    		if (oldValue != null)
        	editor.textProperty().unbindBidirectional(oldValue.entryTextProperty());
			editor.textProperty().bindBidirectional(newValue.entryTextProperty());
    		editor.setText(newValue.getEntryText());
    		imageView.setImage(newValue.getImage());
    		System.out.println(newValue.getTags());
    	});
    	
    	
    	editor = new MarkdownEditor();

    	System.out.println(System.getProperty("user.timezone"));
    	
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

    	TextFields.bindAutoCompletion(searchField, manager.getTags());
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
	            //setText(item == null ? "null" : item.getEntryText().substring(0, 10));
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/avatarhurden/lifeorganizer/diary/views/EntryCell.fxml"));
	            try {
					setGraphic(loader.load());
					loader.<EntryCellController>getController().setContent(item);
					loader.<EntryCellController>getController().setWidth(entryList.getPrefWidth() - 20);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
		
	}
	
}
