package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProjectManager {

	private ObservableList<Project> activeProjects;
	
	public ProjectManager() {
		activeProjects = FXCollections.observableArrayList();
	}
	
	public Project getProject(String name) {
		for (Project p : activeProjects)
			if (p.getName().equals(name))
				return p;
		return null;
	}
	
	public Project createProject(String name) {
		Project project = new Project(name);
		activeProjects.add(project);
		return project;
	}
	
	public ObservableList<Project> getActiveProjects() {
		return activeProjects;
	}
	

}
