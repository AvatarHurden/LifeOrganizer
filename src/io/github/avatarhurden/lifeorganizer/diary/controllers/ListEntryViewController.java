package io.github.avatarhurden.lifeorganizer.diary.controllers;

import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class ListEntryViewController {

	@FXML
	private SVGPath previousButton, homeButton, nextButton;
	@FXML
	private AnchorPane contentPane;
	
	private EntryViewController entryViewController;
	private AnchorPane entryView;
	
	private DayOneEntry entry;
	
	@FXML
	private void initialize() {
		previousButton.strokeProperty().bind(Bindings.when(previousButton.hoverProperty()).then(Color.BLUE).otherwise(Color.TRANSPARENT));
		
		nextButton.strokeProperty().bind(Bindings.when(nextButton.hoverProperty()).then(Color.BLUE).otherwise(Color.TRANSPARENT));

		homeButton.strokeProperty().bind(Bindings.when(homeButton.hoverProperty()).then(Color.BLUE).otherwise(Color.TRANSPARENT));
		
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
    	
		contentPane.getChildren().setAll(entryView);
	}
	
	public void setCurrentEntry(DayOneEntry entry) {
		this.entry = entry;
		
		entryViewController.setEntry(entry);
	}
	
	public void setOnHomeClicked(Runnable action) {
		homeButton.setOnMouseClicked(event -> action.run());
	}
	
}
