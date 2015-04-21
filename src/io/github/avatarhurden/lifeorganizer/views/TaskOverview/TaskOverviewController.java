package io.github.avatarhurden.lifeorganizer.views.TaskOverview;

import io.github.avatarhurden.lifeorganizer.managers.TaskManager;
import io.github.avatarhurden.lifeorganizer.objects.Context;
import io.github.avatarhurden.lifeorganizer.objects.Project;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.views.CustomizableTableView;
import io.github.avatarhurden.lifeorganizer.views.SingleTaskView.SingleTaskViewController;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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
	
	private SingleTaskViewController controller;
	
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
		
		table.<Task.State>addColumn("State", t -> t.StateProperty());
		table.<DateTime>addColumn("Completion Date", t -> t.CompletionDateProperty(), null,  col -> new DueDateCell());
		table.<Character>addColumn("Priority", t -> t.PriorityProperty(), (c1, c2) -> c1 == null ? c2 == null ? 0 : 1 : c2 == null ? -1 : c1.compareTo(c2));
		table.<DateTime>addColumn("Due Date", t -> t.DueDateProperty(), 
				(c1, c2) -> c1 == null ? c2 == null ? 0 : 1 : c2 == null ? -1 : c1.compareTo(c2), col -> new DueDateCell());
		table.<DateTime>addColumn("Creation Date", t -> t.CreationDateProperty(), null, col -> new DueDateCell());
		table.<String>addColumn("Name", t -> t.NameProperty());
		table.<ObservableList<Context>>addColumn("Contexts", t -> t.ContextsProperty(), null, (event) -> {
			return new TableCell<Task, ObservableList<Context>>() {
				protected void updateItem(ObservableList<Context> date, boolean empty) {
					super.updateItem(date, empty);
					
					if (date == null || empty) {
						setText(null);
						setStyle("");
					} else {
						List<String> strings = new ArrayList<String>();
						for (Context p : date)
							strings.add(p.getName());
			            	
						setText(String.join(",", strings));
					}
				}
			};
		});
		
		table.<ObservableList<Project>>addColumn("Projects", t -> t.ProjectsProperty(), null, (event) -> {
			return new TableCell<Task, ObservableList<Project>>() {
				protected void updateItem(ObservableList<Project> date, boolean empty) {
					super.updateItem(date, empty);
					
					if (date == null || empty) {
						setText(null);
						setStyle("");
					} else {
						List<String> strings = new ArrayList<String>();
						for (Project p : date)
							strings.add(p.getName());
			            	
						setText(String.join(",", strings));
					}
				}
			};
		});
		
		table.<String>addColumn("Note", t -> t.NoteProperty(), null, (event) -> {
			return new TableCell<Task, String>() {
				protected void updateItem(String date, boolean empty) {
					super.updateItem(date, empty);
					
					if (date == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(date.split("\n")[0]);
					}
				}
			};
		});
		table.<DateTime>addColumn("Edit Date", t -> t.EditDateProperty(), null, col -> new DueDateCell());
		
		tablePane.getChildren().clear();
		tablePane.getChildren().add(table);
		AnchorPane.setBottomAnchor(table, 0d);
		AnchorPane.setTopAnchor(table, 0d);
		AnchorPane.setLeftAnchor(table, 0d);
		AnchorPane.setRightAnchor(table, 0d);
		
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				if (controller == null) {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/avatarhurden/lifeorganizer/views/SingleTaskView/SingleTaskView.fxml"));
					try {
						mainPane.setContent(loader.load());
						controller = loader.<SingleTaskViewController>getController();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				controller.setTask(newValue);
			}
		});
		
		table.setRowFactory(table -> {
			return new TableRow<Task>() {
				@Override
				protected void 	updateItem(Task t, boolean empty) {
					super.updateItem(t, empty);
					if (t == null || empty) return;
					
					if (t.getDueDate() != null && t.getDueDate().isBeforeNow())
						getStyleClass().add("highlightedRow");
					else
						getStyleClass().remove("highlightedRow");
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
		table.setItems(manager.getTodoList());
	}
	
	@FXML
	private void showDone() {
		table.setItems(manager.getDoneList());
	}

	@FXML
	private void openConfig() {
		configStage.showAndWait();
	}
	
	public void saveState(Config config) {
		config.setListProperty("column_order", table.getColumnOrder());
		config.setListProperty("column_width", table.getColumnWidth(), d -> d.toString());
		config.setListProperty("column_shown", table.getColumnShown(), b -> b.toString());
		config.setListProperty("column_sort", table.getColumnSortOrder());
		
		config.setListProperty("view_split", String.valueOf(splitPane.getDividerPositions()[0]));
	}
	
	public void loadState(Config config) {
		List<String> colOrder = config.getListProperty("column_order");
		if (colOrder != null && !colOrder.isEmpty())
			table.setColumnOrder(colOrder);
		
		List<Double> colWidth = config.getListProperty("column_width", s -> Double.valueOf(s));
		if (colWidth != null && !colWidth.isEmpty())
			table.setColumnWidth(colWidth);
		
		List<Boolean> colShown = config.getListProperty("column_shown", s -> Boolean.valueOf(s));
		if (colShown != null && !colShown.isEmpty())
			table.setColumnShown(colShown);
		
		List<String> colSort = config.getListProperty("column_sort");
		if (colSort != null && !colSort.isEmpty())
			table.setColumnSortOrder(colSort);
		
		splitPane.setDividerPosition(0, config.getProperty("view_split", s -> Double.valueOf(s), 0.5d));
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
	
	private class DueDateCell extends TableCell<Task, DateTime> {
		protected void updateItem(DateTime date, boolean empty) {
			super.updateItem(date, empty);

			if (date == null || empty) {
				setText(null);
				setStyle("");
			} else {
				setText(date.toString("YYYY.MM.dd@HH:mm"));
				if (date.isAfter(DateTime.now()))
					setStyle("-fx-text-fill: red;");
			}
		}
	}
}		
