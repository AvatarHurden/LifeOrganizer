package io.github.avatarhurden.lifeorganizer.views.SingleTaskView;

import io.github.avatarhurden.lifeorganizer.managers.TaskManager;
import io.github.avatarhurden.lifeorganizer.objects.Context;
import io.github.avatarhurden.lifeorganizer.objects.Project;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.views.ObjectListView;
import io.github.avatarhurden.lifeorganizer.views.DueDateView.DueDateViewController;

import java.io.IOException;
import java.util.Locale;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;


public class SingleTaskViewController {

	private Task task;
	
	@FXML
	private ToggleButton todoButton, doneButton, failedButton;
	private ToggleGroup stateButtons;
	private ChangeListener<Task.State> stateListener;
	
	@FXML
	private TextField nameTextField;
	@FXML
	private ComboBox<Character> priorityBox;

	@FXML
	private HBox projectsBox;
	private ObjectListView<Project> projectsView;
	
	@FXML
	private HBox contextsBox;
	private ObjectListView<Context> contextsView;
	
	@FXML
	private Label lastEditLabel;
	private ChangeListener<DateTime> editDateListener;
	
	@FXML
	private AnchorPane dueDatePane;
	private DueDateViewController dueDateController;
	
	@FXML
	private TextArea noteArea;
	
	private FXMLLoader loader;
	
	public SingleTaskViewController() {
		loader = new FXMLLoader(
				getClass().getResource("/io/github/avatarhurden/lifeorganizer/views/SingleTaskView/SingleTaskView.fxml"));
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException e) {}
	}
	
	public Node getView() {	
		return loader.getRoot();
	}
	
	@SuppressWarnings("unchecked")
	@FXML
	private void initialize() {
		noteArea.setWrapText(true);
		
		editDateListener = (obs, oldValue, newValue) -> {
			lastEditLabel.setText(new PrettyTime(Locale.US).format(newValue.toDate()));
		};
		
		stateListener = (obs, oldValue, newValue) -> {
			switch (newValue) {
			case TODO:	
				stateButtons.selectToggle(todoButton);
				break;
			case DONE:
				stateButtons.selectToggle(doneButton);
				break;
			case FAILED:
				stateButtons.selectToggle(failedButton);
				break;
			}
		};
		
		stateButtons = new ToggleGroup();
		todoButton.setToggleGroup(stateButtons);
		todoButton.setUserData(Task.State.TODO);
		
		doneButton.setToggleGroup(stateButtons);
		doneButton.setUserData(Task.State.DONE);
		
		failedButton.setToggleGroup(stateButtons);
		failedButton.setUserData(Task.State.FAILED);
		
		stateButtons.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null)
				((Property<Task.State>) stateButtons.getUserData()).setValue((Task.State) newValue.getUserData());
			else
				// Does not allow to have no selected button
				oldValue.setSelected(true);
		});
	
		priorityBox.getItems().add(' ');
		for (char c = 'A'; c <= 'Z'; c++)
			priorityBox.getItems().add(c);
		
		priorityBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null)
					((Property<Character>) priorityBox.getUserData()).setValue(newValue == ' ' ? null : newValue);
		});
		priorityBox.setOnKeyTyped((event) -> {
			if (Character.isAlphabetic(event.getCharacter().charAt(0)))
				priorityBox.setValue(event.getCharacter().toUpperCase().charAt(0));
		});
		
		projectsView = new ObjectListView<Project>(p -> p.NameProperty());
		projectsView.setPromptText("Add Project");
		
		projectsBox.getChildren().add(projectsView);
		
		contextsView = new ObjectListView<Context>(p -> p.NameProperty());
		contextsView.setPromptText("Add Context");
		
		contextsBox.getChildren().add(contextsView);
		
		dueDateController = new DueDateViewController();
		dueDatePane.getChildren().add(dueDateController.getView());
	}

	public void setTask(Task task, TaskManager manager) {
		priorityBox.setUserData(task.PriorityProperty());
		priorityBox.setValue(task.getPriority());
		
		dueDateController.setTimeProperty(task.DueDateProperty());
		
		if (this.task != null)
			noteArea.textProperty().unbindBidirectional(this.task.NoteProperty());
		noteArea.textProperty().bindBidirectional(task.NoteProperty());
		
		if (this.task != null)
			this.task.EditDateProperty().removeListener(editDateListener);
		task.EditDateProperty().addListener(editDateListener);
		lastEditLabel.setText(new PrettyTime(Locale.US).format(task.getEditDate().toDate()));
		
		for (Project p : manager.getProjectManager().getProjects())
			System.out.println(p.getName() + " " + p.getActiveTasks() + " " + p.getInactiveTasks());

		for (Context p : manager.getContextManager().getContexts())
			System.out.println(p.getName() + " " + p.getActiveTasks() + " " + p.getInactiveTasks());
		
		projectsView.setList(task.getProjects());
		projectsView.setSuggestions(manager.getProjectManager().getActiveProjects());
		projectsView.setCreationPolicy(s -> task.addProject(s));
		projectsView.setDeletionPolicy(proj -> task.removeProject(proj));
		projectsView.clearTextField();
		
		contextsView.setList(task.getContexts());
		contextsView.setSuggestions(manager.getContextManager().getActiveContexts());
		contextsView.setCreationPolicy(s -> manager.getContextManager().createContext(s, true));
		contextsView.setDeletionPolicy(cont -> manager.getContextManager().decrementContexts(true, cont));
		contextsView.clearTextField();
		
		if (this.task != null)
			nameTextField.textProperty().unbindBidirectional(this.task.NameProperty());
		nameTextField.textProperty().bindBidirectional(task.NameProperty());
		
		if (this.task != null)
			this.task.StateProperty().removeListener(stateListener);
		task.StateProperty().addListener(stateListener);
		stateButtons.setUserData(task.StateProperty());
		
		switch (task.getState()) {
		case TODO:	
			stateButtons.selectToggle(todoButton);
			break;
		case DONE:
			stateButtons.selectToggle(doneButton);
			break;
		case FAILED:
			stateButtons.selectToggle(failedButton);
			break;
		default:
			break;
		}
		
		this.task = task;
		
	}
	
}
