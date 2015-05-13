package io.github.avatarhurden.lifeorganizer.controllers;

import io.github.avatarhurden.lifeorganizer.managers.TaskManager;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.ui.DeleteButton;
import io.github.avatarhurden.lifeorganizer.ui.RemoveButton;
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
	private AnchorPane root, statusPane, removePane, deletePane;
	@FXML
	private TextField nameField, noteField;
	
	private StatusSelector status;
	private DeleteButton delete;
	private RemoveButton remove;
	
	private Task current;
	
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
		getStylesheets().add("/style/TaskCell.css");
		getStyleClass().setAll("cell");
		root.getStyleClass().setAll("cell");
		
		status = new StatusSelector(false);
		statusPane.getChildren().add(status);
		
		delete = new DeleteButton();
		deletePane.getChildren().add(delete);
		
		remove = new RemoveButton();
		removePane.getChildren().add(remove);
	}
	
	private void setTask(Task t) {
		
		if (current != null)
			status.statusProperty().unbindBidirectional(current.statusProperty());
		status.statusProperty().bindBidirectional(t.statusProperty());
		if (current != null)
			nameField.textProperty().unbindBidirectional(current.nameProperty());
		nameField.textProperty().bindBidirectional(t.nameProperty());
		if (current != null)
			noteField.textProperty().unbindBidirectional(current.noteProperty());
		noteField.textProperty().bindBidirectional(t.noteProperty());


//		remove.setOnAction(event -> TaskManager.get().deleteTask(t.getUUID()));
		delete.setOnAction(event -> {
			TaskManager.get().deleteTask(t.getUUID());
		});
	}
	
	@Override
	public void updateItem(Task task, boolean empty) {
		super.updateItem(task, empty);
		
		System.out.println(task);
		System.out.println(empty);
		if (!empty && task != null) {
			setAlignment(Pos.CENTER);
//			setDisclosureNode(null);
			setTask(task);
			setGraphic(root);
		} else
			setGraphic(null);
		
	}
}
