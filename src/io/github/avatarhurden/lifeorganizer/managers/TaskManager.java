package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.tools.StoredList;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TaskManager {

	StoredList<Task> todo, done;
	
	ProjectManager activeProjects, inactiveProjects;
	ContextManager activeContexts, inactiveContexts;
	
	public TaskManager() {
		
		if (!new File(Config.get().getPropertyAndSave("default_folder", new File("data").getAbsolutePath())).exists())
			new File(Config.get().getProperty("default_folder")).mkdir();
		
		activeProjects = new ProjectManager();
		activeContexts = new ContextManager();
		inactiveProjects = new ProjectManager();
		inactiveContexts = new ContextManager();
			
		todo = new StoredList<Task>(getTodoFile(), false, task -> task.encode(), string -> Task.decode(string, activeProjects, activeContexts), task -> task.EditDateProperty());
		done = new StoredList<Task>(getDoneFile(), false, task -> task.encode(), string -> Task.decode(string, inactiveProjects, inactiveContexts), task -> task.EditDateProperty());
	
		todo.setSaveOnEdits(10);
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
	
	public Task addTask(String input) {
		Task t = new Task(input, activeProjects, activeContexts);
		todo.add(t);
		return t;
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
	
	public ProjectManager getActiveProjects() {
		return activeProjects;
	}

	public ContextManager getActiveContexts() {
		return activeContexts;
	}
}
