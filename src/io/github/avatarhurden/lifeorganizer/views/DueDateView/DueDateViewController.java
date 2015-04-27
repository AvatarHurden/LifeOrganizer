package io.github.avatarhurden.lifeorganizer.views.DueDateView;

import io.github.avatarhurden.lifeorganizer.objects.DueDate;

import java.io.IOException;
import java.time.LocalDate;

import javafx.animation.FadeTransition;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.joda.time.MutableDateTime;


public class DueDateViewController {

	@FXML
	private DatePicker datePicker;
	@FXML
	private Label timeLabel;
	@FXML
	private Slider hourSlider, minuteSlider;	
	
	@FXML
	private HBox timeBox, closeHBox, dueDateBox;
	@FXML
	private StackPane buttonPane;
	@FXML
	private Button closeButton;
	
	private Property<DueDate> timeProperty;
	private boolean timeEnabled;

	private FXMLLoader loader;
	
	public DueDateViewController() {
		loader = new FXMLLoader(
				getClass().getResource("/io/github/avatarhurden/lifeorganizer/views/DueDateView/DueDateView.fxml"));
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
		
		timeBox.managedProperty().bind(timeBox.visibleProperty());
		buttonPane.managedProperty().bind(buttonPane.visibleProperty());
		
		// Allows to cycle through days with the keyboard
		datePicker.getEditor().setOnKeyPressed((event) -> {
			if (datePicker.getEditor().getText().equals("") && (event.getCode().equals(KeyCode.UP) || event.getCode().equals(KeyCode.DOWN)))
				datePicker.setValue(LocalDate.now());
			else if (event.getCode().equals(KeyCode.UP))
				datePicker.setValue(datePicker.getValue().plusDays(1));
			else if (event.getCode().equals(KeyCode.DOWN))
				datePicker.setValue(datePicker.getValue().minusDays(1));
		});
		
		datePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (timeProperty == null)
				return;
			
			if (newValue == null)
				timeProperty.setValue(null);
			else {
				MutableDateTime t = new MutableDateTime(newValue.toString());
				
				t.setHourOfDay(timeProperty.getValue() == null ? 
						0 : timeProperty.getValue().getDateTime().getHourOfDay());
				t.setMinuteOfHour(timeProperty.getValue() == null ? 
						0 : timeProperty.getValue().getDateTime().getMinuteOfHour());
					
				timeProperty.setValue(new DueDate(t.toDateTime(), timeBox.isVisible()));
			}
			
			setTimeEnabled(timeProperty.getValue().getHasTime());
			
		});
		
		hourSlider.setSnapToTicks(true);
		hourSlider.setMajorTickUnit(1);
		hourSlider.setMax(23);
		hourSlider.setBlockIncrement(1);

		minuteSlider.setSnapToTicks(true);
		minuteSlider.setMajorTickUnit(1);
		minuteSlider.setBlockIncrement(5);
		minuteSlider.setMax(59);
		
		hourSlider.setOnMouseClicked(event -> {
			if (hourSlider.isDisabled() && !timeProperty.getValue().getHasTime()) {
				setTimeEnabled(true);
				timeProperty.getValue().setHasTime(true);
			}
		});

		minuteSlider.setOnMouseClicked(event -> {
			if (minuteSlider.isDisabled() && !timeProperty.getValue().getHasTime()) {
				setTimeEnabled(true);
				timeProperty.getValue().setHasTime(true);
			}
		});
		
		hourSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (timeProperty != null && timeProperty.getValue() != null)
				timeProperty.setValue(new DueDate(
						timeProperty.getValue().getDateTime().hourOfDay().setCopy(newValue.intValue()), true));
		});
	
		minuteSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (timeProperty != null && timeProperty.getValue() != null)
				timeProperty.setValue(new DueDate(
						timeProperty.getValue().getDateTime().minuteOfHour().setCopy(newValue.intValue()), true));
		});
		
		// Changes the text on the label to show the selected time
		hourSlider.valueProperty().addListener((obs, oldValue, newValue) -> 
			timeLabel.setText(String.format("%02d", newValue.intValue()) + timeLabel.getText().substring(2)));
		minuteSlider.valueProperty().addListener((obs, oldValue, newValue) -> 
			timeLabel.setText(timeLabel.getText().substring(0, 2) + String.format(":%02d", newValue.intValue())));
		
		Region clearButton = new Region();
		clearButton.getStylesheets().add("/io/github/avatarhurden/lifeorganizer/views/style.css");
        clearButton.getStyleClass().addAll("graphic");
        
        StackPane clearButtonPane = new StackPane(clearButton);
        clearButtonPane.setFocusTraversable(true);
        
        clearButtonPane.getStyleClass().addAll("clear-button");
        clearButtonPane.setOpacity(1.0);
        clearButtonPane.setOnMouseReleased(e -> setTimeEnabled(false));
        clearButtonPane.setOnKeyPressed(event -> {
        	if (event.getCode().equals(KeyCode.ENTER))
        		setTimeEnabled(false);
        });
        
        closeHBox.getChildren().add(0, clearButtonPane);
        
		setTimeEnabled(true); // Just to set the layout correctly
	}
	
	public void setTimeProperty(Property<DueDate> timeProperty) {
		this.timeProperty = null; // Allows to edit the nodes without editing the time property
		if (timeProperty.getValue() == null) {
			// If task has no due date, resets the Nodes
			datePicker.setValue(null);
			hourSlider.setValue(0);
			minuteSlider.setValue(0);
			setTimeEnabled(false);
			this.timeProperty = timeProperty;
			return;
		} else
			setTimeEnabled(timeProperty.getValue() == null ? false : timeProperty.getValue().getHasTime());
		
		datePicker.setValue(LocalDate.parse(timeProperty.getValue().getDateTime().toString("YYYY-MM-dd"))); // LocalDate parse only accepts this format
		hourSlider.setValue(timeProperty.getValue().getDateTime().getHourOfDay());
		minuteSlider.setValue(timeProperty.getValue().getDateTime().getMinuteOfHour());
		this.timeProperty = timeProperty;
	}
	
	private void setTimeEnabled(boolean enabled) {
		if (enabled == this.timeEnabled)
			return;
		
		FadeTransition timeBoxFader = new FadeTransition(Duration.millis(350), timeBox);
		timeBoxFader.setCycleCount(1);
		timeBoxFader.setFromValue(enabled ? 0.0 : 1.0);
		timeBoxFader.setToValue(enabled ? 1.0 : 0.0);
		
		FadeTransition buttonPaneFader = new FadeTransition(Duration.millis(350), buttonPane);
		buttonPaneFader.setCycleCount(1);
		buttonPaneFader.setFromValue(!enabled ? 0.0 : 1.0);
		buttonPaneFader.setToValue(!enabled ? 1.0 : 0.0);

		buttonPaneFader.play();
		timeBoxFader.play();

		timeBox.setVisible(enabled);
		buttonPane.setVisible(!enabled);
		
		if (timeProperty != null && timeProperty.getValue() != null)
			timeProperty.setValue(new DueDate(timeProperty.getValue().getDateTime(), enabled));
		
		timeLabel.setText("00:00");
		if (!enabled) {
			hourSlider.setValue(0);
			minuteSlider.setValue(0);
		}
		this.timeEnabled = enabled;
	}
	
	@FXML
	private void enableTime() {
		setTimeEnabled(true);
	}

	@FXML
	private void disableTime() {
		setTimeEnabled(false);
	}
}
