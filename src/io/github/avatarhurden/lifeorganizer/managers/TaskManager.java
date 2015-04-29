package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.DueDate;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.tools.DateUtils;
import io.github.avatarhurden.lifeorganizer.tools.StoredList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import org.joda.time.DateTime;

public class TaskManager {

	private Path taskFolder;
	
	private ObservableMap<String, Task> taskMap;
	private ObservableList<Task> taskList;
	
	ProjectManager projectManager;
	ContextManager contextManager;
	
	public TaskManager() {
		
		if (!new File(Config.get().getProperty("default_folder")).exists())
			new File(Config.get().getProperty("default_folder")).mkdir();
		
		taskMap = FXCollections.<String, Task>observableHashMap();
		
		taskMap.addListener((MapChangeListener.Change<? extends String, ? extends Task> change) -> {
			if (change.wasAdded())
				taskList.add(change.getValueAdded());
			if (change.wasRemoved())
				taskList.remove(change.getValueRemoved());
		});
		
		projectManager = new ProjectManager();
		contextManager = new ContextManager();
			
	}
	
	public Path getFolder() {
		return taskFolder;
	}
	
	public ObservableList<Task> getTodoList() {
		return taskList;
	}
		
	public ProjectManager getProjectManager() {
		return projectManager;
	}

	public ContextManager getContextManager() {
		return contextManager;
	}

	public Task addTask(String input, boolean active) {
		Task t = parseString(input, active);
		taskMap.put(t.getUUID(), t);
		return t;
	}
	
	private Task parseString(String s, boolean active) {
		Task t = Task.createNew(this);
		
		if (!s.endsWith(" "))
			s = s + " "; //Adds whitespace to match pattern if at end
		
		// Parsing the priority
		Pattern pattern = Pattern.compile("^\\(([A-Za-z])\\)");
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
				if (!matcher.group(1).contains("h")) // If user wants due as a hour delta, 
					defaults.put("m", 0); 			// keep the current minute information
				defaults.put("s", 0);
				defaults.put("ms", 0);
				
				DateTime date = DateUtils.parseMultiFormat(matcher.group(1), defaults);
				if (date != null) {
					DueDate dueDate = new DueDate();
					dueDate.setHasTime(matcher.group(1).contains("@") || matcher.group(1).contains("h"));
					dueDate.setDateTime(date);
				
					t.setDueDate(dueDate);
				}
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
			t.addProject(proj);
		for (String cont : contexts)
			t.addContext(cont);
		
		// Removes projects and contexts from the list of "normal" words
		words = words.stream().filter(word -> !projects.contains(word) && !contexts.contains(word)).collect(Collectors.toList());
		s = String.join(" ", words);
		
		t.setName(s.trim());
		
		return t;
	}
}
