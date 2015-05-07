package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.Context;
import io.github.avatarhurden.lifeorganizer.objects.DueDate;
import io.github.avatarhurden.lifeorganizer.objects.Status;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.tools.DateUtils;
import io.github.avatarhurden.lifeorganizer.tools.DirectoryWatcher;
import io.github.avatarhurden.lifeorganizer.tools.JSONFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import org.joda.time.DateTime;
import org.json.JSONArray;

public class TaskManager {
	
	private static ObservableMap<String, Task> tasks;
	private static ObservableMap<String, Context> contexts;
	
	public static Task getTask(String uuid) {
		return tasks.get(uuid);
	}

	public static Context getContext(String uuid) {
		return contexts.get(uuid);
	}
	
	private static final String activeFile = "active.txt";
	private static final String doneFile = "done.txt";
	private static final String canceledFile = "canceled.txt";
	
	private Path folder, activePath, donePath, canceledPath;
	
	private ObservableList<String> savingTasks;
	private ObservableList<String> active, done, canceled;
	
	private DirectoryWatcher watcher;
	
	public static boolean isInitiliazed() {
		return Config.get().getProperty("task_folder") != null;
	}
	
	public TaskManager() {
		Path folder = Paths.get(Config.get().getProperty("task_folder"));
		activePath = folder.resolve(activeFile);
		donePath = folder.resolve(doneFile);
		canceledPath = folder.resolve(canceledFile);
		
		if (!folder.toFile().exists())
			folder.toFile().mkdirs();
			
		tasks = FXCollections.<String, Task>observableHashMap();
		
		readActive();
	}
	
	public Path getPathForTask(String uuid) {
		return folder.resolve(uuid+".task");
	}
	
	private void readActive() {
		JSONArray j = null;
		try {
			j = JSONFile.loadJSONArray(activePath.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < j.length(); i++) {
			String uuid = j.getString(i);
			if (!tasks.containsKey(uuid))
				try {
					loadTask(uuid, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			active.add(uuid);
		}
	}
	
	private void readDone() {
		JSONArray j = null;
		try {
			j = JSONFile.loadJSONArray(donePath.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < j.length(); i++) {
			String uuid = j.getString(i);
			if (!tasks.containsKey(uuid))
				try {
					loadTask(uuid, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			done.add(uuid);
		}
	}
	
	private void readCanceled() {
		JSONArray j = null;
		try {
			j = JSONFile.loadJSONArray(canceledPath.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < j.length(); i++) {
			String uuid = j.getString(i);
			if (!tasks.containsKey(uuid))
				try {
					loadTask(uuid, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			canceled.add(uuid);
		}
	}
	
	/**
	 * Loads the specified task from the file, adding it to the task map.
	 * 
	 * @param uuid - The UUID of the task to be loaded.
	 * 
	 * @param loadChildren
	 * If the function should recurse down the children of the task
	 * @throws IOException 
	 */
	private void loadTask(String uuid, boolean loadChildren) throws IOException {
		Path p = getPathForTask(uuid);
		Task t = Task.loadFromJSON(JSONFile.loadJSONObject(p.toFile()));
		addTaskListeners(uuid);
		
		tasks.put(uuid, t);
		if (loadChildren)
			for (String child : t.getChildren())
				loadTask(child, true);
	}
	
	public Task createNewTask() {
		Task t = Task.createNew();
		tasks.put(t.getUUID(), t);
		return t;
	}
	
	private void addTaskListeners(String uuid) {
		Task t = getTask(uuid);
		
		// Listener to save the task
		t.editDateProperty().addListener((obs, oldValue, newValue) -> {
			if (savingTasks.contains(t)) return;
			
			savingTasks.add(uuid);
			new Thread(() -> {
				
				try {
					Thread.sleep(3000);
					Path p = getPathForTask(uuid);
				
					watcher.ignorePath(p);
					JSONFile.saveJSONObject(t.toJSON(), p.toFile(), 4);

					Thread.sleep(100);
					savingTasks.remove(t);
					watcher.watchPath(p);
				} catch (InterruptedException e) {
				}  catch (IOException exc) {
					exc.printStackTrace();
				 }
				
			}).start();
		});
		
		// Listener to put the task in the correct category
		t.stateProperty().addListener((obs, oldValue, newValue) -> {
			if (t.hasParents()) // Only maintain top level tasks in lists
				return;
			
			if (newValue == Status.ACTIVE)
				active.add(uuid);
			else
				active.remove(uuid);
			
			if (newValue == Status.CANCELED)
				canceled.add(uuid);
			else
				canceled.remove(uuid);
			
			if (newValue == Status.DONE)
				done.add(uuid);
			else
				done.remove(uuid);
		});
		
		t.getParents().addListener((ListChangeListener.Change<? extends String> event) -> {
			if (event.getAddedSize() == event.getList().size()) // Had no parents, has now
				switch (t.getState()) {
				case ACTIVE:
					active.remove(t.getUUID());
					break;
				case DONE:
					done.remove(t.getUUID());
					break;
				case CANCELED:
					canceled.remove(t.getUUID());
					break;
				}
			else if (event.getList().size() == 0) // Has no parents now
				switch (t.getState()) {
				case ACTIVE:
					if (!active.contains(t.getUUID()))
						active.add(t.getUUID());
					break;
				case DONE:
					if (!done.contains(t.getUUID()))
						done.add(t.getUUID());
					break;
				case CANCELED:
					if (!canceled.contains(t.getUUID()))
						canceled.add(t.getUUID());
					break;
				}
		});
		
	}
	
	public void loadAndWatch() {
		listenToFolder();
	}
	
	public void close() {
		watcher.stopWatching();
		watcher = null;
	}
	
	public Path getFolder() {
		return folder;
	}
	
	public ObservableList<Task> getTodoList() {
		return taskList.filtered(t -> !t.isArchived()).sorted();
	}
	
	public ObservableList<Task> getTasks() {
		return taskList;
	}
	
	public Task addTask(String input) {
		Task t = parseString(input, active);
		tasks.put(t.getUUID(), t);
		return t;
	}
	
	public void deleteTask(Task task) {
		tasks.remove(task.getUUID());
		task.delete();
	}
	
	private Task parseString(String s, boolean active) {
		Task t = Task.createNew();
		
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
	
	private void listenToFolder() {
		watcher = new DirectoryWatcher(folder);
		watcher.addFilter(path -> Pattern.matches("^[0-9abcdefABCDEF]{32}\\.txt", path.getFileName().toString()));
		
		watcher.addAction((path, kind) -> readFile(path, kind));
		
		new Thread(() -> {
			try {
				watcher.startWatching();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	private void readFile(Path file, WatchEvent.Kind<?> latestKind) {
		String id = file.getFileName().toString().replace(".task", "");
		
		Platform.runLater(() -> {
			if (latestKind == StandardWatchEventKinds.ENTRY_CREATE ||
				latestKind == StandardWatchEventKinds.ENTRY_MODIFY)
				try {
					loadTask(id, false);
				} catch (Exception e) {	
					e.printStackTrace();
				}
			else if (latestKind == StandardWatchEventKinds.ENTRY_DELETE)
				tasks.remove(id);
		});
	}
}
