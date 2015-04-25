package io.github.avatarhurden.lifeorganizer.views.TableView;

import io.github.avatarhurden.lifeorganizer.objects.Project;
import io.github.avatarhurden.lifeorganizer.objects.Task;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class ProjectsTableColumn extends TableColumn<Task, ObservableList<Project>>{
	
	public ProjectsTableColumn() {
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
		setCellValueFactory(col -> col.getValue().ProjectsProperty());
	}
	
	private void setCellFactory() {
		setCellFactory(value -> 
			new TableCell<Task, ObservableList<Project>>() {
			protected void updateItem(ObservableList<Project> projects, boolean empty) {
				super.updateItem(projects, empty);

				if (projects == null || empty) {
					setText(null);
					setStyle("");
				} else {
					List<String> strings = new ArrayList<String>();
					for (Project p : projects)
						strings.add(p.getName());
		            	
					setText(String.join(", ", strings));
				}
			}
			}
		);
	}

}
