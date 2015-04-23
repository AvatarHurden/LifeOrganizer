package io.github.avatarhurden.lifeorganizer.views.DueDateView;

import io.github.avatarhurden.lifeorganizer.objects.DueDate;

import java.time.LocalDate;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;

import org.joda.time.MutableDateTime;


public class DueDateViewController {

	@FXML
	private DatePicker datePicker;
	@FXML
	private Label timeLabel;
	@FXML
	private Slider hourSlider, minuteSlider;
	
	private Property<DueDate> timeProperty;
	
	@FXML
	private void initialize() {
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
			
			setTimeEnabled(newValue != null);
			
			if (newValue == null)
				timeProperty.getValue().setDateTime(null);
			else {
				MutableDateTime t = new MutableDateTime(newValue.toString());
				
				t.setHourOfDay(timeProperty.getValue() == null ? 
						0 : timeProperty.getValue().getDateTime().getHourOfDay());
				t.setMinuteOfHour(timeProperty.getValue() == null ? 
						0 : timeProperty.getValue().getDateTime().getMinuteOfHour());
					
				timeProperty.getValue().setDateTime(t.toDateTime());
			}
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
				timeProperty.getValue().setDateTime(
						timeProperty.getValue().getDateTime().hourOfDay().setCopy(newValue.intValue()));
		});
	
		minuteSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (timeProperty != null && timeProperty.getValue() != null)
				timeProperty.getValue().setDateTime(
						timeProperty.getValue().getDateTime().minuteOfHour().setCopy(newValue.intValue()));
		});
		
		// Changes the text on the label to show the selected time
		hourSlider.valueProperty().addListener((event) -> 
			timeLabel.setText(String.format("%02d:%02d", hourSlider.valueProperty().intValue(), minuteSlider.valueProperty().intValue())));
		minuteSlider.valueProperty().addListener((event) -> 
			timeLabel.setText(String.format("%02d:%02d", hourSlider.valueProperty().intValue(), minuteSlider.valueProperty().intValue())));
		
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
			setTimeEnabled(timeProperty.getValue().getHasTime());
		
		datePicker.setValue(LocalDate.parse(timeProperty.getValue().getDateTime().toString("YYYY-MM-dd"))); // LocalDate parse only accepts this format
		hourSlider.setValue(timeProperty.getValue().getDateTime().getHourOfDay());
		minuteSlider.setValue(timeProperty.getValue().getDateTime().getMinuteOfHour());
		this.timeProperty = timeProperty;
	}
	
	private void setTimeEnabled(boolean enabled) {
		hourSlider.setDisable(!enabled);
		minuteSlider.setDisable(!enabled);
		timeLabel.setText(enabled ? "00:00" : "");
	}
	
}
