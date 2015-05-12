package io.github.avatarhurden.lifeorganizer.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.AnchorPane;

public class ProjectCell extends TreeCell<String> {

	@FXML
	private AnchorPane root;
	@FXML
	private Label nameLabel, childrenLabel;
	
	public ProjectCell() {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/fxml/ProjectCell.fxml"));
			loader.setController(this);
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}
	
	@FXML
	private void initiliaze() {
		
	}
	
	public void setValue(String name, int children) {
		nameLabel.setText(name);
		childrenLabel.setText(""+children);
	}
	
	@Override
	public void updateItem(String task, boolean empty) {
		super.updateItem(task, empty);
		
		if (!empty) {
			setAlignment(Pos.CENTER);
//			setDisclosureNode(null);
			setValue(task, getIndex());
			setGraphic(root);
		} else
			setGraphic(null);
		
	}
}
