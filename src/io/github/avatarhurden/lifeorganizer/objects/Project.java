package io.github.avatarhurden.lifeorganizer.objects;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class Project {

	private Property<String> name;
	private Property<Boolean> active;
	
	public Project() {
		name = new SimpleStringProperty();
		active = new SimpleBooleanProperty();
	}
	
	public Project(String name) {
		this();
		setName(name);
	}

	public Project(String name, boolean active) {
		this();
		setName(name);
		setActive(active);
	}
	
	public void setName(String name) {
		if (!name.startsWith("+"))
			name = "+" + name;
		this.name.setValue(name);;
	}
	
	public String getName() {
		return name.getValue();
	}
	
	public Property<String> NameProperty() {
		return name;
	}
	
	public void setActive(boolean active) {
		this.active.setValue(active);
	}
	
	public boolean isActive() {
		return active.getValue();
	}
	
	public Property<Boolean> activeProperty() {
		return active;
	}
	
	public String toString() {
		return name.getValue();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof Project))
			return false;
		else
			return getName().equals(((Project) obj).getName());
	}
	
	public int hashCode() {
		return getName().hashCode();
	}
	
}
