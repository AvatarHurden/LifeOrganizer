package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import managers.TaskManager;
import tools.Config;
import views.ConfigView.ConfigViewController;
import views.TaskOverview.TaskOverviewController;

public class Main extends Application {

	private Config config;
	private TaskManager manager;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		config = new Config(new File("config.txt"));
		
		manager = new TaskManager(config);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TaskOverview/TaskOverview.fxml"));

		Scene scene = new Scene((Parent) loader.load());
		
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
	
	private Stage getConfigStage() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ConfigView/ConfigView.fxml"));

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
		config.setListProperty("windowPos", pos, d -> d.toString());
	}
	
	private void setPosition(Window window) {
		List<Double> pos = config.getListProperty("windowPos", s -> Double.valueOf(s), null);
		if (pos == null)
			return;
		
		window.setX(pos.get(0));
		window.setY(pos.get(1));
		window.setHeight(pos.get(2));
		window.setWidth(pos.get(3));
	}
	
	
}
