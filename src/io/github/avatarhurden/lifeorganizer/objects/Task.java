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

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Task {

	public static final long SAVE_INTERVAL = 5000;
	
	public enum State {
		TODO, DONE, FAILED;
	}
	
	private final String uuid;
	private boolean isArchived;
	
	private Property<State> stateProperty;
	private Property<String> nameProperty;
	private Property<Character> priorityProperty;
	
	private Property<DueDate> dueDateProperty;
	
	private Property<DateTime> creationDateProperty;
	private Property<DateTime> completionDateProperty;
	private Property<DateTime> editDateProperty;
	
	private Property<String> noteProperty;

	private ObservableList<Project> projects;
	private ObservableList<Context> contexts;
	
	private Property<ObservableList<Project>> projectsProperty;
	private Property<ObservableList<Context>> contextsProperty;
	
	private TaskManager manager;
	private File file;
	
	private Thread scheduledSave = null;
	
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
		try {
			new JSONObject(buffer.toString());
		} catch (JSONException e) {
			System.out.println(buffer.toString());
		}
		return new Task(manager, new JSONObject(buffer.toString()), file);
	}
	
	public Task(TaskManager manager, JSONObject json, File file) {
		this.manager = manager;
		this.file = file;
		uuid = json.getString("uuid");
		
		projects = FXCollections.observableArrayList(p -> new Observable[] {p.NameProperty()});
		
		projectsProperty = new SimpleObjectProperty<ObservableList<Project>>(projects);
		projects.addListener((ListChangeListener.Change<? extends Project> event) -> {
			projectsProperty.setValue(null);
			projectsProperty.setValue(projects);
		});
		
		contexts = FXCollections.observableArrayList(p -> new Observable[] {p.NameProperty()});
		
		contextsProperty = new SimpleObjectProperty<ObservableList<Context>>(contexts);
		contexts.addListener((ListChangeListener.Change<? extends Context> event) -> {
			contextsProperty.setValue(null);
			contextsProperty.setValue(contexts);
		});
		
		nameProperty = new SimpleStringProperty();
		stateProperty = new SimpleObjectProperty<Task.State>(State.TODO);
		priorityProperty = new SimpleObjectProperty<Character>();
		
		dueDateProperty = new SimpleObjectProperty<DueDate>();
		
		creationDateProperty = new SimpleObjectProperty<DateTime>(new DateTime());
		editDateProperty = new SimpleObjectProperty<DateTime>(new DateTime());
		completionDateProperty = new SimpleObjectProperty<DateTime>();
		
		noteProperty = new SimpleStringProperty();
		
		editDateProperty.addListener((obs, oldValue, newValue) -> {
			if (scheduledSave == null && saveChanges) {
				scheduledSave = new Thread(() -> {
					try {
						Thread.sleep(SAVE_INTERVAL);
						save();
						scheduledSave = null;
					} catch (Exception e) {}
				});
				scheduledSave.start();
			}
		});

		setEditDateNow();

		loadJSON(json);
	}
	
	public void setFile(File f) {
		this.file = f;
	}
	
	public File getFile() {
		return file;
	}
	
	public void readFile() throws IOException {
		if (!file.canRead())
			return;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null)
			buffer.append(line);
		reader.close();

		if (!buffer.toString().trim().equals(""))
			loadJSON(new JSONObject(buffer.toString()));
	}
	
	private void loadJSON(JSONObject json) {
		saveChanges = false;
		
		if (json.has("archived"))
			setArchived(json.getBoolean("archived"));
		
		if (json.has("name"))
			nameProperty.setValue(json.getString("name"));
		
		if (json.has("state"))
			switch (json.getString("state")) {
			case "todo": stateProperty.setValue(State.TODO); break;
			case "done": stateProperty.setValue(State.DONE); break;
			case "failed": stateProperty.setValue(State.FAILED); break;
			}
		
		if (json.has("creationDate"))
			creationDateProperty.setValue(DateUtils.parseDateTime(
				json.getString("creationDate"), "yyyy.MM.dd@HH:mm"));
		
		if (json.has("note"))
			noteProperty.setValue(json.getString("note"));
		
		if (json.has("completionDate"))
			completionDateProperty.setValue(DateUtils.parseDateTime(
					json.getString("completionDate"), "yyyy.MM.dd@HH:mm"));
	
		if (json.has("priority"))
			priorityProperty.setValue(json.getString("priority").charAt(0));
		
		if (json.has("dueDate"))
			dueDateProperty.setValue(new DueDate(DateUtils.parseDateTime(
					json.getString("dueDate"), "yyyy.MM.dd@HH:mm", "yyyy.MM.dd"), 
					json.getString("dueDate").contains("@")));
		
		projects.clear();
		if (json.has("projects"))
			for (int i = 0; i < json.getJSONArray("projects").length(); i++)
				addProject(json.getJSONArray("projects").getString(i));
		
		contexts.clear();
		if (json.has("contexts"))
			for (int i = 0; i < json.getJSONArray("contexts").length(); i++)
				addContext(json.getJSONArray("contexts").getString(i));
		
		if (json.has("editDate"))
			editDateProperty.setValue(DateUtils.parseDateTime(
				json.getString("editDate"), "yyyy.MM.dd@HH:mm"));
		
		saveChanges = true;
	}
	
	public void save() {
		if (!saveChanges)
			return;

		ignoreForEdit(() -> {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write(toJSON().toString(4));
				writer.close();
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
		});	
	}
	
	public void delete() {
		ignoreForEdit(() -> {
			file.delete();
			manager.getProjectManager().decrementProjects(!isArchived, projects);
			manager.getContextManager().decrementContexts(!isArchived, contexts);
		});
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
		
		switch (stateProperty.getValue()) {
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
		stateProperty.setValue(state);
		setEditDateNow();
	}

	public State getState() {
		return stateProperty.getValue();
	}

	public Property<State> stateProperty() {
		return stateProperty;
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
