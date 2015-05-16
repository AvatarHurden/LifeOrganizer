package io.github.avatarhurden.lifeorganizer.diary.views;

import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;


import java.util.Calendar;
import java.util.Locale;


import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.StringConverter;
import jfxtras.scene.control.CalendarPicker;


import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.joda.time.DateTime;

public class EntryViewController {

	@FXML
	private Label dayOfWeekLabel, dayOfMonthLabel, monthYearLabel, timeLabel;
	
	@FXML
	private BorderPane datePane;
	
	@FXML
	private StackPane editorPane;
	private MarkdownEditor editor;
	@FXML
	private SVGPath favoriteIcon, tagIcon, deleteLidIcon, deleteBodyIcon;
	@FXML
	private VBox deleteIcon;
	
	private DayOneEntry entry;
	
	@FXML
	private void initialize() {
		editor = new MarkdownEditor();
		editorPane.getChildren().setAll(editor);
		editor.prefHeightProperty().bind(editorPane.prefHeightProperty());
		editor.prefWidthProperty().bind(editorPane.prefWidthProperty());
		
		tagIcon.fillProperty().bind(Bindings.when(BooleanBinding.booleanExpression(tagIcon.hoverProperty())).then(Color.ALICEBLUE.saturate()).otherwise(Color.TRANSPARENT));

		deleteLidIcon.fillProperty().bind(Bindings.when(deleteIcon.hoverProperty()).then(Color.RED).otherwise(Color.BLACK));
		deleteLidIcon.rotateProperty().bind(Bindings.when(deleteIcon.hoverProperty()).then(40.6).otherwise(0));
		deleteBodyIcon.fillProperty().bind(Bindings.when(deleteIcon.hoverProperty()).then(Color.RED).otherwise(Color.BLACK));
		deleteIcon.hoverProperty().addListener((obs, oldValue, newValue) -> {
			VBox.setMargin((Node) deleteLidIcon, newValue ? new Insets(4, 0, 0, 12) : new Insets(4, 0, 0, 0));
		});
	}
	
	public void setEntry(DayOneEntry newEntry) {
		dayOfWeekLabel.textProperty().bind(Bindings.createStringBinding(() -> 
			newEntry.getCreationDate().toString("EEE"), newEntry.creationDateProperty()));
		dayOfMonthLabel.textProperty().bind(Bindings.createStringBinding(() -> 
			newEntry.getCreationDate().toString("dd"), newEntry.creationDateProperty()));
		
		monthYearLabel.textProperty().bind(Bindings.createStringBinding(() -> 
			newEntry.getCreationDate().toString("MMM \n YYYY"), newEntry.creationDateProperty()));
		
		timeLabel.textProperty().bind(Bindings.createStringBinding(() -> 
			newEntry.getCreationDate().toString("HH:mm"), newEntry.creationDateProperty()));
		
		if (entry != null)
			editor.textProperty().unbindBidirectional(entry.entryTextProperty());
		editor.textProperty().bindBidirectional(newEntry.entryTextProperty());
		editor.setText(newEntry.getEntryText());
		
		favoriteIcon.fillProperty().bind(Bindings.when(
				BooleanBinding.booleanExpression(newEntry.starredProperty())).then(Color.GOLD).otherwise(
						Bindings.when(BooleanBinding.booleanExpression(favoriteIcon.hoverProperty())).then(Color.GOLD.deriveColor(0, 1, 1, 0.3)).otherwise(Color.TRANSPARENT)));
		favoriteIcon.setOnMouseClicked(event -> {
			newEntry.starredProperty().setValue(!newEntry.starredProperty().getValue());
		});
		
		datePane.setOnMouseClicked(event -> {
			PopOver over = new PopOver();
			over.setDetachable(false);
			over.setAutoHide(true);
			over.setArrowLocation(ArrowLocation.TOP_CENTER);
			
			CalendarPicker picker = new CalendarPicker();
			picker.setShowTime(true);
			picker.setAllowNull(false);

			picker.setCalendar(newEntry.getCreationDate().toGregorianCalendar());
			SimpleStringProperty converter = new SimpleStringProperty();
			converter.bindBidirectional(newEntry.creationDateProperty(), new StringConverter<DateTime>() {
				public DateTime fromString(String string) {
					return new DateTime(string);
				}
				public String toString(DateTime object) {
					return object.toString();
				}
			});
			converter.bindBidirectional(picker.calendarProperty(), new StringConverter<Calendar>() {
				public Calendar fromString(String string) {
					return new DateTime(string).toCalendar(Locale.getDefault());
				}
				public String toString(Calendar object) {
					return new DateTime(object).toString();
				}
			});
			
			over.setContentNode(picker);
			over.show(datePane);
		});
		
		deleteIcon.setOnMouseClicked(event -> newEntry.delete());
		
		entry = newEntry;
	}
	
}
