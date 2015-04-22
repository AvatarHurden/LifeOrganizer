package io.github.avatarhurden.lifeorganizer.views;

import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.objects.Task.State;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class StateTableColumn extends TableColumn<Task, State>{
	
	public StateTableColumn() {
		super();
		
		setCellFactory();
		setCellValueFactory();
		setComparator();
	}
	
	private void setComparator() {
		setComparator((state1, state2) -> {
			if (state1 == state2)
				return 0;
			else if (state1.equals(State.TODO))
				return -1;
			else if (state1.equals(State.FAILED))
				return 1;
			else if (state2.equals(State.TODO))
				return 1;
			else if (state2.equals(State.FAILED))
				return -1;
			else
				return 0;
		});
	}
	
	private void setCellValueFactory() {
		setCellValueFactory(col -> col.getValue().StateProperty());
	}
	
	private void setCellFactory() {
		setCellFactory(value -> 
			new TableCell<Task, State>() {
			protected void updateItem(State state, boolean empty) {
				super.updateItem(state, empty);

				if (state == null || empty) {
					setText(null);
					setStyle("");
					setGraphic(null);
				} else {
					CheckBox box = new CheckBox();
					box.setFocusTraversable(false);
					box.setAllowIndeterminate(true);
					
					box.setIndeterminate(state.equals(Task.State.FAILED));
					if (!box.isIndeterminate())
						box.setSelected(state.equals(Task.State.DONE));
					
					box.indeterminateProperty().addListener((obs, oldValue, newValue) -> {
						if (newValue)
							((Task) getTableRow().getItem()).StateProperty().setValue(State.FAILED);
					});
					box.selectedProperty().addListener((obs, oldValue, newValue) ->
						((Task) getTableRow().getItem()).StateProperty().setValue(newValue ? State.DONE : State.TODO));
					
					setGraphic(box);
					setAlignment(Pos.CENTER);
				}
			}
			}
		);
	}

}
