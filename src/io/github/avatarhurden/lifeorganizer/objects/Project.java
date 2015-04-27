package io.github.avatarhurden.lifeorganizer.objects;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Project {

	private Property<String> name;
	private Property<Boolean> isActive;
	private SimpleIntegerProperty activeTasks, inactiveTasks;
	
	public Project() {
		name = new SimpleStringProperty();
		activeTasks = new SimpleIntegerProperty(0);
		inactiveTasks = new SimpleIntegerProperty(0);

		isActive = new SimpleBooleanProperty();
		isActive.bind(activeTasks.greaterThan(0));
	}
	
	public Project(String name) {
		this();
		setName(name);
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
	
	public boolean isActive() {
		return isActive.getValue();
	}
	
	public Property<Boolean> activeProperty() {
		return isActive;
	}
	
	public int getActiveTasks() {
		return activeTasks.intValue();
	}
	
	public int getInactiveTasks() {
		return inactiveTasks.intValue();
	}
	
	public void incrementCount(boolean active) {
		if (active)
			activeTasks.set(getActiveTasks() + 1);
		else
			inactiveTasks.set(getInactiveTasks() + 1);
	}
	
	public void decrementCount(boolean active) {
		if (active)
			activeTasks.set(getActiveTasks() - 1);
		else
			inactiveTasks.set(getInactiveTasks() - 1);
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
