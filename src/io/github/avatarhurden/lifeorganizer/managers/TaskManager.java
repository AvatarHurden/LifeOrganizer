package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.Context;
import io.github.avatarhurden.lifeorganizer.objects.DueDate;
import io.github.avatarhurden.lifeorganizer.objects.Project;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.tools.DateUtils;
import io.github.avatarhurden.lifeorganizer.tools.StoredList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TaskManager {

	StoredList<Task> todo, done;
	
	ProjectManager projectManager;
	ContextManager contextManager;
	
	public TaskManager() {
		
		if (!new File(Config.get().getProperty("default_folder")).exists())
			new File(Config.get().getProperty("default_folder")).mkdir();
		
		projectManager = new ProjectManager();
		contextManager = new ContextManager();
			
		todo = new StoredList<Task>(getTodoFile(), false, 
				task -> task.encode(), string -> decode(string, true) , task -> task.EditDateProperty());
		done = new StoredList<Task>(getDoneFile(), false, 
				task -> task.encode(), string -> decode(string, false), task -> task.EditDateProperty());
		done.close();
		
		todo.setSaveOnEdits(10);
	}
	
	public void save() {
		try {
			todo.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadArchive() {
		try {
			done.load();
		} catch (IOException e) {}
	}
	
	public void closeArchive() {
		done.close();
	}

	public void reload() {
		try {
			todo.setFile(getTodoFile(), false);
		} catch (IOException e) {}
	}	
	
	private File getTodoFile() {
		String path = Config.get().getPropertyAndSave("todo_file", "todo.txt");
		if (!new File(path).isAbsolute()) {
			String folder = Config.get().getProperty("default_folder");
			path = new File(folder, path).getAbsolutePath();
		}
		return new File(path);
	}
	
	private File getDoneFile() {
		String path = Config.get().getPropertyAndSave("done_file", "done.txt");
		if (!new File(path).isAbsolute()) {
			String folder = Config.get().getProperty("default_folder");
			path = new File(folder, path).getAbsolutePath();
		}
		return new File(path);
	}
	
	public StoredList<Task> getTodoList() {
		return todo;
	}
	
	public StoredList<Task> getDoneList() {
		return done;
	}
  	
	public void archiveTasks() {
		loadArchive();
		
		List<Task> dones = todo.filter(task -> !task.getState().equals(Task.State.TODO));
		
		for (Task t : dones)
			for (Project p : t.getProjects()) {
				projectManager.decrementProject(p, true);
				projectManager.incrementProject(p, false);
			}
		
		done.addAll(dones);
		todo.removeAll(dones);
		
		closeArchive();
		try {
			todo.save();
		} catch (IOException e) {}
	}
	
	public void restore(Task task) {
		if (!done.contains(task))
			return;
		done.remove(task);
		todo.add(task);
		
		for (Project p : task.getProjects()) {
			projectManager.decrementProject(p, false);
			projectManager.incrementProject(p, true);
		}
	}
	
	public ProjectManager getProjectManager() {
		return projectManager;
	}

	public ContextManager getContextManager() {
		return contextManager;
	}

	public Task addTask(String input, boolean active) {
		Task t = parseString(input, active);
		todo.add(t);
		return t;
	}
	
	private void addProject(Task t, String proj, boolean active) {
		Project project = projectManager.createProject(proj, active);
		t.getProjects().add(project);
		t.setEditDateNow();	
	}
	
	private void addContext(Task t, String cont, boolean active) {
		Context context = contextManager.createContext(cont, active);
		t.getContexts().add(context);
		t.setEditDateNow();
	}
	
	public Task decode(String s, boolean active) {
		Task t = new Task();

		// Defining state
		Pattern stateP = Pattern.compile("^\\[(x| |-)\\] ");
		Matcher stateM = stateP.matcher(s);
		stateM.find();
		
		switch (stateM.group().charAt(1)) {
		case 'x':
			t.setState(Task.State.DONE);
			break;
		case ' ':
			t.setState(Task.State.TODO);
			break;
		case '-':
			t.setState(Task.State.FAILED);
		}
		
		// Defining completion date
		Pattern doneP = Pattern.compile("DONE=(\\S*) ");
		Matcher doneM = doneP.matcher(s);	
		if (doneM.find())
			t.setCompletionDate(DateUtils.parseDateTime(doneM.group(1), "yyyy.MM.dd@HH:mm"));

		// Defining priority
		Pattern priP = Pattern.compile("\\(([A-Z])\\) ");
		Matcher priM = priP.matcher(s);
		if (priM.find())
			t.setPriority(priM.group(1).charAt(0));
		
		// Defining due date
		Pattern dueP = Pattern.compile("DUE=(\\S*) ");
		Matcher dueM = dueP.matcher(s);	
		if (dueM.find())
			t.setDueDate(new DueDate(DateUtils.parseDateTime(
					dueM.group(1), "yyyy.MM.dd@HH:mm", "yyyy.MM.dd"), dueM.group(1).contains("@")));
		
		// Defining projects
		Pattern projP = Pattern.compile("PROJS=(\\S*) ");
		Matcher projM = projP.matcher(s);
		if (projM.find())
			for (String proj : projM.group(1).split(","))
				addProject(t, proj, active);
		
		// Defining contexts
		Pattern contP = Pattern.compile("CONTEXTS=(\\S*) ");
		Matcher contM = contP.matcher(s);
		if (contM.find())
			for (String cont : contM.group(1).split(","))
				addContext(t, cont, active);
		
		// Defining creationDate
		Pattern madeP = Pattern.compile("MADE=(\\S*) ");
		Matcher madeM = madeP.matcher(s);	
		madeM.find();
		t.setCreationDate(DateUtils.parseDateTime(madeM.group(1), "yyyy.MM.dd@HH:mm"));
		
		// Defining name
		// Accepts things between unescaped quotes (NAME="Read \"this book\" now")
		Pattern nameP = Pattern.compile("NAME=\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");
		Matcher nameM = nameP.matcher(s);
		nameM.find();
		t.setName(nameM.group(1).replace("\\\"", "\""));
		
		// Defining note
		Pattern noteP = Pattern.compile("NOTE=\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");
		Matcher noteM = noteP.matcher(s);
		if (noteM.find())
			t.setNote(noteM.group(1).replace("\\\\n", "\n").replace("\\\"", "\""));
				
		// Defining edtDate
		Pattern editP = Pattern.compile("EDIT=(\\S*)[\n]?$");
		Matcher editM = editP.matcher(s);	
		editM.find();
		t.setEditDate(DateUtils.parseDateTime(editM.group(1), "yyyy.MM.dd@HH:mm"));

		return t;
	}
	
	private Task parseString(String s, boolean active) {
		Task t = new Task();
		
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
			t.setPriority(matcher.group(1).charAt(0));
			s = s.replace(matcher.group(), "");
		}
		
		// Parsing the due date
		pattern = Pattern.compile("due=(\\S*) ");
		matcher = pattern.matcher(s);
		if (matcher.find()) {
			if (matcher.group(1).length() > 0) {
				HashMap<String, Integer> defaults = new HashMap<String, Integer>();
				defaults.put("m", 0);
				
				DueDate dueDate = new DueDate();
				dueDate.setHasTime(matcher.group(1).contains("@"));
				dueDate.setDateTime(DateUtils.parseMultiFormat(matcher.group(1), defaults));
				
				t.setDueDate(dueDate);
			}
			s = s.replace(matcher.group(), "");
		}
		
		// Parsing the note
		pattern = Pattern.compile("note=\"(.+)\" ");
		matcher = pattern.matcher(s);
		if (matcher.find()) {
			t.setNote(matcher.group(1));
			s = s.replace(matcher.group(), "");
		}
		
		// Parsing the projects and contexts
		List<String> words = Arrays.asList(s.split(" "));
		List<String> projects = words.stream().filter(word -> word.startsWith("+")).collect(Collectors.toList());
		List<String> contexts = words.stream().filter(word -> word.startsWith("@")).collect(Collectors.toList());
		
		for (String proj : projects)
			addProject(t, proj, active);
		for (String cont : contexts)
			addContext(t, cont, active);
		
		// Removes projects and contexts from the list of "normal" words
		words = words.stream().filter(word -> !projects.contains(word) && !contexts.contains(word)).collect(Collectors.toList());
		s = String.join(" ", words);
		
		t.setName(s.trim());
		
		return t;
	}
}
