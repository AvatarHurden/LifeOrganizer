package io.github.avatarhurden.lifeorganizer.objects;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;

public class Context {

	private Property<String> name;
	
	public Context() {
		name = new SimpleStringProperty();
	}
	
	public Context(String name) {
		this();
		setName(name);
	}

	public void setName(String name) {
		if (!name.startsWith("@"))
			name = "@" + name;
		this.name.setValue(name);;
	}
	
	public String getName() {
		return name.getValue();
	}
	
	public Property<String> NameProperty() {
		return name;
	}
	
	public String toString() {
		return name.getValue();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof Context))
			return false;
		else
			return getName().equals(((Context) obj).getName());
	}
	
	public int hashCode() {
		return getName().hashCode();
	}
}
