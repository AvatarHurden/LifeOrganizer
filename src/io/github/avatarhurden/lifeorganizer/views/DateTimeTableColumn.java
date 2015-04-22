package io.github.avatarhurden.lifeorganizer.views;

import io.github.avatarhurden.lifeorganizer.objects.Task;

import java.util.Locale;

import javafx.beans.property.Property;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

public class DateTimeTableColumn extends TableColumn<Task, DateTime>{
	
	public DateTimeTableColumn(Callback<Task, Property<DateTime>> property) {
		super();
		
		setCellFactory();
		setCellValueFactory(col -> property.call(col.getValue()));
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
	
	private void setCellFactory() {
		setCellFactory(value -> 
			new TableCell<Task, DateTime>() {
			protected void updateItem(DateTime date, boolean empty) {
				super.updateItem(date, empty);

				if (date == null || empty) {
					setText(null);
					setStyle("");
				} else {
					setText(new PrettyTime(Locale.US).format(date.toDate()));
					setTooltip(new Tooltip(date.toString(
							date.getMinuteOfDay() == 0 ? "dd/MM/YYYY" : "dd/MM/YYYY HH:mm")));
//					setAlignment(Pos.CENTER);
				}
			}
			}
		);
	}

}
