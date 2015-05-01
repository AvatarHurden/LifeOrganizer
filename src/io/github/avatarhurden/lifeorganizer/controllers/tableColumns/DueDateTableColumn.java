package io.github.avatarhurden.lifeorganizer.controllers.tableColumns;

import io.github.avatarhurden.lifeorganizer.objects.DueDate;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;

public class DueDateTableColumn extends TableColumn<Task, DueDate>{
	
	public DueDateTableColumn() {
		super();
		
		setCellFactory();
		setCellValueFactory(col -> col.getValue().DueDateProperty());
		setComparator();
	}
	
	private void setComparator() {
		setComparator((dueDate1, dueDate2) -> {
			if (dueDate1 == dueDate2)
				return 0;
			else if (dueDate1 == null || dueDate1.getDateTime() == null)
				return 1;
			else if (dueDate2 == null || dueDate2.getDateTime() == null)
				return -1;
			else
				return dueDate1.getDateTime().compareTo(dueDate2.getDateTime());
		});
	}
	
	private void setCellFactory() {
		setCellFactory(value -> 
			new TableCell<Task, DueDate>() {
			protected void updateItem(DueDate date, boolean empty) {
				super.updateItem(date, empty);

				if (empty || date == null || date.getDateTime() == null) {
					setText(null);
					setStyle("");
				} else {
					setText(date.getPrettyTime());
					setTooltip(new Tooltip(date.getDateTime().toString("dd/MM/YYYY")));
//					setAlignment(Pos.CENTER);
				}
			}
			}
		);
	}

}
