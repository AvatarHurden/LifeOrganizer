package io.github.avatarhurden.lifeorganizer.controllers;

import java.io.IOException;

import io.github.avatarhurden.lifeorganizer.controllers.ProjectCell;
import io.github.avatarhurden.lifeorganizer.objects.Status;
import io.github.avatarhurden.lifeorganizer.ui.StatusSelector;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeCell;
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
	
	@FXML
	private void initialize() {
		inboxTab.setGraphic(inboxTabGraphic());
		soonTab.setGraphic(soonTabGraphic());
		projectsTab.setGraphic(projectsTabGraphic());
		contextsTab.setGraphic(contextsTabGraphic());
		
		tabs.setTabMinHeight(106);
		tabs.setTabMaxHeight(106);
		
		TreeItem<String> parentProject = new TreeItem<String>();
		for (int i = 0; i < 10; i++)
			parentProject.getChildren().add(new TreeItem<String>("Project " +i));
		
		parentProject.getChildren().get(2).getChildren().add(new TreeItem<String>("Sub project 1"));
		parentProject.getChildren().get(2).getChildren().add(new TreeItem<String>("Sub project 2"));
		
		TreeView<String> tree = new TreeView<String>(parentProject);
		tree.setCellFactory(tree2 -> new ProjectCell());
		tree.setShowRoot(false);
		
		Property<Status> stau = new SimpleObjectProperty<Status>(Status.DONE);
		
		StatusSelector t = new StatusSelector();
		t.statusProperty().bindBidirectional(stau);
		inboxTab.setContent(t);
		stau.addListener((obs, oldValue, newValue) -> {
			System.out.println(newValue);
		});
		
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
