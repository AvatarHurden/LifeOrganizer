package io.github.avatarhurden.lifeorganizer.diary.views;

import io.github.avatarhurden.lifeorganizer.diary.managers.EntryManager;
import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;
import io.github.avatarhurden.lifeorganizer.diary.models.Tag;
import io.github.avatarhurden.lifeorganizer.views.ObjectListView;

import java.io.IOException;

import javafx.collections.ListChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
	private AnchorPane contentPane, tagPane, editorPane;

    @FXML
    private ImageView imageView;
    @FXML
    private TextField searchField;
    
    private AnchorPane entryView;
    private EntryViewController controller;
    
    private ObjectListView<Tag> tagList;
    
    private MarkdownEditor editor;
    
    private EntryManager manager;
    
    @FXML
    private void initialize() {
    	entryList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue == null)
    			contentPane.getChildren().clear();
    		else {
    			contentPane.getChildren().setAll(entryView);
    			controller.setEntry(newValue);
    		}
    		
//    		if (oldValue != null)
//    			editor.textProperty().unbindBidirectional(oldValue.entryTextProperty());
//			editor.textProperty().bindBidirectional(newValue.entryTextProperty());
//			editor.setText(newValue.getEntryText());
//    		
//    		imageView.setImage(newValue.getImage());
//    		
//    		ObservableList<Tag> tags = FXCollections.observableArrayList();
//    		for (String tag : newValue.getTags())
//    			tags.add(manager.getTag(tag));
//    		tagList.setList(tags);
//    		
//    		tagList.setDeletionPolicy(tag -> newValue.removeTag(tag.getName()));
//    		tagList.setCreationPolicy(tag -> newValue.addTag(tag) ? manager.getTag(tag) : null);
    	});
    	
//    	imageView.managedProperty().bind(imageView.imageProperty().isNotNull());
//		imageView.setCursor(Cursor.HAND);
//		imageView.setOnMouseClicked(event -> {
//			if (event.getClickCount() != 2)
//				return;
//			ImageView full = new ImageView(imageView.getImage());
//			ScrollPane pane = new ScrollPane(full);
//			
//			final DoubleProperty zoom = new SimpleDoubleProperty(1.0);
//			zoom.addListener((obs, oldValue, newValue) -> {
//				double hvalue = pane.getHvalue();
//				double vvalue = pane.getVvalue();
//
//				full.setFitHeight(zoom.get()*imageView.getImage().getHeight());
//				full.setFitWidth(zoom.get()*imageView.getImage().getWidth());
//				
//				pane.setHvalue(hvalue);
//				pane.setVvalue(vvalue);
//			});
//			
//			Alert dialog = new Alert(AlertType.INFORMATION);
//			dialog.setHeaderText(null);
//			dialog.setGraphic(null);
//			
//			pane.setPannable(true);
//			pane.setHbarPolicy(ScrollBarPolicy.NEVER);
//			pane.setVbarPolicy(ScrollBarPolicy.NEVER);
//			
//			full.setOnScroll(value -> {
//				value.consume();
//				if (value.getDeltaY() > 0)
//					zoom.setValue(Math.min(10, 1.1*zoom.getValue()));
//				else if (value.getDeltaY() < 0)
//					zoom.setValue(Math.max(1, zoom.getValue() / 1.1));
//				
//				
//			});
//			
//			full.setPreserveRatio(true);
//			full.setFitHeight(imageView.getImage().getHeight());
//			full.setFitWidth(imageView.getImage().getWidth());
//			
//			dialog.getDialogPane().setContent(pane);
//			dialog.showAndWait();
//		});
//		
//    	editor = new MarkdownEditor();
//    	editorPane.getChildren().setAll(editor);
//    	AnchorPane.setTopAnchor(editor, 0d);
//    	AnchorPane.setRightAnchor(editor, 0d);
//    	AnchorPane.setBottomAnchor(editor, 0d);
//    	AnchorPane.setLeftAnchor(editor, 0d);
//    	
//    	tagList = new ObjectListView<Tag>(tag -> new SimpleStringProperty(tag.getName()));
//    	tagPane.getChildren().add(Borders.wrap(tagList).lineBorder().radius(6).innerPadding(8).color(Color.TRANSPARENT).title("Tags").build().build());
    	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/avatarhurden/lifeorganizer/diary/views/EntryView.fxml"));
    	
    	try {
    		entryView = loader.<AnchorPane>load();
	    	AnchorPane.setTopAnchor(entryView, 0d);
	    	AnchorPane.setRightAnchor(entryView, 0d);
	    	AnchorPane.setBottomAnchor(entryView, 0d);
	    	AnchorPane.setLeftAnchor(entryView, 0d);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	controller = loader.<EntryViewController>getController();
    	
    }

	public void setDiaryManager(EntryManager manager) {
		this.manager = manager;
		SortedList<DayOneEntry> sorted = new SortedList<DayOneEntry>(manager.getEntries());
		// Compares opposite so that later entries are on top
		sorted.setComparator((entry1, entry2) -> entry2.compareTo(entry1));
		entryList.setItems(sorted);
		entryList.getItems().addListener((ListChangeListener.Change<? extends DayOneEntry> event) -> {
			event.next();
			if (event.wasRemoved()) {
				entryList.getSelectionModel().clearSelection();
				System.out.println(event);
			}
		});
//    	tagList.setSuggestions(manager.getTags());

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
					Node n = loader.load();
					setGraphic(n);
					setPadding(Insets.EMPTY);
					loader.<EntryCellController>getController().setContent(item);
					loader.<EntryCellController>getController().setWidth(entryList.getPrefWidth() - 20);
					
//					selectedProperty().addListener((obs, oldValue, newValue) -> {
//						try {
//							if (newValue)
//								controller.setEntry(item);
//							setGraphic(newValue ? entryView : n);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} 
//					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
		
	}
	
}
