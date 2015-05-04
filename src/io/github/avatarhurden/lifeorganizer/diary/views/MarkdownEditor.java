package io.github.avatarhurden.lifeorganizer.diary.views;

import java.io.StringReader;
import java.io.StringWriter;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import org.tautua.markdownpapers.Markdown;
import org.tautua.markdownpapers.parser.ParseException;

public class MarkdownEditor {

	@FXML
	private TextArea text;
	@FXML
	private WebView webView;
	
	private VBox viewer, editor;

	private AnchorPane pane;
	
	public MarkdownEditor() {

		setEditPane();
		setViewerPane();
		
		text.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce sit amet nunc vel dui accumsan tincidunt. Cras non nisi nibh. Integer erat erat, fringilla at fringilla ac, suscipit eu lorem. Aenean a scelerisque velit. Nam a sapien eget metus bibendum mattis. Duis sodales dignissim feugiat. Pellentesque aliquam tellus a pharetra cursus. Sed accumsan elementum nisi et rhoncus. Praesent id ornare elit.\n\nAliquam purus metus, fermentum at tellus ac, gravida accumsan enim. Vestibulum nec justo et tortor rhoncus rutrum consectetur non ipsum. Etiam eu aliquet nisl. Sed sit amet luctus leo. Proin eget justo luctus, commodo neque sed, efficitur libero. Vestibulum feugiat gravida accumsan. Proin id auctor urna. Curabitur imperdiet malesuada lectus, vitae venenatis libero consectetur non. Pellentesque vestibulum aliquam odio, nec suscipit elit accumsan vel. Mauris rutrum odio felis, sed semper odio sagittis et. In tempor rutrum tempus. Curabitur magna lacus, rutrum ac eros eu, tempor aliquam augue.\n\nCum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Sed aliquet dapibus nulla, placerat posuere dolor. Maecenas id eleifend metus. Duis tincidunt dictum est in vehicula. Mauris nec rutrum risus. Cras luctus vestibulum dolor. Vivamus ut consequat ligula. Donec sed aliquet magna, non vestibulum sapien. Nam at fermentum arcu. Vivamus eu tortor sed odio aliquam placerat. Cras mattis eget felis ac laoreet.\n\nMorbi a ultrices diam. In sodales maximus pharetra. Fusce molestie euismod orci, eget viverra eros iaculis non. Praesent in consequat lectus, vitae maximus mauris. Integer nisi ipsum, scelerisque id magna id, tincidunt consequat nibh. Sed vehicula vitae nibh ac condimentum. Morbi placerat dui tellus, vitae pharetra dui scelerisque ac. Aliquam eget semper ligula. Suspendisse semper tellus eget nisl sollicitudin mollis. Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n\nDuis mauris risus, ullamcorper et porttitor sit amet, consectetur a libero. Quisque justo nulla, facilisis non rhoncus et, ultrices at risus. Nam interdum elit non augue auctor varius. Integer a neque consectetur, cursus elit eget, faucibus orci. Integer aliquet nisi et nisi mattis tristique. Nunc in libero vitae mi hendrerit varius ac sit amet nisi. Integer non felis id nulla tempus varius at aliquet justo. Sed non vulputate dui. Donec interdum faucibus ipsum, non pharetra risus iaculis in. Curabitur in est scelerisque, eleifend tortor vel, lacinia risus. Nulla et pellentesque odio, eget ornare felis.");

		pane = new AnchorPane();
		pane.getChildren().setAll(viewer);
		
		AnchorPane.setTopAnchor(editor, 0d);
		AnchorPane.setBottomAnchor(editor, 0d);
		AnchorPane.setLeftAnchor(editor, 0d);
		AnchorPane.setRightAnchor(editor, 0d);
		AnchorPane.setTopAnchor(viewer, 0d);
		AnchorPane.setBottomAnchor(viewer, 0d);
		AnchorPane.setLeftAnchor(viewer, 0d);
		AnchorPane.setRightAnchor(viewer, 0d);
	}
	
	public AnchorPane getView() {
		return pane;
	}
	
	public Property<String> getTextProperty() {
		return text.textProperty();
	}
	
	public void setText(String value) {
		text.setText(value);
	}
	
	public String getText() {
		return text.getText();
	}
	
