package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.Project;

import java.util.List;

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
	
	public void incrementProjects(boolean isActive, Project... p) {
		for (Project project : p)
			project.incrementCount(isActive);
	}

	public void incrementProjects(boolean isActive, List<Project> p) {
		for (Project project : p)
			project.incrementCount(isActive);
	}
	
	public void decrementProjects(boolean isActive, Project... p) {
		for (Project project : p) {
			project.decrementCount(isActive);
			if (!project.isActive() && project.getInactiveTasks() == 0)
				projects.remove(project);
		}
	}
	
	public void decrementProjects(boolean isActive, List<Project> p) {
		for (Project project : p) {
			project.decrementCount(isActive);
			if (!project.isActive() && project.getInactiveTasks() == 0)
				projects.remove(project);
		}
	}
	
	public void moveProjects(boolean toActive, List<Project> p) {
		decrementProjects(!toActive, p);
		incrementProjects(toActive, p);
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
