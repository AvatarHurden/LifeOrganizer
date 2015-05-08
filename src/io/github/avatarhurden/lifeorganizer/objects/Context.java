package io.github.avatarhurden.lifeorganizer.objects;


import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Context {

	private Property<String> name;
	private Property<Status> status;

	private Property<String> note;
	
	private Property<String> parent;
	private ObservableList<String> children;
	
	private ObservableList<String> tasks;
	
	public Context() {
		name = new SimpleStringProperty();

		status = new SimpleObjectProperty<Status>();
		note = new SimpleStringProperty();
		
		parent = new SimpleStringProperty();
		children = FXCollections.observableArrayList();
		tasks = FXCollections.observableArrayList();
		
	}
	
	public Context(String name) {
		this();
		setName(name);
	}

	public Property<String> nameProperty() {
		return name;
	}

	public String getName() {
		return name.getValue();
	}

	public void setName(String name) {
		this.name.setValue(name);
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
	
	public Property<String> noteProperty() {
		return note;
	}

	public String getNote() {
		return note.getValue();
	}

	public void setNote(String note) {
		this.note.setValue(note);
	}
	
	public Property<String> parentProperty() {
		return parent;
	}

	public String getParent() {
		return parent.getValue();
	}

	public void setParent(String parent) {
		this.parent.setValue(parent);
	}

	public ObservableList<String> getChildren() {
		return children;
	}
	
	public void addChild(String uuid) {
		children.add(uuid);
	}
	
	public void removeChild(String uuid) {
		children.remove(uuid);
	}
	
	public ObservableList<String> getTasks() {
		return children;
	}
	
	public void addTask(String uuid) {
		tasks.add(uuid);
	}
	
	public void removeTask(String uuid) {
		tasks.remove(uuid);
	}
}
