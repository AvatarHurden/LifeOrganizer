package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.DueDate;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.tools.DateUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import org.joda.time.DateTime;

public class TaskManager {

	private Path taskFolder;
	private Thread fileWatcher;
	
	private ObservableMap<String, Task> taskMap;
	private ObservableList<Task> taskList;
	
	private Set<String> modifiedTasks;
	
	ProjectManager projectManager;
	ContextManager contextManager;
	
	public static boolean isInitiliazed() {
		return Config.get().getProperty("task_folder") != null;
	}
	
	public TaskManager() {
		taskFolder = Paths.get(Config.get().getProperty("task_folder"));
		if (!taskFolder.toFile().exists())
			taskFolder.toFile().mkdirs();
			
		taskMap = FXCollections.<String, Task>observableHashMap();
		taskList = FXCollections.observableArrayList();
		modifiedTasks = new HashSet<String>();
		
		taskMap.addListener((MapChangeListener.Change<? extends String, ? extends Task> change) -> 
			Platform.runLater(() -> {
				if (change.wasAdded())
					taskList.add(change.getValueAdded());
				if (change.wasRemoved())
					taskList.remove(change.getValueRemoved());
			})
		);
		
		projectManager = new ProjectManager();
		contextManager = new ContextManager();
	}
	
	public void loadAndWatch() {
		try {
			readFolder();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		listenToFolder();
	}
	
	public void close() {
		fileWatcher.interrupt();
		fileWatcher = null;
	}
	
	public Path getFolder() {
		return taskFolder;
	}
	
	public void archive() {
		for (Task t : taskList)
			if (t.getState() != Task.State.TODO && !t.isArchived())
				t.setArchived(true);
	}
	
	public ObservableList<Task> getTodoList() {
		return taskList.filtered(t -> !t.isArchived()).sorted();
	}
	
	public ObservableList<Task> getArchivedList() {
		return taskList.filtered(t -> t.isArchived()).sorted();
	}
		
	public ProjectManager getProjectManager() {
		return projectManager;
	}

	public ContextManager getContextManager() {
		return contextManager;
	}
	
	public void setTaskModified(String uuid) {
		modifiedTasks.add(uuid);
		System.out.println(uuid);
		new Thread(() -> {
			try {
				Thread.sleep(1000);
				modifiedTasks.remove(uuid);
			} catch (Exception e) {} 
		}).start();
		System.out.println(taskMap.get(uuid).toJSON());
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
	
	private void readFolder() throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(taskFolder, path -> path.getFileName().toString().endsWith(".txt"));
		for (Path file: stream) {
		  	String id = file.getFileName().toString().replace(".txt", "");
		   	taskMap.put(id, Task.loadFromPath(this, file.toFile()));
		}
	}
	
	private void listenToFolder() {
		fileWatcher = new Thread(() -> {
			
			WatchService watcher = null;
			try {
				watcher = FileSystems.getDefault().newWatchService();
				taskFolder.register(watcher, 
						StandardWatchEventKinds.ENTRY_CREATE, 
						StandardWatchEventKinds.ENTRY_DELETE, 
						StandardWatchEventKinds.ENTRY_MODIFY);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			if (watcher == null)
				return;
			
			while (true) {
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException e) { return; }
			
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
		        
					System.out.println(taskFolder.resolve((Path) event.context()).toAbsolutePath() + " " + kind);
					
					if (kind == StandardWatchEventKinds.OVERFLOW) continue;
		        
					Path file = taskFolder.resolve((Path) event.context());
		        
					if (!file.getFileName().toString().endsWith(".txt"))
						continue;

					String id = file.getFileName().toString().replace(".txt", "");
					System.out.println(modifiedTasks);
					if (modifiedTasks.contains(id))
						continue;
		    	
					Platform.runLater(() -> {
					if (kind == StandardWatchEventKinds.ENTRY_CREATE)
						try {
							taskMap.put(id, Task.loadFromPath(this, file.toFile()));
						} catch (Exception e) {	
							e.printStackTrace();
						}
					else if (kind == StandardWatchEventKinds.ENTRY_DELETE)
						taskMap.remove(id);
					else if (kind == StandardWatchEventKinds.ENTRY_MODIFY)
						try {
							taskMap.get(id).readFile(file.toFile());
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
			
				key.reset();
			}
		});
		
		fileWatcher.start();
	}
}
