package io.github.avatarhurden.lifeorganizer.ui;

import io.github.avatarhurden.lifeorganizer.objects.Status;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

public class StatusSelector extends Button {
	
	private Property<Status> status;
	
	private Property<Boolean> showText;
	
	public StatusSelector() {
		this(true);
	}
	
	public StatusSelector(boolean show) {
		showText = new SimpleBooleanProperty(show);
		status = new SimpleObjectProperty<Status>();
		
		getStylesheets().add("/style/TaskCell.css");
		getStyleClass().add("statusButton");
		
		setOnAction(event -> openValueSelector());
		
		statusProperty().addListener((obs, oldValue, newValue) -> setGraphic(getStatusNode(newValue, showText.getValue())));
	}
	
	public Property<Status> statusProperty() {
		return status;
	}

	public Status getStatus() {
		return status.getValue();
	}

	public void setStatus(Status status) {
		this.status.setValue(status);
	}
	
	public Property<Boolean> showTextProperty() {
		return showText;
	}

	public Boolean getShowText() {
		return showText.getValue();
	}

	public void setShowText(Boolean showText) {
		this.showText.setValue(showText);
	}
	
	private void openValueSelector() {
		PopOver pop = new PopOver();
		pop.setDetachable(false);
		pop.setAutoHide(true);
		pop.setArrowLocation(ArrowLocation.TOP_CENTER);
		
		ListView<Status> list = new ListView<Status>();
		
		for (Status status : Status.values())
			list.getItems().add(status);
		
		list.setCellFactory(tree -> new StatusFactory());
		
		list.setPrefHeight((6 + getStatusNode(Status.COMPLETED, true).getPrefHeight())*3 + 20);
		list.setPrefWidth(getStatusNode(Status.COMPLETED, true).getPrefWidth() + 30);
		
		list.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			pop.hide();
			setStatus(newValue);
		});

		pop.setContentNode(list);
		pop.show(this);
	}
	
	private class StatusFactory extends ListCell<Status> {
		{ setContentDisplay(ContentDisplay.GRAPHIC_ONLY); }
		
		@Override
		public void updateItem(Status status, boolean isEmpty) {
			super.updateItem(status, isEmpty);
			
			if (isEmpty)
				setGraphic(null);
			else 
				setGraphic(getStatusNode(status, true));
		}
	}
	
	private HBox getStatusNode(Status status, boolean hasText) {
		HBox box = new HBox(5);
		box.setAlignment(Pos.CENTER_LEFT);
		
		Image image = status.getImage();
		
		box.getChildren().add(new ImageView(image));
		if (hasText) 
			box.getChildren().add(new Label(status.getName()));
		
		box.prefHeightProperty().bind(image.heightProperty());
		box.prefWidthProperty().bind(image.widthProperty().add(hasText ? 60 : 0));
		return box;
	}
	
}
