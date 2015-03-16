package objects;

import java.util.List;

import org.joda.time.DateTime;

public class Task {

	public enum State {
		TODO, DONE, FAILED;
	}
	
	private State state;
	private String name;
	
	private List<String> projects;
	private List<String> contexts;
	
	private String note;
	
	private DateTime dueDate;
	
	private DateTime creationDate;
	private DateTime editDate;
	private DateTime completionDate;
	
	public Task() {
		
		setCreationDate(new DateTime());
		setEditDate(getCreationDate());
		
	}
	
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
		setEditDate(new DateTime());
		
		if (state != State.TODO)
			setCompletionDate(getEditDate());
		else
			setCompletionDate(null);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public DateTime getEditDate() {
		return editDate;
	}

	public void setEditDate(DateTime editDate) {
		this.editDate = editDate;
	}

	public DateTime getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(DateTime completionDate) {
		this.completionDate = completionDate;
	}
	
	public DateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(DateTime creationDate) {
		this.creationDate = creationDate;
	}

}
