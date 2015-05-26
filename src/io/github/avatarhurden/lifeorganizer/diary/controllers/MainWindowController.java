package io.github.avatarhurden.lifeorganizer.diary.controllers;

import io.github.avatarhurden.lifeorganizer.diary.managers.EntryManager;
import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;

import java.io.IOException;

import javafx.collections.ListChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class MainWindowController {

	@FXML private AnchorPane contentPane;
	
	@FXML private ToggleButton newButton, listButton;
	private ToggleGroup group;
	
	// Creation view
	private AnchorPane entryView;
	private EntryViewController entryViewController;
	
	// List View
	private ListView<DayOneEntry> listView;
	private BorderPane entryListView;
	private ListEntryViewController entryListViewController;
	
	
	private EntryManager manager;

	public void setDiaryManager(EntryManager manager) {
		this.manager = manager;
		
		SortedList<DayOneEntry> sorted = new SortedList<DayOneEntry>(manager.getEntries());
		// Compares opposite so that later entries are on top
		sorted.setComparator((entry1, entry2) -> entry2.compareTo(entry1));
		listView.setItems(sorted);
		listView.getItems().addListener((ListChangeListener.Change<? extends DayOneEntry> event) -> {
			event.next();
			if (event.wasRemoved()) 
				listView.getSelectionModel().clearSelection();
		});
	}
	
	@FXML
	private void initialize() {
		group = new ToggleGroup();
		newButton.setToggleGroup(group);
		listButton.setToggleGroup(group);
		
		group.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == null)
				group.selectToggle(oldValue);
		});
		
		listView = new ListView<DayOneEntry>();
		listView.setCellFactory(table -> new EntryCell());
		AnchorPane.setTopAnchor(listView, 0d);
    	AnchorPane.setRightAnchor(listView, 0d);
    	AnchorPane.setBottomAnchor(listView, 0d);
    	AnchorPane.setLeftAnchor(listView, 0d);
    	
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EntryView.fxml"));
    	try {
    		entryView = loader.<AnchorPane>load();
	    	AnchorPane.setTopAnchor(entryView, 0d);
	    	AnchorPane.setRightAnchor(entryView, 0d);
	    	AnchorPane.setBottomAnchor(entryView, 0d);
	    	AnchorPane.setLeftAnchor(entryView, 0d);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	entryViewController = loader.<EntryViewController>getController();
    	
    	loader = new FXMLLoader(getClass().getResource("/fxml/ListEntryView.fxml"));
    	try {
    		entryListView = loader.<BorderPane>load();
	    	AnchorPane.setTopAnchor(entryListView, 0d);
	    	AnchorPane.setRightAnchor(entryListView, 0d);
	    	AnchorPane.setBottomAnchor(entryListView, 0d);
	    	AnchorPane.setLeftAnchor(entryListView, 0d);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	entryListViewController = loader.<ListEntryViewController>getController();
    	entryListViewController.setOnHomeClicked(() -> contentPane.getChildren().setAll(listView));
	}
	
	@FXML
	private void showNewEntry() {
		if (entryViewController.getEntry() != null && entryViewController.getEntry().isEmpty())
			manager.deleteEntry(entryViewController.getEntry());
		entryViewController.setEntry(manager.addEntry());
		contentPane.getChildren().setAll(entryView);
	}
	
	@FXML
	private void showEntryList() {
		contentPane.getChildren().setAll(listView);
	}
	
	private class EntryCell extends ListCell<DayOneEntry> {
		
		@Override public void updateItem(DayOneEntry item, boolean empty) {
	        super.updateItem(item, empty);
	 
	        setOnMouseClicked(event -> {
	        	if (event.getClickCount() == 2) {
	        		entryListViewController.setCurrentEntry(item);
	        		contentPane.getChildren().setAll(entryListView);
	        	}
	        });
	        
	        if (empty) {
	            setText(null);
	            setGraphic(null);
	        } else {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EntryCell.fxml"));
	            try {
					Node n = loader.load();
					setGraphic(n);
					setPadding(Insets.EMPTY);
					loader.<EntryCellController>getController().setContent(item);
					loader.<EntryCellController>getController().setWidth(listView.getPrefWidth() - 20);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
		
	}
}
