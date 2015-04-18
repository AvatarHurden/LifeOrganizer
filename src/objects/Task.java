package objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.joda.time.DateTime;

import tools.DateUtils;

public class Task extends SimpleObjectProperty<Task> implements Comparable<Task>{

	public enum State {
		TODO, DONE, FAILED;
		
		public String toString() {
			switch (this) {
			case TODO:
				return "Todo";
			case DONE:
				return "Done";
			case FAILED:
				return "Failed";
			default: 
				throw new IllegalArgumentException();
			}
		}
	}
	
	private State state;
	private String name;
	
	private Character priority;
	
	private ObservableList<Project> projects;
	private ObservableList<Context> contexts;
	
	private String note;
	
	private DateTime dueDate;
	
	private DateTime creationDate;
	private DateTime editDate;
	private DateTime completionDate;
	
	// ObjectProperties
	private Property<State> stateProperty;
	private Property<String> nameProperty;
	private Property<Character> priorityProperty;
	private Property<DateTime> dueDateProperty;
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
	
	public Task(String input) {
		this();
		parseString(input);
	}
	
	private void parseString(String s) {
		if (!s.endsWith(" "))
			s = s + " "; //Adds whitespace to match pattern if at end
		
		// Parsing the priority
		Pattern pattern = Pattern.compile("^\\(([A-Za-z])\\) ");
		Matcher matcher = pattern.matcher(s);
		if (!matcher.find()) {
			pattern = Pattern.compile("pri=([A-Za-z]) ");
			matcher = pattern.matcher(s);
		}
		
		if (matcher.find(0)) {
			setPriority(matcher.group(1).charAt(0));
			s = s.replace(matcher.group(), "");
		} else {
			setPriority(null);
			s = s.replace("pri= ", "");
		}
		
		// Parsing the due date
		pattern = Pattern.compile("due=(\\S*) ");
		matcher = pattern.matcher(s);
		if (matcher.find()) {
			if (matcher.group(1).length() == 0)
				setDueDate(null);
			else {	
				HashMap<String, Integer> defaults = new HashMap<String, Integer>();
				if (!matcher.group(1).contains("@"))
					defaults.put("H", 0);
				defaults.put("m", 0);
				setDueDate(DateUtils.parseMultiFormat(matcher.group(1), defaults));
			}
			s = s.replace(matcher.group(), "");
		}
		
		// Parsing the note
		pattern = Pattern.compile("note=\"(.+)\" ");
		matcher = pattern.matcher(s);
		if (matcher.find()) {
			setNote(matcher.group(1));
			s = s.replace(matcher.group(), "");
		}
		
		// Parsing the projects and contexts
		List<String> words = Arrays.asList(s.split(" "));
		List<String> projects = words.stream().filter(word -> word.startsWith("+")).collect(Collectors.toList());
		List<String> contexts = words.stream().filter(word -> word.startsWith("@")).collect(Collectors.toList());
		
		for (String proj : projects)
			addProject(proj);
		for (String cont : contexts)
			addContext(cont);
		
		// Removes projects and contexts from the list of "normal" words
		words = words.stream().filter(word -> !projects.contains(word) && !contexts.contains(word)).collect(Collectors.toList());
		s = String.join(" ", words);
		
		
		setName(s.trim());
	}
	
	public static Task decode(String s) {
		Task t = new Task();

		// Defining state
		Pattern stateP = Pattern.compile("^\\[(x| |-)\\] ");
		Matcher stateM = stateP.matcher(s);
		stateM.find();
		
		switch (stateM.group().charAt(1)) {
		case 'x':
			t.state = Task.State.DONE;
			break;
		case ' ':
			t.state = Task.State.TODO;
			break;
		case '-':
			t.state = Task.State.FAILED;
		}
		
		// Defining completion date
		Pattern doneP = Pattern.compile("DONE=(\\S*) ");
		Matcher doneM = doneP.matcher(s);	
		if (doneM.find())
			t.completionDate = DateUtils.parseDateTime(doneM.group(1), "yyyy.MM.dd@HH:mm");

		// Defining priority
		Pattern priP = Pattern.compile("\\(([A-Z])\\) ");
		Matcher priM = priP.matcher(s);
		if (priM.find())
			t.priority = priM.group(1).charAt(0);
		
		// Defining due date
		Pattern dueP = Pattern.compile("DUE=(\\S*) ");
		Matcher dueM = dueP.matcher(s);	
		if (dueM.find())
			t.dueDate = DateUtils.parseDateTime(dueM.group(1), "yyyy.MM.dd@HH:mm");
		
		// Defining projects
		Pattern projP = Pattern.compile("PROJS=(\\S*) ");
		Matcher projM = projP.matcher(s);
		if (projM.find())
			for (String proj : projM.group(1).split(","))
				t.projects.add(new Project(proj));
		
		// Defining contexts
		Pattern contP = Pattern.compile("CONTEXTS=(\\S*) ");
		Matcher contM = contP.matcher(s);
		if (contM.find())
			for (String cont : contM.group(1).split(","))
				t.contexts.add(new Context(cont));
		
		// Defining creationDate
		Pattern madeP = Pattern.compile("MADE=(\\S*) ");
		Matcher madeM = madeP.matcher(s);	
		madeM.find();
		t.creationDate = DateUtils.parseDateTime(madeM.group(1), "yyyy.MM.dd@HH:mm");
		
		// Defining name
		// Accepts things between unescaped quotes (NAME="Read \"this book\" now")
		Pattern nameP = Pattern.compile("NAME=\"((?:\\\\.|[^\"\\\\])*)\"");
		Matcher nameM = nameP.matcher(s);
		nameM.find();
		t.name = nameM.group(1).replace("\\\"", "\"");
		
		// Defining note
		Pattern noteP = Pattern.compile("NOTE=\"((?:\\\\.|[^\"\\\\])*)\"");
		Matcher noteM = noteP.matcher(s);
		if (noteM.find())
			t.note = noteM.group(1).replace("\\\\n", "\n").replace("\\\"", "\"");
				
		
		// Defining edtDate
		Pattern editP = Pattern.compile("EDIT=(\\S*)[\n]?$");
		Matcher editM = editP.matcher(s);	
		editM.find();
		t.editDate = DateUtils.parseDateTime(editM.group(1), "yyyy.MM.dd@HH:mm");

		return t;
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
			parts.add("DUE=" + DateUtils.format(dueDate, "yyyy.MM.dd@HH:mm"));
		
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
	
	public Property<DateTime> DueDateProperty() {
		if (dueDateProperty == null)
			dueDateProperty = new TaskObjectProperty<DateTime>("dueDate").get();
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
		// This manual way must be done in order to allow for changes to be fired on the property
		setEditDateNow();
		if (this.projects == projects)
			return;
		for (Project p : this.projects)
			if (!projects.contains(p))
				this.projects.remove(p);
		for (Project p : projects)
			if (!this.projects.contains(p))
				this.projects.add(p);
	}
	
	private void addProject(String name) {
		projects.add(new Project(name));
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
	
	private void addContext(String name) {
		contexts.add(new Context(name));
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
	
	public void setDueDate(DateTime dueDate) {
		setEditDateIfDifferent(this.dueDate, dueDate);
		this.dueDate = dueDate;
	}
	
	public DateTime getDueDate() {
		return dueDate;
	}
	
}
