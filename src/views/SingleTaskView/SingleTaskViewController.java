package views.SingleTaskView;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import objects.Context;
import objects.Project;
import objects.Task;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

import views.ObjectListView;
import views.DueDateView.DueDateViewController;


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
	
	@SuppressWarnings("unchecked")
	@FXML
	private void initialize() {
		
		editDateListener = (obs, oldValue, newValue) -> {
			lastEditLabel.setText(new PrettyTime().format(newValue.toDate()));
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
		
		projectsView = new ObjectListView<Project>(s -> s.length() > 0 ? new Project(s) : null,
				p -> p.NameProperty());
		projectsView.setPromptText("Add Project");
		
		projectsBox.getChildren().add(projectsView);
		
		contextsView = new ObjectListView<Context>(s -> s.length() > 0 ? new Context(s) : null,
				p -> p.NameProperty());
		contextsView.setPromptText("Add Context");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DueDateView/DueDateView.fxml"));
		try {
			dueDatePane.getChildren().add(loader.load());
			dueDateController = loader.<DueDateViewController>getController();
		} catch (Exception e) {
			e.printStackTrace();
		}

		contextsBox.getChildren().add(contextsView);
	}

	public void setTask(Task task) {
		priorityBox.setUserData(task.PriorityProperty());
		priorityBox.setValue(task.getPriority());
		
		dueDateController.setTimeProperty(task.DueDateProperty());
		
		if (this.task != null)
			noteArea.textProperty().unbindBidirectional(this.task.NoteProperty());
		noteArea.textProperty().bindBidirectional(task.NoteProperty());
		
		if (this.task != null)
			this.task.EditDateProperty().removeListener(editDateListener);
		task.EditDateProperty().addListener(editDateListener);
		lastEditLabel.setText(new PrettyTime().format(task.getEditDate().toDate()));
		
		projectsView.setList(task.ProjectsProperty());
		
		contextsView.setList(task.ContextsProperty());
		
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