package io.github.avatarhurden.lifeorganizer.ui;

import io.github.avatarhurden.lifeorganizer.objects.Status;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

public class StatusSelector extends Label {
	
	private Property<Status> status;
	
	public StatusSelector() {
		status = new SimpleObjectProperty<Status>();
		status.addListener((obs, oldValue, newValue) -> {
			System.out.println("internal " + newValue);
		});
		
		BooleanBinding isActive = Bindings.createBooleanBinding(() -> status.getValue() == Status.ACTIVE, status);
		BooleanBinding isDone = Bindings.createBooleanBinding(() -> status.getValue() == Status.DONE, status);
		
		graphicProperty().bind(Bindings.when(isActive).then(getActiveNode()).
				otherwise(Bindings.when(isDone).then(getDoneNode()).otherwise(getCanceledeNode())));

		setOnMouseClicked(event -> openValueSelector());
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
	
	private void openValueSelector() {
		PopOver pop = new PopOver();
		
		ListView<Status> list = new ListView<Status>();
		list.getItems().add(Status.ACTIVE);
		list.getItems().add(Status.DONE);
		list.getItems().add(Status.CANCELED);
		list.setCellFactory(tree -> new StatusFactory());
		list.setPrefHeight((6+getActiveNode().getPrefHeight())*3+2);
		list.setPrefWidth(getActiveNode().getPrefWidth());
		
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
					setGraphic(getActiveNode());
					break;
				case DONE:
					setGraphic(getDoneNode());
					break;
				case CANCELED:
					setGraphic(getCanceledeNode());
					break;
				}
		}
	}
	
	private HBox getActiveNode() {
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.setOnMouseEntered(event -> {
			box.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
		});
		box.setOnMouseExited(event -> {
			box.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
		});
		box.getChildren().add(new ImageView(new Image("/style/inbox.png")));
		box.getChildren().add(new Label("Active"));
		box.prefHeightProperty().bind(new Image("/style/inbox.png").heightProperty());
		return box;
	}
	
	private HBox getDoneNode() {
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.setOnMouseEntered(event -> {
			box.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
		});
		box.setOnMouseExited(event -> {
			box.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
		});
		box.getChildren().add(new ImageView(new Image("/style/inbox.png")));
		box.getChildren().add(new Label("Done"));
		box.prefHeightProperty().bind(new Image("/style/inbox.png").heightProperty());
		return box;
	}
	
	private HBox getCanceledeNode() {
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.setOnMouseEntered(event -> {
			box.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
		});
		box.setOnMouseExited(event -> {
			box.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
		});
		box.getChildren().add(new ImageView(new Image("/style/inbox.png")));
		box.getChildren().add(new Label("Canceled"));
		box.prefHeightProperty().bind(new Image("/style/inbox.png").heightProperty());
		return box;
	}
	
}
