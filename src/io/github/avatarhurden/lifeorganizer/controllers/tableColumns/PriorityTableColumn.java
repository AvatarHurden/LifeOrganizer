package io.github.avatarhurden.lifeorganizer.controllers.tableColumns;

import io.github.avatarhurden.lifeorganizer.objects.Task;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class PriorityTableColumn extends TableColumn<Task, Character>{
	
	public PriorityTableColumn() {
		super();
		
		setCellFactory();
		setCellValueFactory();
		setComparator();
	}
	
	private void setComparator() {
		setComparator((char1, char2) -> {
			if (char1 == char2)
				return 0;
			else if (char1 == null)
				return 1;
			else if (char2 == null)
				return -1;
			else
				return char1.compareTo(char2);
		});
	}
	
	private void setCellValueFactory() {
		setCellValueFactory(col -> col.getValue().PriorityProperty());
	}
	
	private void setCellFactory() {
		setCellFactory(value -> 
			new TableCell<Task, Character>() {
			protected void updateItem(Character priority, boolean empty) {
				super.updateItem(priority, empty);

				if (priority == null || empty) {
					setText(null);
					setStyle("");
				} else {
					setText(priority.toString());
					setAlignment(Pos.CENTER);
				}
			}
			}
		);
	}

}
