package io.github.avatarhurden.lifeorganizer.controllers;

import io.github.avatarhurden.lifeorganizer.managers.LegacyParser;
import io.github.avatarhurden.lifeorganizer.tools.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.stage.DirectoryChooser;

public class StartupViewController {

	@FXML
	private TitledPane locationPane, creationPane;
	@FXML
	private Accordion accordion;
	@FXML
	private Button dropboxButton;
	@FXML
	private Label noOnlineLabel;
	@FXML
	private Hyperlink noOnlineLink;
	
	private FXMLLoader loader;
	private Runnable onClose;
	
	private boolean getOld = false;
	
	private File localLocation;
	private File onlineLocation;
	
	public StartupViewController() {
		loader = new FXMLLoader(
				getClass().getResource("/fxml/StartupView.fxml"));
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		localLocation = new File(System.getProperty("user.home"), 
				"Documents" + File.separator + "LifeOrganizer" + File.separator + "tasks");
		onlineLocation = new File(System.getProperty("user.home"), 
				"Dropbox" + File.separator + "Apps" + File.separator + "LifeOrganizer" + File.separator + "tasks");
		
		dropboxButton.setDisable(!new File(System.getProperty("user.home"), "Dropbox").exists());
		noOnlineLabel.visibleProperty().bind(dropboxButton.disabledProperty());
		noOnlineLink.visibleProperty().bind(dropboxButton.disabledProperty());
		
		accordion.setExpandedPane(creationPane);
	}
	
	public Node getView() {	
		return loader.getRoot();
	}
	
	@FXML
	private void newTasks() {
		accordion.setExpandedPane(locationPane);
	}
	
	@FXML
	private void importOld() {
		accordion.setExpandedPane(locationPane);
		getOld = true;
	}
	
	@FXML
	private void onlineSync() {
		Config.get().setProperty("task_folder", onlineLocation.getAbsolutePath());
		if (getOld)
			try {
				LegacyParser.convert();
			} catch (FileNotFoundException e) {}
		onClose.run();
	}
	
	@FXML
	private void localSync() {
		Config.get().setProperty("task_folder", localLocation.getAbsolutePath());
		if (getOld)
			try {
				LegacyParser.convert();
			} catch (FileNotFoundException e) {}
		onClose.run();
	}
	
	@FXML
	private void openLocationDialog() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File selected = chooser.showDialog(getView().getScene().getWindow());
		if (selected != null)
			localLocation = new File(selected, "tasks");
	}
	
	@FXML
	private void openDropboxDialog() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File selected = chooser.showDialog(getView().getScene().getWindow());
		if (selected != null) {
			onlineLocation = new File(selected, "Apps" + File.separator + "LifeOrganizer" + File.separator + "tasks");
			dropboxButton.setDisable(false);
		}
	}

	public void setOnClose(Runnable onClose) {
		this.onClose = onClose;
	}
}
