package io.github.avatarhurden.lifeorganizer.views.TaskOverview;

import io.github.avatarhurden.lifeorganizer.managers.TaskManager;
import io.github.avatarhurden.lifeorganizer.objects.Context;
import io.github.avatarhurden.lifeorganizer.objects.DueDate;
import io.github.avatarhurden.lifeorganizer.objects.Project;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.views.ContextsTableColumn;
import io.github.avatarhurden.lifeorganizer.views.CustomizableTableView;
import io.github.avatarhurden.lifeorganizer.views.DateTimeTableColumn;
import io.github.avatarhurden.lifeorganizer.views.DueDateTableColumn;
import io.github.avatarhurden.lifeorganizer.views.NoteTableColumn;
import io.github.avatarhurden.lifeorganizer.views.PriorityTableColumn;
import io.github.avatarhurden.lifeorganizer.views.ProjectsTableColumn;
import io.github.avatarhurden.lifeorganizer.views.StateTableColumn;
import io.github.avatarhurden.lifeorganizer.views.SingleTaskView.SingleTaskViewController;

import java.util.List;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.controlsfx.control.StatusBar;
import org.joda.time.DateTime;

public class TaskOverviewController {

	CustomizableTableView<Task> table;
	
	@FXML
	private SplitPane splitPane;
	@FXML
	private AnchorPane tablePane;
	@FXML
	private TextField textField;
	@FXML
	private Button addButton;
	@FXML
	private ScrollPane mainPane;
	@FXML
	private ToggleButton todoButton, doneButton;
	private ToggleGroup shownTaskGroup;
	
	@FXML
	private AnchorPane statusBox, notificationBox;
	private StatusBar statusBar;
	
	private SingleTaskViewController taskViewController;
	private AnchorPane taskView;
	
	private TaskManager manager;
	private Stage configStage;
	
	public void setTaskManager(TaskManager manager) {
		this.manager = manager;
		
		manager.getTodoList().savedProperty().addListener((obs, oldValue, newValue) -> {
			statusBar.setText(newValue ? "Saved" : "Not Saved");
		});
		statusBar.setText(manager.getTodoList().isSaved() ? "Saved" : "Not Saved");
		showTodo();
	}

	public void setConfigStage(Stage config) {
		this.configStage = config;
	}
	
