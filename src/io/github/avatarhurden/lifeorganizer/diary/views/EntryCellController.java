package io.github.avatarhurden.lifeorganizer.diary.views;

import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class EntryCellController {

	@FXML
	private HBox root;
	@FXML
	private Label textLabel, tagLabel, dayLabel, monthYearLabel;
	@FXML
	private ImageView imageView;
	
	@FXML
	private void initialize() {
		imageView.visibleProperty().bind(imageView.imageProperty().isNotNull());
		imageView.managedProperty().bind(imageView.visibleProperty());
	}
	
	public void setContent(DayOneEntry entry) {
		textLabel.setText(entry.getEntryText());
	
		tagLabel.setText(String.join(", ", entry.getTags()));
		
		dayLabel.setText(""+entry.getCreationDate().getDayOfMonth());
		monthYearLabel.setText("" + entry.getCreationDate().getMonthOfYear() + "/" + entry.getCreationDate().getYear());
		
		imageView.setImage(entry.getImage());
	}

	public void setWidth(double width) {
		root.setPrefWidth(width);
	}
	
}
