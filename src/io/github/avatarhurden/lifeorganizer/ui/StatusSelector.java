package io.github.avatarhurden.lifeorganizer.ui;

import io.github.avatarhurden.lifeorganizer.objects.Status;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
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
	
		bindGraphics();
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
		
		ListView<Status> list = new ListView<Status>();
		list.getStylesheets().add("/style/TaskCell.css");
		
		list.getItems().add(Status.ACTIVE);
		list.getItems().add(Status.DONE);
		list.getItems().add(Status.CANCELED);
		
		list.setCellFactory(tree -> new StatusFactory());
		
		list.setPrefHeight((6+getActiveNode(true).getPrefHeight())*3+20);
		list.setPrefWidth(getCanceledNode(true).getPrefWidth() + 20);
		
		list.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			pop.hide();
			setStatus(newValue);
		});
		pop.setAutoHide(true);

		pop.setArrowLocation(ArrowLocation.TOP_CENTER);
		pop.setContentNode(list);
		pop.show(this);
	}
	
	private class StatusFactory extends ListCell<Status> {
		@Override
		public void updateItem(Status status, boolean isEmpty) {
			super.updateItem(status, isEmpty);
			
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			if (isEmpty)
				setGraphic(null);
			else 
				switch (status) {
				case ACTIVE:
					setGraphic(getActiveNode(true));
					break;
				case DONE:
					setGraphic(getDoneNode(true));
					break;
				case CANCELED:
					setGraphic(getCanceledNode(true));
					break;
				}
		}
	}
	
	private void bindGraphics() {
		BooleanBinding isActive = Bindings.createBooleanBinding(() -> status.getValue() == Status.ACTIVE, status);
		BooleanBinding isDone = Bindings.createBooleanBinding(() -> status.getValue() == Status.DONE, status);
		
		graphicProperty().bind(
				Bindings.when(isActive).then(getActiveNode(showText.getValue())).
				otherwise(Bindings.when(isDone).then(getDoneNode(showText.getValue())).
				otherwise(getCanceledNode(showText.getValue()))));
	}
	
	private HBox getActiveNode(boolean text) {
		return getStatusNode("/style/active.png", "Active", text);
	}
	
	private HBox getDoneNode(boolean text) {
		return getStatusNode("/style/completed.png", "Done", text);
	}
	
	private HBox getCanceledNode(boolean text) {
		return getStatusNode("/style/canceled.png", "Canceled", text);
	}
	
	private HBox getStatusNode(String imageUrl, String text, boolean hasText) {
		HBox box = new HBox(5);
		box.setAlignment(Pos.CENTER_LEFT);
		
		Image image = new Image(imageUrl);
		
		box.getChildren().add(new ImageView(image));
		if (hasText) 
			box.getChildren().add(new Label(text));
		
		box.prefHeightProperty().bind(image.heightProperty());
		box.prefWidthProperty().bind(image.widthProperty().add(hasText ? 60 : 0));
		return box;
	}
	
}
