package io.github.avatarhurden.lifeorganizer.controllers;

import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.ui.StatusSelector;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.AnchorPane;

public class TaskCell extends TreeCell<Task> {
	
	@FXML
	private AnchorPane root, statusPane;
	@FXML
	private TextField nameField, noteField;
	
	private StatusSelector status;
	
	public TaskCell() {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/fxml/TaskCell.fxml"));
			loader.setController(this);
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}
	
	@FXML
	private void initialize() {
		status = new StatusSelector(false);
		statusPane.getChildren().add(status);
	}
	
	private void setTask(Task t) {
		
		status.statusProperty().bindBidirectional(t.statusProperty());
		nameField.textProperty().bindBidirectional(t.nameProperty());
		noteField.textProperty().bindBidirectional(t.noteProperty());
		
	}
	
	@Override
	public void updateItem(Task task, boolean empty) {
		super.updateItem(task, empty);
		
		if (getTreeItem() != null)
			System.out.println(getTreeItem().getValue() + " " + getTreeItem().isExpanded());
		
		if (!empty) {
			setAlignment(Pos.CENTER);
//			setDisclosureNode(null);
			setTask(task);
			setGraphic(root);
		} else
			setGraphic(null);
		
	}
}
