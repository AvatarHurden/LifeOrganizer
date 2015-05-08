package io.github.avatarhurden.lifeorganizer.objects;

import io.github.avatarhurden.lifeorganizer.tools.DateUtils;

import java.util.UUID;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.joda.time.DateTime;
import org.json.JSONObject;

public class Task {

	private final String uuid;
	
	private Property<String> name;
	private Property<String> note;
	private Property<Status> status;
	
	// List of the UUIDs of the parent and children. Not references to avoid duplication or not reloading,
	// since the most recent version of a task is available on the TaskManager
	private ObservableList<String> parents;
	private ObservableList<String> children;
	private Property<Boolean> isProject;
	
	private Property<DueDate> dueDate;
	
	private Property<DateTime> creationDate;
	private Property<DateTime> completionDate;
	private Property<DateTime> editDate;
	
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

		name = new SimpleStringProperty();
		status = new SimpleObjectProperty<Status>(Status.ACTIVE);
		note = new SimpleStringProperty();
		
		parents = FXCollections.observableArrayList();
		children = FXCollections.observableArrayList();
		isProject = new SimpleBooleanProperty();
		
		dueDate = new SimpleObjectProperty<DueDate>();
		
		creationDate = new SimpleObjectProperty<DateTime>(new DateTime());
		completionDate = new SimpleObjectProperty<DateTime>();
		editDate = new SimpleObjectProperty<DateTime>(new DateTime());

		contexts = FXCollections.observableArrayList();
		
		loadJSON(json);
	}
	
	public void loadJSON(JSONObject json) {
		
		if (json.has("name"))
			name.setValue(json.getString("name"));
		
		if (json.has("status"))
			status.setValue(Status.valueOf(json.getString("status")));

		if (json.has("note"))
			note.setValue(json.getString("note"));
		
		parents.clear();
		if (json.has("parents"))
			for (int i = 0; i < json.getJSONArray("parents").length(); i++)
				parents.add(json.getJSONArray("parents").getString(i));

		children.clear();
		if (json.has("children"))
			for (int i = 0; i < json.getJSONArray("children").length(); i++)
				children.add(json.getJSONArray("children").getString(i));

		if (json.has("isProject"))
			isProject.setValue(json.getBoolean("isProject"));
		
		if (json.has("dueDate"))
			dueDate.setValue(new DueDate(DateUtils.parseDateTime(
					json.getString("dueDate"), "yyyy.MM.dd@HH:mm", "yyyy.MM.dd"), 
					json.getString("dueDate").contains("@")));
		
		if (json.has("creationDate"))
			creationDate.setValue(DateUtils.parseDateTime(
				json.getString("creationDate"), "yyyy.MM.dd@HH:mm"));
		
		if (json.has("completionDate"))
			completionDate.setValue(DateUtils.parseDateTime(
					json.getString("completionDate"), "yyyy.MM.dd@HH:mm"));

		if (json.has("editDate"))
			editDate.setValue(DateUtils.parseDateTime(
				json.getString("editDate"), "yyyy.MM.dd@HH:mm"));

		contexts.clear();
		if (json.has("contexts"))
			for (int i = 0; i < json.getJSONArray("contexts").length(); i++)
				contexts.add(json.getJSONArray("contexts").getString(i));
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		json.put("uuid", uuid);
		
		json.put("name", name.getValue());
		json.put("status", status.getValue());
		json.put("note", note.getValue());
		
		json.put("creationDate", DateUtils.format(creationDate.getValue(), "yyyy.MM.dd@HH:mm"));
		json.put("editDate", DateUtils.format(editDate.getValue(), "yyyy.MM.dd@HH:mm"));
		if (completionDate.getValue() != null)
			json.put("completionDate", DateUtils.format(completionDate.getValue(), "yyyy.MM.dd@HH:mm"));
		
		json.put("parents", parents);
		json.put("children", children);
		json.put("isProject", isProject.getValue());
		
		if (dueDate.getValue() != null)
			if (dueDate.getValue().getHasTime())
				json.put("dueDate", DateUtils.format(dueDate.getValue().getDateTime(), "yyyy.MM.dd@HH:mm"));
			else
				json.put("dueDate", DateUtils.format(dueDate.getValue().getDateTime(), "yyyy.MM.dd"));
			
		json.put("contexts", contexts);
		
		return json;
	}
	
	// Setters and Getters
	
	public String getUUID() {
		return uuid;
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
	
	public ObservableList<String> getChildren() {
		return children;
	}
	
	public void addChild(String uuid) {
		if (!children.contains(uuid))
			children.add(uuid);
	}

	public void removeChild(String uuid) {
		children.remove(uuid);
	}
	
	public boolean hasParents() {
		return parents.size() > 0;
	}
	
	public ObservableList<String> getParents() {
		return parents;
	}

	public void addParent(String uuid) {
		if (!parents.contains(uuid))
			parents.add(uuid);
	}

	public void removeParent(String uuid) {
		parents.remove(uuid);
	}

	public Property<Boolean> isProjectProperty() {
		return isProject;
	}

	public Boolean getIsProject() {
		return isProject.getValue();
	}

	public void setIsProject(Boolean isProject) {
		this.isProject.setValue(isProject);
	}
	
	public Property<DueDate> dueDateProperty() {
		return dueDate;
	}

	public DueDate getDueDate() {
		return dueDate.getValue();
	}

	public void setDueDate(DueDate dueDate) {
		this.dueDate.setValue(dueDate);
	}
	
	public Property<DateTime> creationDateProperty() {
		return creationDate;
	}

	public DateTime getCreationDate() {
		return creationDate.getValue();
	}

	public Property<DateTime> editDateProperty() {
		return editDate;
	}

	public DateTime getEditDate() {
		return editDate.getValue();
	}

	public void setEditDate(DateTime editDate) {
		this.editDate.setValue(editDate);
	}
	
	public ObservableList<String> getContexts() {
		return contexts;
	}
	

}
