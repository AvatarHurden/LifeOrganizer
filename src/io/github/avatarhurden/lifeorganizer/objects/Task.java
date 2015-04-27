package io.github.avatarhurden.lifeorganizer.objects;

import io.github.avatarhurden.lifeorganizer.tools.DateUtils;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.joda.time.DateTime;

public class Task extends SimpleObjectProperty<Task> implements Comparable<Task>{

	public enum State {
		TODO, DONE, FAILED;
	}
	
	private State state;
	private String name;
	
	private Character priority;
	
	private ObservableList<Project> projects;
	private ObservableList<Context> contexts;
	
	private String note;
	
	private DueDate dueDate;
	
	private DateTime creationDate;
	private DateTime editDate;
	private DateTime completionDate;
	
	// ObjectProperties
	private Property<State> stateProperty;
	private Property<String> nameProperty;
	private Property<Character> priorityProperty;
	private Property<DueDate> dueDateProperty;
	private Property<DateTime> creationDateProperty;
	private Property<DateTime> completionDateProperty;
	private Property<String> noteProperty;
	private Property<ObservableList<Project>> projectsProperty;
	private Property<ObservableList<Context>> contextsProperty;
	private Property<DateTime> editDateProperty;
	
	public Task() {
		StateProperty().setValue(State.TODO);
		
		projects = FXCollections.observableArrayList(p -> new Observable[] {p.NameProperty()});
		contexts = FXCollections.observableArrayList(p -> new Observable[] {p.NameProperty()});
		
		CreationDateProperty().setValue(new DateTime());
		setEditDateNow();
	}
	
	public String encode() {
	 	ArrayList<String> parts = new ArrayList<String>();
	 	
	 	// Adding state
	 	switch (state) {
		case TODO:
			parts.add("[ ]");
			break;
		case DONE:
			parts.add("[x]");
			break;
		case FAILED:
			parts.add("[-]");	
		}
		
		if (completionDate != null)
			parts.add("DONE=" + DateUtils.format(completionDate, "yyyy.MM.dd@HH:mm"));
		
		// Adding priority
		if (priority != null)
			parts.add(String.format("(%c)", priority));
		
		if (dueDate != null)
			if (dueDate.getHasTime())
				parts.add("DUE=" + DateUtils.format(dueDate.getDateTime(), "yyyy.MM.dd@HH:mm"));
			else
				parts.add("DUE=" + DateUtils.format(dueDate.getDateTime(), "yyyy.MM.dd"));
				
		parts.add("MADE=" + DateUtils.format(creationDate, "yyyy.MM.dd@HH:mm"));
		
		parts.add("NAME=\"" + name.replace("\"", "\\\"") + "\"");
		
		if (!projects.isEmpty()) {
			List<String> projNames = new ArrayList<String>();
			for (Project proj : projects)
				projNames.add(proj.getName());
			parts.add("PROJS=" + String.join(",", projNames));
		}
			
		if (!contexts.isEmpty()) {
			List<String> contNames = new ArrayList<String>();
			for (Context proj : contexts)
				contNames.add(proj.getName());
			parts.add("CONTEXTS=" + String.join(",", contNames));
		}
		
		if (note != null && note != "")
			parts.add("NOTE=\"" + note.replace("\n", "\\\\n").replace("\"", "\\\"") + "\"");
			
		parts.add("EDIT=" + DateUtils.format(editDate, "yyyy.MM.dd@HH:mm"));
	
		return String.join(" ", parts);
	}
	
	// Property methods
	public Property<State> StateProperty() {
		if (stateProperty == null) 
			stateProperty = new TaskObjectProperty<State>("state").get();
		return stateProperty;
	}

	public Property<String> NameProperty() {
		if (nameProperty == null)
			nameProperty = new TaskObjectProperty<String>("name").get();
		return nameProperty;
	}
	
	public Property<Character> PriorityProperty() {
		if (priorityProperty == null)
			priorityProperty = new TaskObjectProperty<Character>("priority").get();
		return priorityProperty;
	}
	
	public Property<DueDate> DueDateProperty() {
		if (dueDateProperty == null)
			dueDateProperty = new TaskObjectProperty<DueDate>("dueDate").get();
		return dueDateProperty;
	}
	
	public Property<DateTime> CreationDateProperty() {
		if (creationDateProperty == null)
			creationDateProperty = new TaskObjectProperty<DateTime>("creationDate").get();
		return creationDateProperty;
	}
	
