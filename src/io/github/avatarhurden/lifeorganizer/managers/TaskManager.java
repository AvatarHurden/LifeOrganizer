package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.DueDate;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.tools.DateUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
	private Path taskFolder, archiveFolder;
	
	private static final long READ_DELAY = 300;
	
	private Thread fileWatcher;
	private HashMap<String, WatchEvent.Kind<?>> filesToRead;
	
	private ObservableMap<String, Task> taskMap;
	private ObservableList<Task> taskList;
	
	private Set<String> ignoredTasks;
	
	ProjectManager projectManager;
	ContextManager contextManager;
	
	public static boolean isInitiliazed() {
		return Config.get().getProperty("task_folder") != null;
	}
	
	public TaskManager() {
		Path rootFolder = Paths.get(Config.get().getProperty("task_folder"));
		taskFolder = rootFolder.resolve("active");
		archiveFolder = rootFolder.resolve("archive");
		
		if (!taskFolder.toFile().exists())
			taskFolder.toFile().mkdirs();
		if (!archiveFolder.toFile().exists())
			archiveFolder.toFile().mkdirs();
			
		taskMap = FXCollections.<String, Task>observableHashMap();
		taskList = FXCollections.observableArrayList();
		ignoredTasks = new HashSet<String>();
		
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
		
		filesToRead = new HashMap<String, WatchEvent.Kind<?>>();
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
				moveToArchive(t);
	}
	
	public void moveToArchive(Task t) {
		t.setArchived(true);
		File f = t.getFile();
		try {
			Files.move(t.getFile().toPath(), 
					archiveFolder.resolve(t.getUUID() + ".txt"), 
					StandardCopyOption.REPLACE_EXISTING);
			t.setFile(archiveFolder.resolve(t.getUUID() + ".txt").toFile());
			t.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(f.exists());
	}
	
	public ObservableList<Task> getTodoList() {
		return taskList.filtered(t -> !t.isArchived()).sorted();
	}
	
	public ObservableList<Task> getTasks() {
		return taskList;
	}
		
	public ProjectManager getProjectManager() {
		return projectManager;
	}

	public ContextManager getContextManager() {
		return contextManager;
	}
	
	public void ignoreTask(String uuid) {
		ignoredTasks.add(uuid);
	}
	
	public void removeIgnore(String uuid) {
		ignoredTasks.remove(uuid);
	}

	public Task addTask(String input, boolean active) {
		Task t = parseString(input, active);
		taskMap.put(t.getUUID(), t);
		return t;
	}
	
	public void deleteTask(Task task) {
		System.out.println(task.getName());
		taskMap.remove(task.getUUID());
		task.delete();
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
			t.setPriorityValue(matcher.group(1).charAt(0));
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
				
					t.setDueDateValue(dueDate);
				}
			}
			s = s.replace(matcher.group(), "");
		}
		
		// Parsing the note
		pattern = Pattern.compile("note=\"(.+)\" ");
		matcher = pattern.matcher(s);
		if (matcher.find()) {
			t.setNoteValue(matcher.group(1));
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
		
		t.setNameValue(s.trim());
		
		return t;
	}
	
	private void readFolder() throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(taskFolder, 
				path -> Pattern.matches("^[0-9abcdefABCDEF]{32}\\.txt", path.getFileName().toString()));
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
		        
					if (kind == StandardWatchEventKinds.OVERFLOW) continue;
		        
					Path file = taskFolder.resolve((Path) event.context());
		        
					// Only registers for matching filenames
					if (!Pattern.matches("^[0-9abcdefABCDEF]{32}\\.txt", file.getFileName().toString())) 
						continue;
					
					String id = file.getFileName().toString().replace(".txt", "");
					if (ignoredTasks.contains(id))
						continue;
					
					if (!filesToRead.containsKey(id))
					new Thread(() -> {
						try {
							Thread.sleep(READ_DELAY);
						} catch (Exception e1) {}
						readFile(id, file);
						filesToRead.remove(id);
					}).start();

					filesToRead.put(id, kind);
				}
			
				key.reset();
			}
		});
		
		fileWatcher.start();
	}
	
	private void readFile(String id, Path file) {
		WatchEvent.Kind<?> latestKind = filesToRead.get(id);
		
		Platform.runLater(() -> {
			if (latestKind == StandardWatchEventKinds.ENTRY_CREATE)
				try {
					taskMap.put(id, Task.loadFromPath(this, file.toFile()));
				} catch (Exception e) {	
					e.printStackTrace();
				}
			else if (latestKind == StandardWatchEventKinds.ENTRY_DELETE)
				taskMap.remove(id);
			else if (latestKind == StandardWatchEventKinds.ENTRY_MODIFY)
				try {
					taskMap.get(id).readFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
		});
	}
}
