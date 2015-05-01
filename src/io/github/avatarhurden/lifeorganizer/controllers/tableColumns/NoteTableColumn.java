package io.github.avatarhurden.lifeorganizer.controllers.tableColumns;

import io.github.avatarhurden.lifeorganizer.objects.Task;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class NoteTableColumn extends TableColumn<Task, String>{
	
	public NoteTableColumn() {
		super();
		
		setCellFactory();
		setCellValueFactory();
		setComparator();
	}
	
	private void setComparator() {
		setComparator((date1, date2) -> {
			if (date1 == date2)
				return 0;
			else if (date1 == null)
				return 1;
			else if (date2 == null)
				return -1;
			else
				return date1.compareTo(date2);
		});
	}
	
	private void setCellValueFactory() {
		setCellValueFactory(col -> col.getValue().NoteProperty());
	}
	
	private void setCellFactory() {
		setCellFactory(value -> 
			new TableCell<Task, String>() {
			protected void updateItem(String note, boolean empty) {
				super.updateItem(note, empty);

				if (note == null || empty) {
					setText(null);
					setStyle("");
				} else {
					setText(note.split("\n")[0]);
				}
			}
			}
		);
	}

}
