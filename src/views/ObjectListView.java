package views;

import javafx.beans.property.Property;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ObjectListView<T> extends HBox {

	private Constructor<T> instantiator;
	private StringPropertyGetter<T> property;
	private Property<ObservableList<T>> objects;
	
	private TextField textField;
	
	public ObjectListView(Constructor<T> instantiator, StringPropertyGetter<T> property) {
		this.instantiator = instantiator;
		this.property = property;
		getStylesheets().add("views/style.css");
	
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
		textField = new TextField();
	
		HBox.setMargin(textField, new Insets(0, 5, 0, 5));
		
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
		Label label = new Label();
		label.setFocusTraversable(true);
		label.setOnMousePressed((event) -> {
			event.consume();
			label.requestFocus();
		});
		label.getStyleClass().add("objectlistLabel");
		label.textProperty().bindBidirectional(property.getProperty(object));
		
		MenuItem item = new MenuItem("Delete");
		item.setOnAction((value) -> {
			getChildren().remove(label);
			objects.getValue().remove(object);
		});
		label.setContextMenu(new ContextMenu(item));

		getChildren().add(objects.getValue().indexOf(object), label);
		
		label.setPrefHeight(25);
		
		label.setPadding(new Insets(0, 5, 0, 5));
		HBox.setMargin(label, new Insets(0, 5, 0, 5));
	}
	
	public interface Constructor<T> {
		public T newInstance(String s);
	}
	
	public interface StringPropertyGetter<T> {
		public Property<String> getProperty(T object);
	}
	
}
