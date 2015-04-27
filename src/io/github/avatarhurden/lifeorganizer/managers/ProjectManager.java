package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProjectManager {

	private ObservableList<Project> projects;
	
	public ProjectManager() {
		projects = FXCollections.observableArrayList();
	}
	
	public Project getProject(String name) {
		for (Project p : projects)
			if (p.getName().equals(name))
				return p;
		return null;
	}
	
	public void incrementProject(Project p, boolean isActive) {
		p.incrementCount(isActive);
	}
	
	public void decrementProject(Project p, boolean isActive) {
		p.decrementCount(isActive);
		if (!p.isActive() && p.getInactiveTasks() == 0)
			projects.remove(p);
	}
	
	public Project createProject(String name, boolean isActive) {
		Project p = getProject(name);
		if (p == null)
			p = addProject(name);
		p.incrementCount(isActive);
		return p;
	}
	
	public Project addProject(String name) {
		Project project = new Project(name);
		projects.add(project);
		return project;
	}
	
	public ObservableList<Project> getProjects() {
		return projects;
	}
	
	public ObservableList<Project> getActiveProjects() {
		return projects.filtered(p -> p.isActive());
	}	

}
