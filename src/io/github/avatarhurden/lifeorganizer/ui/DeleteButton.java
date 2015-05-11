package io.github.avatarhurden.lifeorganizer.ui;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class DeleteButton extends StackPane {

	private Property<EventHandler<ActionEvent>> eventHandler;
	
	public DeleteButton() {
		eventHandler = new SimpleObjectProperty<EventHandler<ActionEvent>>();
		
		Region region = new Region();
		region.getStylesheets().add("/style/buttons.css");
		region.getStyleClass().addAll("graphic");
	       
		getChildren().add(region);
		
		getStyleClass().addAll("delete");
	    
	    region.setOnMouseClicked(event -> {
	    	if (eventHandler.getValue() != null) 
	    		eventHandler.getValue().handle(new ActionEvent(this, this));
	    });
	}
	
	public void setOnAction(EventHandler<ActionEvent> value) {
		eventHandler.setValue(value);
	}
	
	public EventHandler<ActionEvent> getOnAction() {
		return eventHandler.getValue();
	}
	
	public Property<EventHandler<ActionEvent>> onActionProperty() {
		return eventHandler;
	}
}
