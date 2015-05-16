package io.github.avatarhurden.lifeorganizer.diary.views;

import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class EntryViewController {

	@FXML
	private Label dayOfWeekLabel, dayOfMonthLabel, monthYearLabel, timeLabel;
	@FXML
	private StackPane editorPane;
	private MarkdownEditor editor;
	@FXML
	private SVGPath favoriteIcon;
	
	private DayOneEntry entry;
	
	@FXML
	private void initialize() {
		editor = new MarkdownEditor();
		editorPane.getChildren().setAll(editor);
		editor.prefHeightProperty().bind(editorPane.prefHeightProperty());
		editor.prefWidthProperty().bind(editorPane.prefWidthProperty());
		
		favoriteIcon.setCursor(Cursor.HAND);
		
	}
	
	public void setEntry(DayOneEntry newEntry) {
		dayOfWeekLabel.setText(newEntry.getCreationDate().toString("EEE"));
		dayOfMonthLabel.setText("" + newEntry.getCreationDate().getDayOfMonth());
		monthYearLabel.setText(newEntry.getCreationDate().toString("MMM") + "\n" + newEntry.getCreationDate().getYear());
		
		timeLabel.setText(newEntry.getCreationDate().toString("HH:mm"));
		
		if (entry != null)
			editor.textProperty().unbindBidirectional(entry.entryTextProperty());
		editor.textProperty().bindBidirectional(newEntry.entryTextProperty());
		editor.setText(newEntry.getEntryText());
		
		favoriteIcon.fillProperty().bind(Bindings.when(
				BooleanBinding.booleanExpression(newEntry.starredProperty())).then(Color.YELLOW).otherwise(Color.TRANSPARENT));
		favoriteIcon.setOnMouseClicked(event -> {
			newEntry.starredProperty().setValue(!newEntry.starredProperty().getValue());
		});
		
		entry = newEntry;
	}
	
}
