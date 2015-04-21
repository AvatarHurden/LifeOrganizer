package io.github.avatarhurden.lifeorganizer.objects;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;

public class Project {

	private Property<String> name;
	
	public Project() {
		name = new SimpleStringProperty();
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
	
}
