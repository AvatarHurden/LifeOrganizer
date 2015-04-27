package io.github.avatarhurden.lifeorganizer.main;

import io.github.avatarhurden.lifeorganizer.managers.TaskManager;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.views.ConfigView.ConfigViewController;
import io.github.avatarhurden.lifeorganizer.views.TaskOverview.TaskOverviewController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.controlsfx.control.NotificationPane;


public class Main extends Application {

	private static final double version = 0.1;
	private static final String changelogURL = "https://raw.githubusercontent.com/AvatarHurden/LifeOrganizer/master/changelog";

	private TaskManager manager;
	private static Stage primaryStage;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Main.primaryStage = primaryStage;
		
		if (Config.get().getProperty("default_folder") == null)
			defineDataFolder();
		
		manager = new TaskManager();

		TaskOverviewController controller = new TaskOverviewController();
		controller.loadState();
		controller.setTaskManager(manager);
		controller.setConfigStage(getConfigStage());
		
		NotificationPane pane = new NotificationPane(controller.getView());
		Scene scene = new Scene(pane);
		
		startUpdater(pane);
		
		primaryStage.setTitle("LifeOrganizer");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		setPosition(primaryStage);
		
		primaryStage.setOnCloseRequest(event -> {
			try {
				savePosition(primaryStage);
				controller.saveState();
				Config.save();
				manager.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		System.out.println(manager.getDoneList().size());
	}
	
	private void startUpdater(NotificationPane pane) {
		new Thread(() -> {
			Updater up = new Updater(version, changelogURL, pane);
			up.start();
		}).start();
	}
	
	private void defineDataFolder() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initStyle(StageStyle.UTILITY);
		alert.setTitle("Choose file location");
		alert.getDialogPane().lookupButton(alert.getButtonTypes().get(0)).setDisable(true);
		alert.setHeaderText(null);
		
		VBox box = new VBox();
		box.setPrefWidth(370);
		box.getChildren().add(new Label("Please select the folder in which to save your data."));
		
		HBox pathBox = new HBox();
		VBox.setMargin(pathBox, new Insets(5));
		
		TextField field = new TextField();
		field.setPrefWidth(300);
		field.textProperty().addListener((obs, oldValue, newValue) -> {
			if (new File(newValue).exists())
				alert.getDialogPane().lookupButton(alert.getButtonTypes().get(0)).setDisable(false);
			else
				alert.getDialogPane().lookupButton(alert.getButtonTypes().get(0)).setDisable(true);
		});
		
		pathBox.getChildren().add(field);
		
		ImageView image = new ImageView(new Image("/folderIcon.png"));
		image.setPreserveRatio(true);
		image.setFitHeight(21);
		image.setFitWidth(30);

		Button button = new Button("", image);
		button.setPadding(new Insets(2, 5, 2, 5));
		
		button.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			if (!field.getText().equals(""))
				chooser.setInitialDirectory(new File(field.getText()));
			File selected = chooser.showDialog(primaryStage);
			if (selected != null)
				field.setText(selected.getAbsolutePath());
		});
		
		pathBox.getChildren().add(button);
		HBox.setMargin(button, new Insets(0, 0, 0, 5));
		
		box.getChildren().add(pathBox);
		
		alert.getDialogPane().setContent(box);
		
		
		Optional<ButtonType> response = alert.showAndWait();
		if (response.get() == ButtonType.OK)
			Config.get().setProperty("default_folder", field.getText());
		else
			exit();
	}
	
	private Stage getConfigStage() throws IOException {
		ConfigViewController configView = new ConfigViewController();
		
		
		Stage stage = new Stage();
		stage.setScene(new Scene((Parent) configView.getView()));
		
		configView.addActionOnExit(() -> manager.reload());
		
		return stage;
	}
	
	private void savePosition(Window window) {
		List<Double> pos = new ArrayList<Double>();
		pos.add(window.getX());
		pos.add(window.getY());
		pos.add(window.getHeight());
		pos.add(window.getWidth());
		Config.get().setListProperty("window_position", pos, d -> d.toString());
	}
	
	private void setPosition(Window window) {
		List<Double> pos = Config.get().getListProperty("window_position", s -> Double.valueOf(s), null);
		if (pos == null)
			return;
		
		window.setX(pos.get(0));
		window.setY(pos.get(1));
		window.setHeight(pos.get(2));
		window.setWidth(pos.get(3));
	}
	
	public static void exit() {
		primaryStage.close();
	}
	
	
}
