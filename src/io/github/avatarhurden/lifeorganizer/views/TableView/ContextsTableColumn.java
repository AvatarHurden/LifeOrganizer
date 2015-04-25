package io.github.avatarhurden.lifeorganizer.views.TableView;

import io.github.avatarhurden.lifeorganizer.objects.Context;
import io.github.avatarhurden.lifeorganizer.objects.Task;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class ContextsTableColumn extends TableColumn<Task, ObservableList<Context>>{
	
	public ContextsTableColumn() {
		super();
		
		setCellFactory();
		setCellValueFactory();
		setComparator();
	}
	
	private void setComparator() {
		setComparator((projects1, projects2) -> {
			if (projects1 == projects2)
				return 0;
			else if (projects1 == null)
				return 1;
			else if (projects2 == null)
				return -1;
			else if (projects1.size() != projects2.size())
				return Integer.compare(projects1.size(), projects2.size());
			else
				for (int i = 0; i < projects1.size(); i++)
					if (!projects1.get(i).getName().equals(projects2.get(i).getName()))
						return projects1.get(i).getName().compareTo(projects2.get(i).getName());
				return 0;
		});
	}
	
	private void setCellValueFactory() {
		setCellValueFactory(col -> col.getValue().ContextsProperty());
	}
	
	private void setCellFactory() {
		setCellFactory(value -> 
			new TableCell<Task, ObservableList<Context>>() {
			protected void updateItem(ObservableList<Context> contexts, boolean empty) {
				super.updateItem(contexts, empty);

				if (contexts == null || empty) {
					setText(null);
					setStyle("");
				} else {
					List<String> strings = new ArrayList<String>();
					for (Context p : contexts)
						strings.add(p.getName());
		            	
					setText(String.join(", ", strings));
				}
			}
			}
		);
	}

}