	private void setEditPane() {
		editor = new VBox();
		
		text = new TextArea();
		text.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER && event.isControlDown())
				pane.getChildren().setAll(viewer);
		});
		text.setWrapText(true);
		text.textProperty().addListener(event -> updateViewer());
		
		FlowPane buttons = getEditButtons();
		
		ButtonBar bar = new ButtonBar();
		
		Button done = new Button("Done");
		done.setOnAction(event -> pane.getChildren().setAll(viewer));	
		bar.getButtons().add(done);
		
		text.prefHeightProperty().bind(editor.heightProperty().subtract(buttons.heightProperty()).subtract(bar.heightProperty()));
		
		editor.getChildren().add(buttons);
		editor.getChildren().add(text);
		editor.getChildren().add(bar);
	}

	private FlowPane getEditButtons() {
		FlowPane flowPane = new FlowPane(0, 2);
		flowPane.setSnapToPixel(true);
		
		Button italic = new Button("I");
		italic.setPrefHeight(30);
		italic.setFont(Font.font("Verdana", FontPosture.ITALIC, 14));
		italic.setOnAction(event -> addItalics());

		Button bold = new Button("B");
		bold.setPrefHeight(30);
		bold.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
		bold.setOnAction(event -> addBold());	

		Button code = new Button("</>");
		code.setPrefHeight(30);
		code.setFont(Font.font("Verdana", FontWeight.NORMAL, 10));
		code.setOnAction(event -> addCode());	

		Button header = new Button("T");
		header.setPrefHeight(30);
		header.setFont(Font.font("Times new Roman", FontWeight.BOLD, 14));
		header.setOnAction(event -> addHeader());	

		Button list = new Button();
		list.setPrefHeight(30);
		list.setGraphic(new ImageView(new Image("/list.png")));
		list.setOnAction(event -> addList());	

		flowPane.getChildren().addAll(italic, bold, code, header, list);
		
		return flowPane;
	}
	
	private void setViewerPane() {
		webView = new WebView();
		webView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2)
				pane.getChildren().setAll(editor);
		});
		
		ButtonBar buttonBar = new ButtonBar();
		
		Button edit = new Button("Edit");
		edit.setOnAction(event -> {
			pane.getChildren().setAll(editor);
		});
		buttonBar.getButtons().add(edit);

		
		viewer = new VBox(webView, buttonBar);
	}
	
	@FXML
	private void updateViewer() {
        try {
            StringWriter writer = new StringWriter();
			new Markdown().transform(new StringReader(text.getText()), writer);
	        webView.getEngine().loadContent(writer.toString());
	        webView.setBlendMode(BlendMode.DARKEN);
	        
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void addHeader() {

		String[] lines = text.getText().split("\n");
		int pos = 0;
		for (int i = 0; i < lines.length; i++) {
			pos += lines[i].length();
			if (pos >= text.getCaretPosition()) {
				lines[i] = "# " + lines[i];
				break;
			}
		}
		JSObject t = (JSObject) webView.getEngine().executeScript("window.getSelection()");
		System.out.println(t.toString());
		text.setText(String.join("\n", lines));
	}
	
	@FXML
	private void addList() {
		String[] lines = text.getText().split("\n");
		int pos = 0;
		for (int i = 0; i < lines.length; i++) {
			pos += lines[i].length();
			if (pos >= text.getCaretPosition()) {
				lines[i] = "- " + lines[i];
				break;
			}
		}
		JSObject t = (JSObject) webView.getEngine().executeScript("window.getSelection()");
		System.out.println(t.toString());
		text.setText(String.join("\n", lines));
	}
	
	@FXML
	private void addBold() {
		if (!text.getSelectedText().equals("")) {
			int caret = text.getCaretPosition();
			System.out.println("text = \"" + text.getText(caret, caret + 1) + "\"");
			while (text.getText(caret-1, caret).matches("[ \\n\\t]"))
				caret--;
			text.insertText(text.getAnchor(), "**");
			text.insertText(caret + 2, "**");
		} else {
			text.insertText(text.getCaretPosition() , "****");
			text.positionCaret(text.getCaretPosition() - 2);
		}
		text.requestFocus();
	}

	@FXML
	private void addItalics() {
		if (!text.getSelectedText().equals("")) {
			int caret = text.getCaretPosition();
			System.out.println("text = \"" + text.getText(caret, caret + 1) + "\"");
			while (text.getText(caret-1, caret).matches("[ \\n\\t]"))
				caret--;
			text.insertText(text.getAnchor(), "*");
			text.insertText(caret + 1, "*");
		} else {
			text.insertText(text.getCaretPosition() , "**");
			text.positionCaret(text.getCaretPosition() - 1);
		}
		text.requestFocus();
	}
	
	@FXML
	private void addCode() {
		if (!text.getSelectedText().equals("")) {
			int caret = text.getCaretPosition();
			System.out.println("text = \"" + text.getText(caret, caret + 1) + "\"");
			while (text.getText(caret-1, caret).matches("[ \\n\\t]"))
				caret--;
			text.insertText(text.getAnchor(), "`");
			text.insertText(caret + 1, "`");
		} else {
			text.insertText(text.getCaretPosition() , "``");
			text.positionCaret(text.getCaretPosition() - 1);
		}
		text.requestFocus();
	}
}
