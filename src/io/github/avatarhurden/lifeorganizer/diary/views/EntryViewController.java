package io.github.avatarhurden.lifeorganizer.diary.views;

import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;
import io.github.avatarhurden.lifeorganizer.views.ObjectListView;
import io.github.avatarhurden.lifeorganizer.views.ObjectListView.ObjectLayout;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;
import jfxtras.scene.control.CalendarPicker;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.joda.time.DateTime;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;


public class EntryViewController {

	@FXML
	private Label dayOfWeekLabel, dayOfMonthLabel, monthYearLabel, timeLabel, tagsLabel;
	
	@FXML
	private BorderPane datePane;
	@FXML
	private SVGPath favoriteIcon, tagIcon, deleteLidIcon, deleteBodyIcon, photoIcon;
	@FXML
	private StackPane tagPane, photoPane;
	
	@FXML
	private ImageView imageView;
	@FXML
	private VBox deleteIcon;
	
	@FXML
	private VBox contentPane;
	@FXML
	private TextArea textArea;
	@FXML
	private WebView webView;
	@FXML
	private Button editButton, saveButton;

	private DayOneEntry entry;
	
	@FXML
	private void initialize() {
		editButton.setOnAction(event -> editButton.setVisible(false));
		saveButton.setOnAction(event -> editButton.setVisible(true));
		
		textArea.visibleProperty().bindBidirectional(saveButton.visibleProperty());
		saveButton.visibleProperty().bind(editButton.visibleProperty().not());
		webView.visibleProperty().bindBidirectional(editButton.visibleProperty());
		
		textArea.prefHeightProperty().bind(contentPane.heightProperty());
		webView.prefHeightProperty().bind(contentPane.heightProperty());
		
		textArea.autosize();
		textArea.setWrapText(true);
		PegDownProcessor processor = new PegDownProcessor(Extensions.ALL);
		textArea.textProperty().addListener((obs, oldValue, newValue) -> {
			String html = processor.markdownToHtml(newValue);
	        webView.getEngine().loadContent(html);
		});
        webView.setBlendMode(BlendMode.DARKEN);
		
        textArea.setOnKeyPressed(event -> {
        	if (event.isControlDown() && event.getCode() == KeyCode.ENTER)
        		editButton.setVisible(true);
        });
        
        webView.setOnMouseClicked(event -> {
        	if (event.getClickCount() == 2)
        		editButton.setVisible(false);
        });
        
		saveButton.managedProperty().bind(saveButton.visibleProperty());
		editButton.managedProperty().bind(editButton.visibleProperty());
		textArea.managedProperty().bind(textArea.visibleProperty());
		webView.managedProperty().bind(webView.visibleProperty());

		deleteLidIcon.fillProperty().bind(Bindings.when(deleteIcon.hoverProperty()).then(Color.RED).otherwise(Color.BLACK));
		deleteLidIcon.rotateProperty().bind(Bindings.when(deleteIcon.hoverProperty()).then(40.6).otherwise(0));
		deleteBodyIcon.fillProperty().bind(Bindings.when(deleteIcon.hoverProperty()).then(Color.RED).otherwise(Color.BLACK));
		deleteIcon.hoverProperty().addListener((obs, oldValue, newValue) -> {
			VBox.setMargin((Node) deleteLidIcon, newValue ? new Insets(4, 0, 0, 12) : new Insets(4, 12, 0, 0));
		});
		
		photoIcon.fillProperty().bind(Bindings.when(photoPane.hoverProperty()).then(Color.ALICEBLUE.saturate()).otherwise(Color.TRANSPARENT));
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
			textArea.textProperty().unbindBidirectional(entry.entryTextProperty());
//		editor.viewViewer();
		textArea.textProperty().bindBidirectional(newEntry.entryTextProperty());
		editButton.setVisible(true);
//		editor.setText(newEntry.getEntryText());
		
		if (newEntry.getTags().size() > 0)
			tagsLabel.setText(newEntry.getTags().size()+"");
		else
			tagsLabel.setText("");
		
		tagIcon.fillProperty().bind(Bindings.when(tagsLabel.textProperty().isNotEmpty()).then(Color.LIGHTCYAN).otherwise(
				Bindings.when(BooleanBinding.booleanExpression(tagIcon.hoverProperty())).then(Color.ALICEBLUE.saturate()).otherwise(Color.TRANSPARENT)));

		favoriteIcon.fillProperty().bind(Bindings.when(
				BooleanBinding.booleanExpression(newEntry.starredProperty())).then(Color.GOLD).otherwise(
						Bindings.when(BooleanBinding.booleanExpression(favoriteIcon.hoverProperty())).then(Color.GOLD.deriveColor(0, 1, 1, 0.3)).otherwise(Color.TRANSPARENT)));
		favoriteIcon.setOnMouseClicked(event -> {
			newEntry.starredProperty().setValue(!newEntry.starredProperty().getValue());
		});
		
		tagPane.setOnMouseClicked(event -> {
			PopOver over = new PopOver();
			over.setDetachable(false);
			over.setAutoHide(true);
			over.setArrowLocation(ArrowLocation.TOP_CENTER);
			
			ObjectListView<String> tagView = new ObjectListView<String>(s -> new SimpleStringProperty(s), true, ObjectLayout.VERTICAL);
			tagView.setList(newEntry.getObservableTags());
			tagView.setCreationPolicy(s -> newEntry.addTag(s) ? s : null);
			tagView.setDeletionPolicy(s -> newEntry.removeTag(s));
			
			tagView.setSuggestions(newEntry.getManager().getTags(), t -> t.getName());
			
			StackPane pane = new StackPane(tagView);
			pane.setPadding(new Insets(5));
			
			over.setContentNode(pane);
			over.show(tagPane);
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
			
			StackPane pane = new StackPane(picker);
			pane.setPadding(new Insets(5));
			over.setContentNode(pane);
			over.show(datePane);
		});
		
		deleteIcon.setOnMouseClicked(event -> {
			PopOver over = new PopOver();
			over.setDetachable(false);
			over.setAutoHide(true);
			over.setArrowLocation(ArrowLocation.TOP_CENTER);
			
			Button delete = new Button("Delete Entry");
			delete.setTextFill(Color.RED);
			delete.setOnAction(event2 -> newEntry.delete());
			
			VBox box = new VBox(5);
			box.setAlignment(Pos.CENTER);
			box.setPadding(new Insets(5));
			box.getChildren().addAll(delete);
			
			over.setContentNode(box);
			over.show(deleteIcon);
		});
		
		photoIcon.setOnMouseClicked(event -> {
			PopOver over = new PopOver();
			over.setDetachable(false);
			over.setAutoHide(true);
			over.setArrowLocation(ArrowLocation.TOP_CENTER);
			
			Button select = new Button("Select Image");
			select.setOnAction(event2 -> {
				FileChooser chooser = new FileChooser();
				chooser.setInitialDirectory(new File(System.getProperty("user.home"), "Pictures"));
				chooser.getExtensionFilters().add(new ExtensionFilter("JPEG Images", "*.jpg"));
				File selected = chooser.showOpenDialog(null);
				if (selected != null )
					newEntry.setNewImage(selected);
			});
			
			Button remove = new Button("Remove Image");
			remove.setTextFill(Color.RED);
			remove.disableProperty().bind(Bindings.createBooleanBinding(() -> newEntry.getImage() == null, newEntry.imageProperty()));
			remove.setOnAction(event2 -> {
				newEntry.removeImage();
			});

			VBox box = new VBox(5);
			box.setAlignment(Pos.CENTER);
			box.setPadding(new Insets(5));
			box.getChildren().addAll(select, remove);
			
			over.setContentNode(box);
			over.show(photoIcon);
		});
		
		imageView.imageProperty().bind(newEntry.imageProperty());
		if (imageView.getImage() == null) {
			imageView.fitHeightProperty().unbind();
			imageView.fitWidthProperty().unbind();
			imageView.setFitHeight(0);
			imageView.setFitWidth(0);
		} else {
			imageView.fitHeightProperty().bind(Bindings.min(400, imageView.getImage().heightProperty()));
			imageView.fitWidthProperty().bind(Bindings.min(imageView.getScene().getWidth(), imageView.getImage().widthProperty()));
		}
		
		entry = newEntry;
	}
	
}
