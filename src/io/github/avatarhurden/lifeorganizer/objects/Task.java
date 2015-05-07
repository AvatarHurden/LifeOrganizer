package io.github.avatarhurden.lifeorganizer.objects;

import io.github.avatarhurden.lifeorganizer.managers.TaskManager;
import io.github.avatarhurden.lifeorganizer.tools.DateUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Task {

	private final String uuid;
	
	private Property<String> nameProperty;
	private Property<String> noteProperty;
	private Property<Status> statusProperty;
	
	// List of the UUIDs of the parent and children. Not references to avoid duplication or not reloading,
	// since the most recent version of a task is available on the TaskManager
	private ObservableList<String> parents;
	private ObservableList<String> children;
	
	private Property<DueDate> dueDateProperty;
	
	private Property<DateTime> creationDateProperty;
	private Property<DateTime> completionDateProperty;
	private Property<DateTime> editDateProperty;
	
	private ObservableList<String> contexts;
	
	public static Task createNew() {
		JSONObject json = new JSONObject();
		
		String uuid = UUID.randomUUID().toString().replace("-", "");
		json.put("uuid", uuid);
		
		return new Task(json);
	}
	
	public static Task loadFromJSON(JSONObject json) {
		return new Task(json);
	}
	
	public Task(JSONObject json) {
		uuid = json.getString("uuid");
		
		contexts = FXCollections.observableArrayList();
		parents = FXCollections.observableArrayList();
		children = FXCollections.observableArrayList();
		
		nameProperty = new SimpleStringProperty();
		statusProperty = new SimpleObjectProperty<Status>(Status.ACTIVE);
		noteProperty = new SimpleStringProperty();
		
		dueDateProperty = new SimpleObjectProperty<DueDate>();
		
		creationDateProperty = new SimpleObjectProperty<DateTime>(new DateTime());
		completionDateProperty = new SimpleObjectProperty<DateTime>();
		editDateProperty = new SimpleObjectProperty<DateTime>(new DateTime());
		
		loadJSON(json);
	}
	
	private void loadJSON(JSONObject json) {
		if (json.has("name"))
			nameProperty.setValue(json.getString("name"));
		
		if (json.has("state"))
			statusProperty.setValue(Status.valueOf(json.getString("state")));
		
		if (json.has("creationDate"))
			creationDateProperty.setValue(DateUtils.parseDateTime(
				json.getString("creationDate"), "yyyy.MM.dd@HH:mm"));
		
		if (json.has("note"))
			noteProperty.setValue(json.getString("note"));
		
		if (json.has("completionDate"))
			completionDateProperty.setValue(DateUtils.parseDateTime(
					json.getString("completionDate"), "yyyy.MM.dd@HH:mm"));
	
		if (json.has("dueDate"))
			dueDateProperty.setValue(new DueDate(DateUtils.parseDateTime(
					json.getString("dueDate"), "yyyy.MM.dd@HH:mm", "yyyy.MM.dd"), 
					json.getString("dueDate").contains("@")));
		
		contexts.clear();
		if (json.has("contexts"))
			for (int i = 0; i < json.getJSONArray("contexts").length(); i++)
				contexts.add(TaskManager.getContext(json.getJSONArray("contexts").getString(i)));
		
		if (json.has("editDate"))
			editDateProperty.setValue(DateUtils.parseDateTime(
				json.getString("editDate"), "yyyy.MM.dd@HH:mm"));
		
	}
	
	private void ignoreForEdit(Runnable action) {
		manager.ignoreTask(uuid);
		
		action.run();
		
		new Thread(() -> {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			manager.removeIgnore(uuid);
		}).run();
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		json.put("uuid", uuid);
		json.put("archived", isArchived);
		json.put("name", nameProperty.getValue());
		json.put("creationDate", DateUtils.format(creationDateProperty.getValue(), "yyyy.MM.dd@HH:mm"));
		json.put("editDate", DateUtils.format(editDateProperty.getValue(), "yyyy.MM.dd@HH:mm"));
		
		switch (statusProperty.getValue()) {
		case TODO: json.put("state", "todo"); break;
		case DONE: json.put("state", "done"); break;
		case FAILED: json.put("state", "failed"); break;
		}
		
		if (priorityProperty.getValue() != null)
			json.put("priority", priorityProperty.getValue());
		
		if (dueDateProperty.getValue() != null)
			if (dueDateProperty.getValue().getHasTime())
				json.put("dueDate", DateUtils.format(dueDateProperty.getValue().getDateTime(), "yyyy.MM.dd@HH:mm"));
			else
				json.put("dueDate", DateUtils.format(dueDateProperty.getValue().getDateTime(), "yyyy.MM.dd"));
			
		if (projects != null) {
			JSONArray ar = new JSONArray();
			for (Project p : projects)
				ar.put(p.getName());
			json.put("projects", ar);
		}

		if (contexts != null) {
			JSONArray ar = new JSONArray();
			for (Context c : contexts)
				ar.put(c.getName());
			json.put("contexts", ar);
		}
		
		if (noteProperty.getValue() != null)
			json.put("note", noteProperty.getValue());
		
		return json;
	}
	
	// Setters and Getters
	public ObservableList<String> getChildren() {
		return children;
	}
	
	public boolean hasParents() {
		return parents.size() > 0;
	}
	
	public ObservableList<String> getParents() {
		return parents;
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public void setArchived(boolean isArchived) {
		boolean old = this.isArchived;
		this.isArchived = isArchived;
		
		if (this.isArchived != old) {
			manager.getProjectManager().moveProjects(!this.isArchived, projects);
			manager.getContextManager().moveContexts(!this.isArchived, contexts);
		}
		setEditDateNow();
	}
	
	public boolean isArchived() {
		return isArchived;
	}
	
	public void setState(State state) {
		statusProperty.setValue(state);
		setEditDateNow();
	}

	public Status getState() {
		return statusProperty.getValue();
	}

	public Property<Status> stateProperty() {
		return statusProperty;
	}
	
	public void setName(String name) {
		nameProperty.setValue(name);
		setEditDateNow();
	}

	public String getName() {
		return nameProperty.getValue();
	}

	public Property<String> nameProperty() {
		return nameProperty;
	}
	
	public void setPriority(Character priority) {
		priorityProperty.setValue(priority);
		setEditDateNow();
	}
	
	public Character getPriority() {
		return priorityProperty.getValue();
	}

	public Property<Character> priorityProperty() {
		return priorityProperty;
	}
	
	public void setCompletionDate(DateTime completionDate) {
		completionDateProperty.setValue(completionDate);
		setEditDateNow();
	}
	
	public DateTime getCompletionDate() {
		return completionDateProperty.getValue();
	}
	
	public Property<DateTime> completionDateProperty() {
		return completionDateProperty;
	}

	public void setCreationDate(DateTime creationDate) {
		creationDateProperty.setValue(creationDate);
		setEditDateNow();
	}
	
	public DateTime getCreationDate() {
		return creationDateProperty.getValue();
	}

	public Property<DateTime> creationDateProperty() {
		return completionDateProperty;
	}
	
	public Project addProject(String name) {
		Project p = manager.getProjectManager().getProject(name);
		
		if (projects.contains(p))
			return null;
		
		p = manager.getProjectManager().createProject(name, !isArchived);
		
		projects.add(p);
		setEditDateNow();
		return p;
	}

	public void removeProject(Project p) {
		manager.getProjectManager().decrementProjects(!isArchived, p);
		projects.remove(p);
		setEditDateNow();
	}
	
	public ObservableList<Project> getProjects() {
		return projects;
	}

	public Property<ObservableList<Project>> projectsProperty() {
		return projectsProperty;
	}

	public Context addContext(String name) {
		Context c = manager.getContextManager().getContext(name);
	
		if (contexts.contains(c))
			return null;
	
		c = manager.getContextManager().createContext(name, !isArchived);
		
		contexts.add(c);
		setEditDateNow();
		return c;
	}

	public void removeContext(Context c) {
		manager.getContextManager().decrementContexts(!isArchived, c);
		contexts.remove(c);
		setEditDateNow();
	}
	
	public ObservableList<Context> getContexts() {
		return contexts;
	}
	
	public Property<ObservableList<Context>> contextsProperty() {
		return contextsProperty;
	}
	
	public void setNote(String note) {
		noteProperty.setValue(note);
		setEditDateNow();
	}
	
	public String getNote() {
		return noteProperty.getValue();
	}

	public Property<String> noteProperty() {
		return noteProperty;
	}
	
	public void setDueDate(DueDate dueDate) {
		dueDateProperty.setValue(dueDate);
		setEditDateNow();
	}
	
	public DueDate getDueDate() {
		return dueDateProperty.getValue();
	}

	public Property<DueDate> dueDateProperty() {
		return dueDateProperty;
	}

	public void setEditDate(DateTime editDate) {
		editDateProperty.setValue(editDate);
	}
	
	private void setEditDateNow() {
		editDateProperty.setValue(new DateTime());
	}
	
	public DateTime getEditDate() {
		return editDateProperty.getValue();
	}
	
	public Property<DateTime> editDateProperty() {
		return editDateProperty;
	}
}