	public Property<DateTime> CompletionDateProperty() {
		if (completionDateProperty == null)
			completionDateProperty = new TaskObjectProperty<DateTime>("completionDate").get();
		return completionDateProperty;
	}
	
	public Property<DateTime> EditDateProperty() {
		if (editDateProperty == null) {
			// Listen to editdate, since it is changed every time another value is changed
			editDateProperty = new TaskObjectProperty<DateTime>("editDate").get();
			editDateProperty.addListener((obs, oldValue, newValue) -> fireValueChangedEvent());
		}
		return editDateProperty;
	}
	
	public Property<String> NoteProperty() {
		if (noteProperty == null)
			noteProperty = new TaskObjectProperty<String>("note").get();
		return noteProperty;
	}
	
	public Property<ObservableList<Project>> ProjectsProperty() {
		if (projectsProperty == null)
			projectsProperty = new TaskObjectProperty<ObservableList<Project>>("projects").get();
		return projectsProperty;
	}
	
	public Property<ObservableList<Context>> ContextsProperty() {
		if (contextsProperty == null) 
			contextsProperty = new TaskObjectProperty<ObservableList<Context>>("contexts").get();
		return contextsProperty;
	}
	
	private class TaskObjectProperty<T> {
		private JavaBeanObjectPropertyBuilder<T> propbuilder;
		
		private TaskObjectProperty(String name) {
			propbuilder = new JavaBeanObjectPropertyBuilder<T>();
			propbuilder.name(name).bean(Task.this);
		}
		
		private Property<T> get() {
			try {
				return propbuilder.build();
			} catch (NoSuchMethodException e) {
				return null;
			}
		}
	}

	@Override
	public int compareTo(Task o) {
		return encode().compareTo(o.encode());
	}
	
	// Setters and Getters

	public void setState(State state) {
		setEditDateIfDifferent(this.state, state);
		
		this.state = state;
		
		if (state != State.TODO)
			CompletionDateProperty().setValue(getEditDate());
		else
			CompletionDateProperty().setValue(null);
	}

	public State getState() {
		return state;
	}

	private void setEditDateIfDifferent(Object currentValue, Object newValue) {
		if (currentValue == null || !currentValue.equals(newValue))
			setEditDateNow();
	}

	public void setEditDate(DateTime editDate) {
		this.editDate = editDate;
	}

	public void setEditDateNow() {
		if (editDateProperty != null)
			editDateProperty.setValue(new DateTime());
		else
			setEditDate(new DateTime());
	}
	
	public DateTime getEditDate() {
		return editDate;
	}

	public void setName(String name) {
		setEditDateIfDifferent(this.name, name);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setPriority(Character priority) {
		setEditDateIfDifferent(this.priority, priority);
		if (priority == null)
			this.priority = null;
		else
			this.priority = Character.toUpperCase(priority);
	}
	
	public Character getPriority() {
		return priority;
	}
	
	public void setCompletionDate(DateTime completionDate) {
		this.completionDate = completionDate;
	}

	public DateTime getCompletionDate() {
		return completionDate;
	}

	public void setCreationDate(DateTime creationDate) {
		this.creationDate = creationDate;
	}

	public DateTime getCreationDate() {
		return creationDate;
	}

	public void setProjects(ObservableList<Project> projects) {
		for (Project p : this.projects)
			if (!projects.contains(p))
				this.projects.remove(p);
		for (Project p : projects)
			if (!this.projects.contains(p))
				this.projects.add(p);
		setEditDateNow();
	}
	
	public ObservableList<Project> getProjects() {
		return projects;
	}

	public void setContexts(ObservableList<Context> contexts) {
		// This manual way must be done in order to allow for changes to be fired on the property
		for (Context p : this.contexts)
			if (!contexts.contains(p))
				this.contexts.remove(p);
		for (Context p : contexts)
			if (!this.contexts.contains(p))
				this.contexts.add(p);
		setEditDateNow();
	}

	public ObservableList<Context> getContexts() {
		return contexts;
	}
	
	public void setNote(String note) {
		setEditDateIfDifferent(this.note, note);
		this.note = note;
	}
	
	public String getNote() {
		return note;
	}
	
	public void setDueDate(DueDate dueDate) {
		setEditDateIfDifferent(this.dueDate, dueDate);
		this.dueDate = dueDate;
	}
	
	public DueDate getDueDate() {
		return dueDate;
	}
	
}
