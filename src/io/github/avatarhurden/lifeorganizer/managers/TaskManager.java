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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.Pair;

import org.joda.time.DateTime;
import org.json.JSONArray;

public class TaskManager {
	
	private static final TaskManager instance = new TaskManager();
	
	public static TaskManager get() {
		return instance;
	}
	
	private ObservableMap<String, Task> tasks;
	private ObservableMap<String, Context> contexts;
	
	private static final String activeFile = "active.txt";
	private static final String completedFile = "completed.txt";
	private static final String canceledFile = "canceled.txt";
	
	private Path folder, activePath, completedPath, canceledPath;
	
	/** Tasks that should have changes ignored when it comes to saving the file **/
	private ObservableList<String> tasksToIgnore;
	
	private ObservableList<String> active, done, canceled;
	
	private List<Pair<ObservableList<Task>, Predicate<Task>>> requestedLists;
	
	private DirectoryWatcher watcher;
	
	public static boolean isInitiliazed() {
		return Config.get().getProperty("task_folder") != null;
	}
	
	public TaskManager() {
		folder = Paths.get(Config.get().getProperty("task_folder"));
		watcher = new DirectoryWatcher(folder);
		
		if (!folder.toFile().exists())
			folder.toFile().mkdirs();
		
		activePath = folder.resolve(activeFile);
		completedPath = folder.resolve(completedFile);
		canceledPath = folder.resolve(canceledFile);
		
		try {
			if (!activePath.toFile().exists())
				JSONFile.saveJSONArray(new JSONArray(), activePath.toFile(), 4);
			if (!completedPath.toFile().exists())
				JSONFile.saveJSONArray(new JSONArray(), completedPath.toFile(), 4);
			if (!canceledPath.toFile().exists())
				JSONFile.saveJSONArray(new JSONArray(), canceledPath.toFile(), 4);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		requestedLists = new ArrayList<Pair<ObservableList<Task>, Predicate<Task>>>();
		
		tasks = FXCollections.<String, Task>observableHashMap();
		
		active = FXCollections.observableArrayList();
		done = FXCollections.observableArrayList();
		canceled = FXCollections.observableArrayList();

		tasksToIgnore = FXCollections.observableArrayList();
		
		readActive();
		readDone();
		readCanceled();
		for (String uuid : active)
			try {
				loadTask(uuid, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
//		printHierarchy();
	}
	
	public ObservableList<Task> requestList(Predicate<Task> predicate) {
		ObservableList<Task> list = FXCollections.<Task>observableArrayList();
		
		for (Task t : tasks.values())
			if (predicate.test(t))
				list.add(t);
		
		requestedLists.add(new Pair<ObservableList<Task>, Predicate<Task>>(list, predicate));
		
		return list;
	}
	
	public ObservableList<String> getActiveTasks() {
		return active;
	}

	public Task getTask(String uuid) {
		return tasks.get(uuid);
	}

	public Context getContext(String uuid) {
		return contexts.get(uuid);
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
		
		for (int i = 0; i < j.length(); i++)
			active.add(j.getString(i));
		
		active.addListener((ListChangeListener.Change<? extends String> event) -> {
			try {
				JSONFile.saveJSONArray(new JSONArray((String[]) active.toArray(new String[active.size()])), activePath.toFile(), 4);
			} catch (IOException e) {}
		});
	}
	
	private void readDone() {
		JSONArray j = null;
		try {
			j = JSONFile.loadJSONArray(completedPath.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < j.length(); i++)
			done.add(j.getString(i));
		
		done.addListener((ListChangeListener.Change<? extends String> event) -> {
			try {
				JSONFile.saveJSONArray(new JSONArray((String[]) done.toArray(new String[done.size()])), completedPath.toFile(), 4);
			} catch (IOException e) {}
		});
	}
	
	private void readCanceled() {
		JSONArray j = null;
		try {
			j = JSONFile.loadJSONArray(canceledPath.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < j.length(); i++)
			canceled.add(j.getString(i));
		
		canceled.addListener((ListChangeListener.Change<? extends String> event) -> {
			try {
				JSONFile.saveJSONArray(new JSONArray((String[]) canceled.toArray(new String[canceled.size()])), canceledPath.toFile(), 4);
			} catch (IOException e) {}
		});
	}
	
	/**
	 * Loads the specified task from the file, adding it to the task map.
	 * 
	 * @param uuid - The UUID of the task to be loaded.
	 * @throws IOException 
	 */
	private void loadTask(String uuid, boolean loadChildren) throws IOException {
		Path p = getPathForTask(uuid);
		Task t = getTask(uuid);
		System.out.println(t);
		if (t != null)
			t.loadJSON(JSONFile.loadJSONObject(p.toFile()));
		else {
			t = Task.loadFromJSON(JSONFile.loadJSONObject(p.toFile()));
			tasks.put(uuid, t);
			addTaskListeners(uuid);
		
		}
		
		if (loadChildren)
			for (String child : t.getChildren())
				loadTask(child, true);
	}
	
	public Task createNewTask() {
		Task t = Task.createNew();
		
		saveTask(t.getUUID());
		tasks.put(t.getUUID(), t);
		active.add(t.getUUID());
		addTaskListeners(t.getUUID());
		
		for (Pair<ObservableList<Task>, Predicate<Task>> pair : requestedLists)
			if (pair.getValue().test(t))
				pair.getKey().add(t);
		
		return t;
	}
	
	public void deleteTask(String uuid) {
		Task t = getTask(uuid);
		
		for (String parent : t.getParents()) // Removes references to the task
			getTask(parent).removeChild(uuid);
		for (String child : t.getChildren())
			getTask(child).removeParent(uuid);
		for (String context : t.getContexts())
			getContext(context).removeTask(uuid);
		

		for (Pair<ObservableList<Task>, Predicate<Task>> pair : requestedLists)
			pair.getKey().remove(t);
		
		if (t.getStatus() == Status.ACTIVE)
			active.remove(uuid);
		else if (t.getStatus() == Status.CANCELED)
			canceled.remove(uuid);
		else if (t.getStatus() == Status.COMPLETED)
			done.remove(uuid);
		
		tasksToIgnore.add(uuid);
		getPathForTask(uuid).toFile().delete(); // Deletes the file
		tasksToIgnore.remove(uuid);

		tasks.remove(uuid); // Removes from the list
	}
	
	public void addRelationship(String parent, String child) {
		getTask(parent).addChild(child);
		getTask(child).addParent(parent);
	}
	
	public void removeRelationship(String parent, String child) {
		getTask(parent).removeChild(child);
		getTask(child).removeParent(parent);
	}
	
	public void addContext(String task, String context) {
		getTask(task).addContext(context);
		getContext(context).addTask(task);
	}
	
	public void removeContext(String task, String context) {
		getTask(task).removeContext(context);
		getContext(context).removeTask(task);
	}
	
	private void addTaskListeners(String uuid) {
		Task t = getTask(uuid);
		
		// Listener to save the task
		t.editDateProperty().addListener((obs, oldValue, newValue) -> {
			System.out.println(t.getName());
			saveTask(uuid);
			for (Pair<ObservableList<Task>, Predicate<Task>> pair : requestedLists) {
				if (!pair.getKey().contains(t) && pair.getValue().test(t))
					pair.getKey().add(t);
				else if (pair.getKey().contains(t) && !pair.getValue().test(t))
					pair.getKey().remove(t);
			}
		});
		
		// Listener to put the task in the correct category
		t.statusProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == Status.ACTIVE)
				active.add(uuid);
			else if (newValue == Status.CANCELED)
				canceled.add(uuid);
			else if (newValue == Status.COMPLETED)
				done.add(uuid);
			
			if (oldValue == Status.ACTIVE)
				active.remove(uuid);
			else if (oldValue == Status.CANCELED)
				canceled.remove(uuid);
			else if (oldValue == Status.COMPLETED)
				done.remove(uuid);
		});
	}
	
	public void saveTask(String uuid) {
		if (tasksToIgnore.contains(uuid)) return;
		
		tasksToIgnore.add(uuid);
		new Thread(() -> {
			
			try {
				Thread.sleep(3000);
				Path p = getPathForTask(uuid);
			
				watcher.ignorePath(p);
				JSONFile.saveJSONObject(getTask(uuid).toJSON(), p.toFile(), 4);

				Thread.sleep(100);
				tasksToIgnore.remove(uuid);
				watcher.watchPath(p);
			} catch (InterruptedException e) {
			}  catch (IOException exc) {
				exc.printStackTrace();
			 }
		}).start();
	}
	
	public void loadAndWatch() {
		listenToFolder();
	}
	
	public void close() {
		watcher.stopWatching();
	}
	
	public Path getFolder() {
		return folder;
	}
	
	private Task parseString(String s, boolean active) {
		Task t = Task.createNew();
		
		if (!s.endsWith(" "))
			s = s + " "; //Adds whitespace to match pattern if at end
		
		// Parsing the due date
		Pattern pattern = Pattern.compile("due=(\\S*) ");
		Matcher matcher = pattern.matcher(s);
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
		
		// Removes projects and contexts from the list of "normal" words
		words = words.stream().filter(word -> !projects.contains(word) && !contexts.contains(word)).collect(Collectors.toList());
		s = String.join(" ", words);
		
		t.setName(s.trim());
		
		return t;
	}
	
	private void listenToFolder() {
		watcher.addFilter(path -> Pattern.matches("^[0-9abcdefABCDEF]{32}\\.task", path.getFileName().toString()));
		
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
					tasksToIgnore.add(id);
					loadTask(id, false);
					tasksToIgnore.remove(id);
				} catch (Exception e) {	
					e.printStackTrace();
				}
			else if (latestKind == StandardWatchEventKinds.ENTRY_DELETE)
				tasks.remove(id);
		});
	}
}
