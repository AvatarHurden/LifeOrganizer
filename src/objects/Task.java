package objects;

import java.util.ArrayList;
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
	
	public void setState(State state) {
		this.state = state;
		setEditDate();
		
		if (state != State.TODO)
			setCompletionDate(getEditDate());
		else
			setCompletionDate(null);
	}
	
	private void setEditDate() {
		this.editDate = new DateTime();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		setEditDate();
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
	
	public State getState() {
		return state;
	}

	public void addProject(String string) {
		if (projects == null)
			projects = new ArrayList<String>();
		if (!string.startsWith("+"))
			string = "+" + string;
		projects.add(string);
		setEditDate();
	}
	
	public void removeProject(String string) {
		if (!string.startsWith("+"))
			string = "+" + string;
		projects.remove(string);
	}
	
	public List<String> getProjects() {
		return projects;
	}
	
	public void addContext(String string) {
		if (contexts == null)
			contexts = new ArrayList<String>();
		if (!string.startsWith("@"))
			string = "@" + string;
		contexts.add(string);
		setEditDate();
	}

	public void removeContext(String string) {
		if (!string.startsWith("@"))
			string = "@" + string;
		contexts.remove(string);
	}

	
	public List<String> getContexts() {
		return contexts;
	}
	
	public void setNote(String string) {
		this.note = string;
		setEditDate();
	}
	
	public String getNote() {
		return note;
	}
	
	public void setDueDate(DateTime due) {
		this.dueDate = due;
		setEditDate();
	}
	
	public DateTime getDueDate() {
		return dueDate;
	}
}
