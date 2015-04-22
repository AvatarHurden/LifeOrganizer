package io.github.avatarhurden.lifeorganizer.managers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.tools.StoredList;

public class TaskManager {

	StoredList<Task> todo, done;
	
	public TaskManager() {
		
		if (!new File(Config.get().getPropertyAndSave("default_folder", new File("data").getAbsolutePath())).exists())
			new File(Config.get().getProperty("default_folder")).mkdir();
			
		todo = new StoredList<Task>(getTodoFile(), false, task -> task.encode(), string -> Task.decode(string), task -> task.EditDateProperty());
		done = new StoredList<Task>(getDoneFile(), false, task -> task.encode(), string -> Task.decode(string), task -> task.EditDateProperty());
	
		todo.setSaveOnEdits(10);
	}
	
	private File getTodoFile() {
		String path = Config.get().getPropertyAndSave("todo_file", "todo.txt");
		if (!new File(path).isAbsolute()) {
			String folder = Config.get().getProperty("default_folder", "data");
			path = new File(folder, path).getAbsolutePath();
		}
		return new File(path);
	}
	
	private File getDoneFile() {
		String path = Config.get().getPropertyAndSave("done_file", "done.txt");
		if (!new File(path).isAbsolute()) {
			String folder = Config.get().getProperty("default_folder", "data");
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