	@FXML
	private void initialize() {
		statusBar = new StatusBar();
		statusBox.getChildren().add(statusBar);
		AnchorPane.setTopAnchor(statusBar, 0d);
		AnchorPane.setBottomAnchor(statusBar, 0d);
		AnchorPane.setLeftAnchor(statusBar, 0d);
		AnchorPane.setRightAnchor(statusBar, 0d);
		
		shownTaskGroup = new ToggleGroup();
		todoButton.setToggleGroup(shownTaskGroup);
		doneButton.setToggleGroup(shownTaskGroup);
		todoButton.setSelected(true);
		
		shownTaskGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == null)
				oldValue.setSelected(true);
		});
		
		table = new CustomizableTableView<Task>();
		table.getStylesheets().add("/io/github/avatarhurden/lifeorganizer/views/style.css");
		
		table.<Task.State>addColumn("State", new StateTableColumn());
		
		table.<DateTime>addColumn("Completion Date", 
				new DateTimeTableColumn(t -> t.CompletionDateProperty()));
		
		table.<Character>addColumn("Priority", new PriorityTableColumn());
		
		table.<DueDate>addColumn("Due Date", new DueDateTableColumn());
		
		table.<DateTime>addColumn("Creation Date", 
				new DateTimeTableColumn(t -> t.CreationDateProperty()));
		
		table.<String>addColumn("Name", t -> t.NameProperty());
		
		table.<ObservableList<Context>>addColumn("Contexts", new ContextsTableColumn());
		
		table.<ObservableList<Project>>addColumn("Projects", new ProjectsTableColumn());
		
		table.<String>addColumn("Note", new NoteTableColumn());
		
		table.<DateTime>addColumn("Last Edit", new DateTimeTableColumn(t -> t.EditDateProperty()));
		
		tablePane.getChildren().clear();
		tablePane.getChildren().add(table);
		AnchorPane.setBottomAnchor(table, 0d);
		AnchorPane.setTopAnchor(table, 0d);
		AnchorPane.setLeftAnchor(table, 0d);
		AnchorPane.setRightAnchor(table, 0d);
		
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				if (taskView == null) {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/avatarhurden/lifeorganizer/views/SingleTaskView/SingleTaskView.fxml"));
					try {
						taskView = loader.load();
						mainPane.setContent(taskView);
						taskViewController = loader.<SingleTaskViewController>getController();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				FadeTransition fader = new FadeTransition(Duration.millis(350), taskView);
				fader.setCycleCount(1);
				fader.setFromValue(0.0);
				fader.setToValue(1.0);
				
				taskView.setOpacity(0);
				taskViewController.setTask(newValue);
				
				fader.play();
			}
		});
		
		table.setRowFactory(table -> {
			return new TableRow<Task>() {
				@Override
				protected void updateItem(Task t, boolean empty) {
					super.updateItem(t, empty);
					if (t == null || empty) {
//						getStyleClass().remove("table-row-highlight");
						return;
					}
					
					MenuItem item = new MenuItem("Deletar");
					item.setOnAction(event -> table.getItems().remove(table.getSelectionModel().getSelectedItem()));
					setContextMenu(new ContextMenu(item));
					
//					if (t.getDueDate() != null && t.getDueDate().isAfterNow())
//						getStyleClass().add("table-row-highlight");
//					else
//						getStyleClass().remove("table-row-highlight");
				}
			};
		});
	}
	
	@FXML
	private void click() {
		manager.addTask(new Task(textField.getText()));
		table.sort();
		textField.setText("");
	}
	
	@FXML
	private void archive() {
		manager.archiveTasks();
	}
	
	@FXML
	private void showTodo() {
		List<String> sorts = table.getColumnSortOrder();
		table.setItems(manager.getTodoList());
		table.setColumnSortOrder(sorts);
	}
	
	@FXML
	private void showDone() {
		List<String> sorts = table.getColumnSortOrder();
		table.setItems(manager.getDoneList());
		table.setColumnSortOrder(sorts);
	}

	@FXML
	private void openConfig() {
		configStage.showAndWait();
	}
	
	public void saveState() {
		Config.get().setListProperty("column_order", table.getColumnOrder());
		Config.get().setListProperty("column_width", table.getColumnWidth(), d -> d.toString());
		Config.get().setListProperty("column_shown", table.getColumnShown(), b -> b.toString());
		Config.get().setListProperty("column_sort", table.getColumnSortOrder());
		
		Config.get().setListProperty("view_split", String.valueOf(splitPane.getDividerPositions()[0]));
	}
	
	public void loadState() {
		List<String> colOrder = Config.get().getListProperty("column_order");
		if (colOrder != null && !colOrder.isEmpty())
			table.setColumnOrder(colOrder);
		
		List<Double> colWidth = Config.get().getListProperty("column_width", s -> Double.valueOf(s));
		if (colWidth != null && !colWidth.isEmpty())
			table.setColumnWidth(colWidth);
		
		List<Boolean> colShown = Config.get().getListProperty("column_shown", s -> Boolean.valueOf(s));
		if (colShown != null && !colShown.isEmpty())
			table.setColumnShown(colShown);
		
		List<String> colSort = Config.get().getListProperty("column_sort");
		if (colSort != null && !colSort.isEmpty())
			table.setColumnSortOrder(colSort);
		
		splitPane.setDividerPosition(0, Config.get().getProperty("view_split", s -> Double.valueOf(s), 0.5d));
	}
	
	@FXML
	private void keyboardShorcuts(KeyEvent event) {
		if (event.getCode().equals(KeyCode.F5))
			table.sort();
		if (event.isControlDown())
			switch (event.getCode()) {
			case D:
				table.getSelectionModel().getSelectedItem().StateProperty().setValue(Task.State.DONE);
				break;
			case F:
				table.getSelectionModel().getSelectedItem().StateProperty().setValue(Task.State.FAILED);
				break;
			case T:
				table.getSelectionModel().getSelectedItem().StateProperty().setValue(Task.State.TODO);
				break;
			case N:
				textField.requestFocus();
				break;
			default:
				break;
			}
	}
}		
