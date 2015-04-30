package io.github.avatarhurden.lifeorganizer.views.TaskOverview;

import io.github.avatarhurden.lifeorganizer.managers.TaskManager;
import io.github.avatarhurden.lifeorganizer.objects.Context;
import io.github.avatarhurden.lifeorganizer.objects.DueDate;
import io.github.avatarhurden.lifeorganizer.objects.Project;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.views.SingleTaskView.SingleTaskViewController;
import io.github.avatarhurden.lifeorganizer.views.TableView.ContextsTableColumn;
import io.github.avatarhurden.lifeorganizer.views.TableView.CustomizableTableView;
import io.github.avatarhurden.lifeorganizer.views.TableView.DateTimeTableColumn;
import io.github.avatarhurden.lifeorganizer.views.TableView.DueDateTableColumn;
import io.github.avatarhurden.lifeorganizer.views.TableView.NoteTableColumn;
import io.github.avatarhurden.lifeorganizer.views.TableView.PriorityTableColumn;
import io.github.avatarhurden.lifeorganizer.views.TableView.ProjectsTableColumn;
import io.github.avatarhurden.lifeorganizer.views.TableView.StateTableColumn;

import java.io.IOException;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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
	private Button archiveButton, restoreButton, configButton;
	@FXML
	private Button addButton;
	@FXML
	private ScrollPane mainPane;
	
	@FXML
	private AnchorPane statusBox;
	private StatusBar statusBar;
	
	private SingleTaskViewController taskViewController;
	
	private TaskManager manager;
	private Stage configStage;

	private FXMLLoader loader;
	
	public TaskOverviewController() {
		loader = new FXMLLoader(
				getClass().getResource("/io/github/avatarhurden/lifeorganizer/views/TaskOverview/TaskOverview.fxml"));
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Node getView() {
		return loader.getRoot();
	}
	
	@FXML
	private void initialize() {
		statusBar = new StatusBar();
		statusBox.getChildren().add(statusBar);
		AnchorPane.setTopAnchor(statusBar, 0d);
		AnchorPane.setBottomAnchor(statusBar, 0d);
		AnchorPane.setLeftAnchor(statusBar, 0d);
		AnchorPane.setRightAnchor(statusBar, 0d);
		
		archiveButton.managedProperty().bind(archiveButton.visibleProperty());
		restoreButton.managedProperty().bind(restoreButton.visibleProperty());
		
		archiveButton.setTooltip(new Tooltip("Archive completed tasks"));
		restoreButton.setTooltip(new Tooltip("Restore selected task from archive"));
		configButton.setTooltip(new Tooltip("Open settings screen"));
		
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
				FadeTransition fader = new FadeTransition(Duration.millis(350), taskViewController.getView());
				fader.setCycleCount(1);
				fader.setFromValue(0.0);
				fader.setToValue(1.0);
				
				taskViewController.setTask(newValue, manager);
				
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
					
					MenuItem delete = new MenuItem("Delete");
					delete.setOnAction(event -> table.getItems().remove(table.getSelectionModel().getSelectedItem()));
					setContextMenu(new ContextMenu(delete));
					
//					if (t.getDueDate() != null && t.getDueDate().isAfterNow())
//						getStyleClass().add("table-row-highlight");
//					else
//						getStyleClass().remove("table-row-highlight");
				}
			};
		});
		
		taskViewController = new SingleTaskViewController();
		mainPane.setContent(taskViewController.getView());
		
		Platform.runLater(() -> textField.requestFocus());
	}

	public void setTaskManager(TaskManager manager) {
		this.manager = manager;
		
		showTodo();
		table.getSelectionModel().select(0);
	}

	public void setConfigStage(Stage config) {
		this.configStage = config;
	}
	
	@FXML
	private void click() {
		Task t = manager.addTask(textField.getText(), true);
		table.sort();
		textField.setText("");
		table.getSelectionModel().select(t);
	}
	
	@FXML
	private void archive() {
		manager.archive();
		showTodo();
	}
	
//	@FXML
//	private void restore() {
//		manager.restore(table.getSelectionModel().getSelectedItem());
//	}
	
	@FXML
	private void showTodo() {
		List<String> sorts = table.getColumnSortOrder();
		
		SortedList<Task> list = (SortedList<Task>) manager.getTodoList();
		list.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(list);
		
		table.setColumnSortOrder(sorts);
	
		textField.setDisable(false);
		addButton.setDisable(false);
		taskViewController.getView().setDisable(false);
		
		archiveButton.setVisible(true);
		restoreButton.setVisible(false);
	}
	
	@FXML
	private void showDone() {
		List<String> sorts = table.getColumnSortOrder();
		table.setItems(manager.getArchivedList());
		table.setColumnSortOrder(sorts);
		
		textField.setDisable(true);
		addButton.setDisable(true);
		taskViewController.getView().setDisable(true);
		
		archiveButton.setVisible(false);
		restoreButton.setVisible(true);
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
			case A:
				archive();
				break;
			default:
				break;
			}
	}
}		
