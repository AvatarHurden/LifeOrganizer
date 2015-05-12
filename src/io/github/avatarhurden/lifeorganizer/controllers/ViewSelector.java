package io.github.avatarhurden.lifeorganizer.controllers;

import io.github.avatarhurden.lifeorganizer.managers.TaskManager;
import io.github.avatarhurden.lifeorganizer.objects.Status;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import javafx.beans.property.Property;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ViewSelector {

	@FXML
	private Tab inboxTab, soonTab, projectsTab, contextsTab;
	@FXML
	private TabPane tabs;
	Property<Status> stau;
	@FXML
	private void initialize() {
		inboxTab.setGraphic(inboxTabGraphic());
		soonTab.setGraphic(soonTabGraphic());
		projectsTab.setGraphic(projectsTabGraphic());
		contextsTab.setGraphic(contextsTabGraphic());
		
		tabs.setTabMinHeight(106);
		tabs.setTabMaxHeight(106);
		
		TreeView<Task> tree = new TreeView<Task>(getAllTasks());
		
		TaskManager.get().getActiveTasks().addListener((ListChangeListener.Change<? extends String> event) -> {
			tree.setRoot(getAllTasks());
		});
		
		tree.setCellFactory(tree2 -> new TaskCell());
		tree.setShowRoot(false);
		
		inboxTab.setContent(tree);
	}
	
	private TreeItem<Task> getAllTasks() {
		TreeItem<Task> root = new TreeItem<Task>();
		
		TaskManager manager = TaskManager.get();
		for (String uuid : manager.getActiveTasks())
			if (!manager.getTask(uuid).hasParents())
				root.getChildren().add(getChildren(manager.getTask(uuid)));
		
		return root;
		
	}
	
	private TreeItem<Task> getChildren(Task task) {
		TreeItem<Task> item = new TreeItem<Task>(task);
		for (String child : task.getChildren())
			item.getChildren().add(getChildren(TaskManager.get().getTask(child)));
		return item;
	}
	
	@FXML
	private void createNewTask() {
		TaskManager.get().createNewTask();
	}
	
	private Node inboxTabGraphic() {
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		Image g = new Image("/style/inbox.png");
		box.getChildren().add(new ImageView(g));
		box.getChildren().add(new Label("Inbox"));
		return box;
	}
	
	private Node soonTabGraphic() {
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		Image g = new Image("/style/soon.png");
		box.getChildren().add(new ImageView(g));
		box.getChildren().add(new Label("Soon"));
		return box;
	}
	
	private Node projectsTabGraphic() {
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		Image g = new Image("/style/projects.png");
		box.getChildren().add(new ImageView(g));
		box.getChildren().add(new Label("Projects"));
		return box;
	}
	
	private Node contextsTabGraphic() {
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		Image g = new Image("/style/contexts.png");
		box.getChildren().add(new ImageView(g));
		box.getChildren().add(new Label("Contexts"));
		return box;
	}
	
}
