package io.github.avatarhurden.lifeorganizer.views.ConfigView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import io.github.avatarhurden.lifeorganizer.tools.Config;
	
public class ConfigViewController {
	
	private Config oldConfig;
	
	private List<Runnable> actions;
	
	@FXML
	private TextField folderPath, todoPath, donePath;

	private FXMLLoader loader;
	
	public ConfigViewController() {
		loader = new FXMLLoader(
				getClass().getResource("/io/github/avatarhurden/lifeorganizer/views/ConfigView/ConfigView.fxml"));
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException e) {}
	}
	
	public Node getView() {	
		return loader.getRoot();
	}
	
	@FXML
	private void initialize() {
		actions = new ArrayList<Runnable>();
	
		oldConfig = new Config(Config.get());
		
		folderPath.setText(oldConfig.getProperty("default_folder"));
		todoPath.setText(oldConfig.getProperty("todo_file"));
		donePath.setText(oldConfig.getProperty("done_file"));
	}
	
	@FXML
	private void selectFolderPath() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(folderPath.getText().equals("") ? null : new File(folderPath.getText()));
		File selected = chooser.showDialog(folderPath.getScene().getWindow());
		if (selected != null)
			folderPath.setText(selected.getAbsolutePath());
	}
	
	@FXML
	private void selectTodoPath() {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(folderPath.getText().equals("") ? null : new File(folderPath.getText()));
		chooser.setInitialFileName(new File(todoPath.getText()).getName());
		chooser.getExtensionFilters().add(new ExtensionFilter("TXT files", "*.txt"));
		File selected = chooser.showOpenDialog(todoPath.getScene().getWindow());
		if (selected != null)
			todoPath.setText(selected.getAbsolutePath().replace(folderPath.getText(), ""));
	}
	
	@FXML
	private void selectDonePath() {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(folderPath.getText().equals("") ? null : new File(folderPath.getText()));
		chooser.setInitialFileName(new File(donePath.getText()).getName());
		chooser.getExtensionFilters().add(new ExtensionFilter("text", "*.txt"));
		File selected = chooser.showOpenDialog(donePath.getScene().getWindow());
		if (selected != null)
			donePath.setText(selected.getAbsolutePath().replace(folderPath.getText()+"\\", ""));
	}
	
	@FXML
	private void apply() throws FileNotFoundException, IOException {
		Config.get().setProperty("default_folder", folderPath.getText());
		Config.get().setProperty("todo_file", todoPath.getText());
		Config.get().setProperty("done_file", donePath.getText());
		Config.save();
	}
	
	@FXML
	private void ok() throws FileNotFoundException, IOException {
		apply();
		for (Runnable action : actions)
			action.run();
		((Stage) folderPath.getScene().getWindow()).close();
	}
	
	@FXML
	private void cancel() {
		Config.get().restore(oldConfig);
		((Stage) folderPath.getScene().getWindow()).close();
	}

	/**
	 * <p>Add an action to run after the window is closed and changes were applied.
	 * <p>In other words, the actions added are only called after the "ok" button is pressed.
	 * 
	 * @param action
	 */
	public void addActionOnExit(Runnable action) {
		actions.add(action);
	}
	
}
