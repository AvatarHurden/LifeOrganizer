package io.github.avatarhurden.lifeorganizer.views;

import javafx.animation.FadeTransition;
import javafx.beans.property.Property;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.controlsfx.control.textfield.TextFields;

public class ObjectListView<T> extends HBox {

	private Constructor<T> instantiator;
	private StringPropertyGetter<T> property;
	private Property<ObservableList<T>> objects;
	
	private TextField textField;
	
	public ObjectListView(Constructor<T> instantiator, StringPropertyGetter<T> property) {
		this.instantiator = instantiator;
		this.property = property;
		getStylesheets().add("/io/github/avatarhurden/lifeorganizer/views/style.css");
	
		initialize();
	}
	
	public void setPromptText(String text) {
		textField.setPromptText(text);
	}
	
	public void setList(Property<ObservableList<T>> objects) {
		// Clears the list of labels
		if (getChildren().size() > 1)
			getChildren().remove(0, getChildren().size() - 1);
		
		this.objects = objects;
		// Adds listener so that the property fires a changeEvent when the list fires an event
		ObservableList<T> list = objects.getValue();
		list.addListener((ListChangeListener.Change<? extends T> listener) -> this.objects.setValue(list));
		
		for (T object : objects.getValue())
			addLabel(object);
	}
	
	private void initialize() {
		textField = TextFields.createClearableTextField();
	
		HBox.setMargin(textField, new Insets(0, 5, 0, 5));
		setAlignment(Pos.CENTER_LEFT);
		
		textField.setOnAction((event) -> {
				T object = instantiator.newInstance(textField.getText());
				if (object != null) {
					objects.getValue().add(object);
					addLabel(object);
					textField.clear();
				}
		});
		getChildren().add(textField);
	}
	
	private void addLabel(T object) {
		HBox itemBox = new HBox(0);
		
		itemBox.setFocusTraversable(true);
		itemBox.setOnMousePressed((event) -> {
			event.consume();
			itemBox.requestFocus();
		});

		itemBox.getStyleClass().add("object-box");
		
		Label label = new Label();
		label.textProperty().bindBidirectional(property.getProperty(object));
		label.setPrefHeight(25);
		HBox.setMargin(label, new Insets(0, 5, 0, 0));
        itemBox.getChildren().add(label);
		
		Region clearButton = new Region();
        clearButton.getStyleClass().addAll("graphic");
        
        StackPane clearButtonPane = new StackPane(clearButton);
        clearButtonPane.getStyleClass().addAll("clear-button");
        clearButtonPane.setOnMouseReleased(event -> {
        	fadeObject(itemBox, false).setOnFinished(finished -> getChildren().remove(itemBox));
			objects.getValue().remove(object);
        });
        clearButtonPane.visibleProperty().bind(itemBox.hoverProperty());

        itemBox.getChildren().add(clearButtonPane);
		
		getChildren().add(objects.getValue().indexOf(object), itemBox);
    	fadeObject(itemBox, true);
		HBox.setMargin(itemBox, new Insets(0, 5, 0, 5));
		
		itemBox.hoverProperty().addListener((obs, oldValue, newValue) -> fadeObject(clearButtonPane, newValue));
	}
	
	private FadeTransition fadeObject(Node object, boolean toVisible) {
		FadeTransition fader = new FadeTransition(Duration.millis(350), object);
		fader.setFromValue(toVisible ? 0.0 : 1.0);
		fader.setToValue(toVisible ? 1.0 : 0.0);
		fader.play();
		return fader;
	}
	
	public interface Constructor<T> {
		public T newInstance(String s);
	}
	
	public interface StringPropertyGetter<T> {
		public Property<String> getProperty(T object);
	}

	public void clearTextField() {
		textField.clear();
	}
	
}
