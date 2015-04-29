package io.github.avatarhurden.lifeorganizer.objects;

import io.github.avatarhurden.lifeorganizer.managers.TaskManager;
import io.github.avatarhurden.lifeorganizer.tools.DateUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Task extends SimpleObjectProperty<Task> implements Comparable<Task>{

	public enum State {
		TODO, DONE, FAILED;
	}
	
	private final String uuid;
	private boolean isArchived;
	
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
	
	private TaskManager manager;
	private File file;
	
	private boolean saveChanges = false;
	
	public static Task createNew(TaskManager manager) {
		JSONObject json = new JSONObject();
		
		String uuid = UUID.randomUUID().toString().replace("-", "");
		json.put("uuid", uuid);
		File file = new File(manager.getFolder().toFile(), uuid + ".txt");
		
		return new Task(manager, json, file);
	}
	
	public static Task loadFromPath(TaskManager manager, File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuffer buffer = new StringBuffer();
		int read;
		while ((read = reader.read()) != -1)
			buffer.append((char) read);
		reader.close();
		
		return new Task(manager, new JSONObject(buffer.toString()), file);
	}
	
	public Task(TaskManager manager, JSONObject json, File file) {
		this.manager = manager;
		this.file = file;
		uuid = json.getString("uuid");
		
		projects = FXCollections.observableArrayList(p -> new Observable[] {p.NameProperty()});
		contexts = FXCollections.observableArrayList(p -> new Observable[] {p.NameProperty()});

		state = State.TODO;
		creationDate = new DateTime();

		setEditDateNow();

		loadJSON(json);
		
		saveChanges = true;
	}
	
	private void loadJSON(JSONObject json) {
	
		if (json.has("archived"))
			isArchived = json.getBoolean("archived");
		
		if (json.has("name"))
			name = json.getString("name");
		
		if (json.has("state"))
			switch (json.getString("state")) {
			case "todo": state = State.TODO; break;
			case "done": state = State.DONE; break;
			case "failed": state = State.FAILED; break;
			}
		
		if (json.has("creationDate"))
			creationDate = DateUtils.parseDateTime(
				json.getString("creationDate"), "yyyy.MM.dd@HH:mm");
		
		if (json.has("editDate"))
			editDate = DateUtils.parseDateTime(
				json.getString("editDate"), "yyyy.MM.dd@HH:mm");
		
		if (json.has("note"))
			note = json.getString("note");
		
		if (json.has("completionDate"))
			completionDate = DateUtils.parseDateTime(
					json.getString("completionDate"), "yyyy.MM.dd@HH:mm");
	
		if (json.has("priority"))
			priority = json.getString("priority").charAt(0);
		
		if (json.has("dueDate"))
			dueDate = new DueDate(DateUtils.parseDateTime(
					json.getString("dueDate"), "yyyy.MM.dd@HH:mm", "yyyy.MM.dd"), 
					json.getString("dueDate").contains("@"));
		
		if (json.has("projects"))
			for (int i = 0; i < json.getJSONArray("projects").length(); i++)
				addProject(json.getJSONArray("projects").getString(i));
		
		if (json.has("contexts"))
			for (int i = 0; i < json.getJSONArray("contexts").length(); i++)
				addContext(json.getJSONArray("contexts").getString(i));
	}
	
	private void save() {
		if (!saveChanges)
			return;
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(toJSON().toString(4));
			writer.close();
			manager.setTaskModified(uuid);
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		json.put("uuid", uuid);
		json.put("archived", isArchived);
		json.put("name", name);
		json.put("creationDate", DateUtils.format(creationDate, "yyyy.MM.dd@HH:mm"));
		json.put("editDate", DateUtils.format(editDate, "yyyy.MM.dd@HH:mm"));
		
		switch (state) {
		case TODO: json.put("state", "todo"); break;
		case DONE: json.put("state", "done"); break;
		case FAILED: json.put("state", "failed"); break;
		}
		
		if (priority != null)
			json.put("priority", priority);
		
		if (dueDate != null)
			if (dueDate.getHasTime())
				json.put("dueDate", DateUtils.format(dueDate.getDateTime(), "yyyy.MM.dd@HH:mm"));
			else
				json.put("dueDate", DateUtils.format(dueDate.getDateTime(), "yyyy.MM.dd"));
			
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
		
		if (note != null)
			json.put("note", note);
		
		return json;
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
			editDateProperty.addListener((obs, oldValue, newValue) -> { 
				fireValueChangedEvent();
				save();
			});
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

	// Setters and Getters

	public String getUUID() {
		return uuid;
	}
	
	public void setArchived(boolean isArchived) {
		this.isArchived = isArchived;
		setEditDateNow();
	}
	
	public boolean isArchived() {
		return isArchived;
	}
	
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
		EditDateProperty().setValue(new DateTime());
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

	public void addProject(String name) {
		projects.add(manager.getProjectManager().createProject(name, !isArchived));
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

	public void addContext(String name) {
		contexts.add(manager.getContextManager().createContext(name, !isArchived));
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

	@Override
	public int compareTo(Task o) {
		return 0;
	}
}
