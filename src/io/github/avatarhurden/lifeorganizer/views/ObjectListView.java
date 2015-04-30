package io.github.avatarhurden.lifeorganizer.views;

import java.util.function.Consumer;

import javafx.animation.FadeTransition;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.Duration;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

public class ObjectListView<T> extends HBox {

	private Callback<String, T> creationPolicy;
	private Consumer<T> deletionPolicy;
	private StringPropertyGetter<T> property;
	
	private AutoCompletionBinding<T> autoCompletion;
	
	private TextField textField;
	
	public ObjectListView(StringPropertyGetter<T> property) {
		this.property = property;
		getStylesheets().add("/io/github/avatarhurden/lifeorganizer/views/style.css");
	
		initialize();
	}
	
	public void setPromptText(String text) {
		textField.setPromptText(text);
	}
	
	public void setList(ObservableList<T> objects) {
		// Clears the list of labels
		if (getChildren().size() > 1)
			getChildren().remove(0, getChildren().size() - 1);
		
		// Adds listener so that the property fires a changeEvent when the list fires an event
		for (T object : objects)
			addLabel(object);
	}

	public void setSuggestions(ObservableList<T> suggestions) {
		if (autoCompletion != null)
			autoCompletion.dispose();
		autoCompletion = TextFields.bindAutoCompletion(textField, suggestions);
	}
	
	/**
	 * Set the action to perform when trying to create a new object. This method should
	 * verify if the value is valid and, if it is invalid and should not be accepted, 
	 * the policy should return null.
	 * 
	 * @param policy
	 */
	public void setCreationPolicy(Callback<String, T> policy) {
		creationPolicy = policy;
	}

	/**
	 * Set the action to perform when deleting an object.
	 * 
	 * @param policy
	 */
	public void setDeletionPolicy(Consumer<T> policy) {
		deletionPolicy = policy;
	}
	
	private void initialize() {
		textField = TextFields.createClearableTextField();
	
		HBox.setMargin(textField, new Insets(0, 5, 0, 5));
		setAlignment(Pos.CENTER_LEFT);
		
		textField.setOnAction((event) -> {
				T object = creationPolicy.call(textField.getText());
				if (object != null) {
					addLabel(object);
					textField.clear();
				}
		});
		getChildren().add(textField);
	}
	
	private void addLabel(T object) {
		HBox itemBox = new HBox(0);
		itemBox.getStyleClass().add("object-box");
		
		itemBox.setFocusTraversable(true);
		itemBox.setOnMousePressed((event) -> {
			event.consume();
			itemBox.requestFocus();
		});
		
		Label label = new Label();
		label.textProperty().bindBidirectional(property.getProperty(object));
		label.setPrefHeight(25);
		
		HBox.setMargin(label, new Insets(0, 5, 0, 0));
        itemBox.getChildren().add(label);
		
		Region clearButton = new Region();
        clearButton.getStyleClass().addAll("graphic");
        
        StackPane clearButtonPane = new StackPane(clearButton);
        clearButtonPane.getStyleClass().addAll("clear-button");
        clearButtonPane.visibleProperty().bind(itemBox.hoverProperty());

        itemBox.getChildren().add(clearButtonPane);
		itemBox.hoverProperty().addListener((obs, oldValue, newValue) -> fadeObject(clearButtonPane, newValue));
        
		Runnable delete = () -> {
			fadeObject(itemBox, false).setOnFinished(finished -> getChildren().remove(itemBox));
			textField.requestFocus();
			deletionPolicy.accept(object);
		};
		
		itemBox.setOnKeyPressed(event -> { if (event.getCode().equals(KeyCode.DELETE)) delete.run(); });
		clearButtonPane.setOnMouseReleased(event -> delete.run());
		
		getChildren().add(getChildren().size() - 1, itemBox);
    	fadeObject(itemBox, true);
		HBox.setMargin(itemBox, new Insets(0, 5, 0, 5));
	}
	
	private FadeTransition fadeObject(Node object, boolean toVisible) {
		FadeTransition fader = new FadeTransition(Duration.millis(350), object);
		fader.setFromValue(toVisible ? 0.0 : 1.0);
		fader.setToValue(toVisible ? 1.0 : 0.0);
		fader.play();
		return fader;
	}
	
	public interface StringPropertyGetter<T> {
		public Property<String> getProperty(T object);
	}

	public void clearTextField() {
		textField.clear();
	}
}
