package io.github.avatarhurden.lifeorganizer.diary.views;

import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

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
//		root.backgroundProperty().bind(Bindings.when(
//				BooleanBinding.booleanExpression(entry.starredProperty())).then(
//						new Background(new BackgroundFill(Color.GOLD, CornerRadii.EMPTY, Insets.EMPTY))).otherwise(
//								new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY))));
		
		textLabel.textProperty().bind(entry.entryTextProperty());
	
		tagLabel.setText(String.join(", ", entry.getTags()));
	
		dayLabel.textFillProperty().bind(Bindings.when(
				BooleanBinding.booleanExpression(entry.starredProperty())).then(Color.GOLD).otherwise(Color.BLACK));
		dayLabel.textProperty().bind(Bindings.createStringBinding(() -> 
			entry.getCreationDate().toString("dd"), entry.creationDateProperty()));

//		monthYearLabel.textFillProperty().bind(Bindings.when(
//				BooleanBinding.booleanExpression(entry.starredProperty())).then(Color.GOLD.darker()).otherwise(Color.BLACK));
		monthYearLabel.textProperty().bind(Bindings.createStringBinding(() ->
			entry.getCreationDate().toString("MMM YYYY"), entry.creationDateProperty()));
		
		imageView.setImage(entry.getImage());
	}

	public void setWidth(double width) {
		root.setPrefWidth(width);
	}
	
}
