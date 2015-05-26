package io.github.avatarhurden.lifeorganizer.diary.controllers;

import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class ListEntryViewController {

	@FXML
	private SVGPath previousButton, homeButton, nextButton;
	@FXML
	private HBox buttonBar;
	@FXML
	private AnchorPane contentPane;
	@FXML
	private BorderPane borderPane;
	
	private EntryViewController entryViewController;
	private AnchorPane entryView;
	
	ListView<DayOneEntry> listView;
	
	SimpleIntegerProperty listSize;
	
	@FXML
	private void initialize() {
		listSize = new SimpleIntegerProperty();
		
		listView = new ListView<DayOneEntry>();
		
		listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			entryViewController.setEntry(newValue);
		});
		
		listView.setCellFactory(table -> new EntryCell());
		AnchorPane.setTopAnchor(listView, 0d);
    	AnchorPane.setRightAnchor(listView, 0d);
    	AnchorPane.setBottomAnchor(listView, 0d);
    	AnchorPane.setLeftAnchor(listView, 0d);
		
		previousButton.strokeProperty().bind(Bindings.when(previousButton.hoverProperty()).then(Color.BLUE).otherwise(Color.TRANSPARENT));
		nextButton.strokeProperty().bind(Bindings.when(nextButton.hoverProperty()).then(Color.BLUE).otherwise(Color.TRANSPARENT));
		homeButton.strokeProperty().bind(Bindings.when(homeButton.hoverProperty()).then(Color.BLUE).otherwise(Color.TRANSPARENT));
		
		nextButton.fillProperty().bind(Bindings.when(nextButton.disableProperty()).then(Color.LIGHTGRAY).otherwise(Color.BLACK));
		previousButton.fillProperty().bind(Bindings.when(previousButton.disableProperty()).then(Color.LIGHTGRAY).otherwise(Color.BLACK));
		
		previousButton.disableProperty().bind(listView.getSelectionModel().selectedIndexProperty().isEqualTo(0));
		nextButton.disableProperty().bind(listView.getSelectionModel().selectedIndexProperty().isEqualTo(listSize.subtract(1)));
		
		homeButton.setOnMouseClicked(event -> showList());
    	previousButton.setOnMouseClicked(event -> listView.getSelectionModel().selectPrevious());
    	nextButton.setOnMouseClicked(event -> listView.getSelectionModel().selectNext());
		
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
    	
    	showList();
	}
	
	public void setItems(ObservableList<DayOneEntry> items) {
		listView.setItems(items);
		items.addListener((ListChangeListener.Change<? extends DayOneEntry> event) -> {
			event.next();
			if (event.wasRemoved()) 
				listView.getSelectionModel().clearSelection();
		});
		listView.scrollTo(items.size() - 1);
		listSize.bind(Bindings.size(items));
	}
	
	public void showList() {
		contentPane.getChildren().setAll(listView);
		buttonBar.setManaged(false);
		buttonBar.setVisible(false);
	}
	
	private void showSingle() {
		contentPane.getChildren().setAll(entryView);
		buttonBar.setManaged(true);
		buttonBar.setVisible(true);
	}
	
	private class EntryCell extends ListCell<DayOneEntry> {
		
		@Override public void updateItem(DayOneEntry item, boolean empty) {
	        super.updateItem(item, empty);
	 
	        setOnMouseClicked(event -> {
	        	if (event.getClickCount() == 2)
	        		showSingle();
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
