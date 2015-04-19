package managers;

import java.io.File;

import objects.Task;
import tools.Config;
import tools.StoredList;

public class TaskManager {

	Config config;
	StoredList<Task> todo, done;
	
	
	public TaskManager(Config config) {
		this.config = config;
		todo = new StoredList<>(getTodoFile(), false, task -> task.encode(), string -> Task.decode(string));
		done = new StoredList<>(getDoneFile(), false, task -> task.encode(), string -> Task.decode(string));
	}
	
	private File getTodoFile() {
		String path = config.getProperty("todo_file", "todo.txt");
		if (new File(path).isAbsolute()) {
			String folder = config.getProperty("default_folder", "data");
			path = new File(folder, path).getAbsolutePath();
		}
		return new File(path);
	}
	
	private File getDoneFile() {
		String path = config.getProperty("done_file", "todo.txt");
		if (new File(path).isAbsolute()) {
			String folder = config.getProperty("default_folder", "data");
			path = new File(folder, path).getAbsolutePath();
		}
		return new File(path);
	}
	
	public void setTodoFile(String path) {
		config.setProperty("todo_file", path);
	}
	
	public void setDoneFile(String path) {
		config.setProperty("done_file", path);
	}
	
}
