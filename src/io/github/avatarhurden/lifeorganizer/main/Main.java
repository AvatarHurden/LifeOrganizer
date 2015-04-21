package io.github.avatarhurden.lifeorganizer.main;

import io.github.avatarhurden.lifeorganizer.managers.TaskManager;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.views.ConfigView.ConfigViewController;
import io.github.avatarhurden.lifeorganizer.views.TaskOverview.TaskOverviewController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.controlsfx.control.NotificationPane;

public class Main extends Application {

	private static final double version = 0.1;
	private static final String changelogURL = "file:///C:/Users/Arthur/Documents/GitHub/LifeOrganizer-GUI/changelog";
//			"https://raw.githubusercontent.com/AvatarHurden/LifeOrganizer-GUI/master/changelog?token=AE2NPpgsr9kr3FbS2khMJ-zKXVUK5OrCks5VPufAwA%3D%3D";
	
	private Config config;
	private TaskManager manager;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		config = new Config(new File("config.txt"));
		
		manager = new TaskManager(config);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/avatarhurden/lifeorganizer/views/TaskOverview/TaskOverview.fxml"));

		NotificationPane pane = new NotificationPane(loader.load());
		Scene scene = new Scene(pane);
		
		startUpdater(pane);
		
		TaskOverviewController controller = loader.<TaskOverviewController>getController();
		controller.setTaskManager(manager);
		controller.setConfigStage(getConfigStage());
		controller.loadState(config);
		
		primaryStage.setScene(scene);
		primaryStage.show();
		
		setPosition(primaryStage);
		
		primaryStage.setOnCloseRequest(event -> {
			try {
				savePosition(primaryStage);
				controller.saveState(config);
				config.save();
				manager.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
	}
	
	private void startUpdater(NotificationPane pane) {
		Platform.runLater(() -> {
			Updater up = new Updater(version, changelogURL, pane);
			up.start();
		});
	}
	
	private Stage getConfigStage() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/avatarhurden/lifeorganizer/views/ConfigView/ConfigView.fxml"));

		Stage stage = new Stage();
		stage.setScene(new Scene((Parent) loader.load()));  
		
		ConfigViewController controller = loader.<ConfigViewController>getController();
		controller.setConfig(config);
		
		controller.addActionOnExit(() -> manager.reload());
		
		return stage;
	}
	
	private void savePosition(Window window) {
		List<Double> pos = new ArrayList<Double>();
		pos.add(window.getX());
		pos.add(window.getY());
		pos.add(window.getHeight());
		pos.add(window.getWidth());
		config.setListProperty("window_position", pos, d -> d.toString());
	}
	
	private void setPosition(Window window) {
		List<Double> pos = config.getListProperty("window_position", s -> Double.valueOf(s), null);
		if (pos == null)
			return;
		
		window.setX(pos.get(0));
		window.setY(pos.get(1));
		window.setHeight(pos.get(2));
		window.setWidth(pos.get(3));
	}
	
	
}
