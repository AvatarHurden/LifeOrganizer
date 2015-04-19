package managers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import objects.Task;
import tools.Config;
import tools.StoredList;

public class TaskManager {

	Config config;
	StoredList<Task> todo, done;
	
	public TaskManager(Config config) {
		this.config = config;
		
		if (!new File(config.getPropertyAndSave("default_folder", new File("data").getAbsolutePath())).exists())
			new File(config.getProperty("default_folder")).mkdir();
			
		todo = new StoredList<Task>(getTodoFile(), false, task -> task.encode(), string -> Task.decode(string));
		done = new StoredList<Task>(getDoneFile(), false, task -> task.encode(), string -> Task.decode(string));
	
		todo.setSaveOnEdits(10);
	}
	
	private File getTodoFile() {
		String path = config.getPropertyAndSave("todo_file", "todo.txt");
		if (!new File(path).isAbsolute()) {
			String folder = config.getProperty("default_folder", "data");
			path = new File(folder, path).getAbsolutePath();
		}
		return new File(path);
	}
	
	private File getDoneFile() {
		String path = config.getPropertyAndSave("done_file", "done.txt");
		if (!new File(path).isAbsolute()) {
			String folder = config.getProperty("default_folder", "data");
			path = new File(folder, path).getAbsolutePath();
		}
		return new File(path);
	}
	
	public StoredList<Task> getTodoList() {
		return todo;
	}
	
	public void addTask(Task t) {
		todo.add(t);
	}
  	
	public void archiveTasks() {
		List<Task> dones = todo.filter(task -> !task.getState().equals(Task.State.TODO));
		done.addAll(dones);
		todo.removeAll(dones);
		try {
			done.save();
			todo.save();
		} catch (IOException e) {}
	}
	
	public void save() {
		try {
			done.save();
			todo.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void reload() {
		try {
			todo.setFile(getTodoFile(), false);
			done.setFile(getDoneFile(), false);
		} catch (IOException e) {}
	}
	
}
