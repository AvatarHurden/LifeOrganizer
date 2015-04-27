package io.github.avatarhurden.lifeorganizer.diary.views;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;

public class DiaryOverviewController {

	@FXML
	private ListView<DayOneEntry> entryList;
	@FXML
	private AnchorPane contentPane;
	
}
