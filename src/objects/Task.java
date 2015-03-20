package objects;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Task {

	public enum State {
		TODO, DONE, FAILED;
	}
	
	private State state;
	private String name;
	
	private char priority;
	
	private List<String> projects;
	private List<String> contexts;
	
	private String note;
	
	private DateTime dueDate;
	
	private DateTime creationDate;
	private DateTime editDate;
	private DateTime completionDate;
	
	public Task() {
		
		projects = new ArrayList<String>();
		contexts = new ArrayList<String>();
		
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
	
	public void setPriority(char priority) {
		this.priority = Character.toUpperCase(priority);
		setEditDate();
	}
	
	public char getPriority() {
		return priority;
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
	
	public static Task decode(String s) {
		Task t = new Task();

		// Defining state
		Pattern stateP = Pattern.compile("^\\[(x| |-)\\] ");
		Matcher stateM = stateP.matcher(s);
		stateM.find();
		
		switch (stateM.group().charAt(1)) {
		case 'x':
			t.state = Task.State.DONE;
			break;
		case ' ':
			t.state = Task.State.TODO;
			break;
		case '-':
			t.state = Task.State.FAILED;
		}
		
		// Defining completion date
		Pattern doneP = Pattern.compile("DONE=(\\S*) ");
		Matcher doneM = doneP.matcher(s);	
		if (doneM.find())
			t.completionDate = convertTime(doneM.group(1));

		// Defining priority
		Pattern priP = Pattern.compile("\\(([A-Z])\\) ");
		Matcher priM = priP.matcher(s);
		if (priM.find())
			t.priority = priM.group(1).charAt(0);
		
		// Defining due date
		Pattern dueP = Pattern.compile("DUE=(\\S*) ");
		Matcher dueM = dueP.matcher(s);	
		if (dueM.find())
			t.dueDate = convertTime(dueM.group(1));
		
		// Defining note
		Pattern noteP = Pattern.compile("NOTE=\"(.*)\"");
		Matcher noteM = noteP.matcher(s);
		if (noteM.find())
			t.note = noteM.group(1);
		
		// Defining projects
		Pattern projP = Pattern.compile("PROJS=(\\S*) ");
		Matcher projM = projP.matcher(s);
		if (projM.find())
			for (String proj : projM.group(1).split(","))
				t.projects.add(proj);
		
		// Defining contexts
		Pattern contP = Pattern.compile("CONTEXTS=(\\S*) ");
		Matcher contM = contP.matcher(s);
		if (contM.find())
			for (String cont : contM.group(1).split(","))
				t.contexts.add(cont);
		
		// Defining creationDate
		Pattern madeP = Pattern.compile("MADE=(\\S*) ");
		Matcher madeM = madeP.matcher(s);	
		madeM.find();
		t.creationDate = convertTime(madeM.group(1));
		
		// Defining name
		// Accepts things between unescaped quotes (NAME="Read \"this book\" now")
		Pattern nameP = Pattern.compile("NAME=\"((?:\\\\.|[^\"\\\\])*)\"");
		Matcher nameM = nameP.matcher(s);
		nameM.find();
		t.name = nameM.group(1).replace("\\\"", "\"");
		
		// Defining edtDate
		Pattern editP = Pattern.compile("EDIT=(\\S*)[\n]?$");
		Matcher editM = editP.matcher(s);	
		editM.find();
		t.editDate = convertTime(editM.group(1));

		return t;
	}
	
	public String encode() {
	 	ArrayList<String> parts = new ArrayList<String>();
	 	
	 	// Adding state
	 	switch (state) {
		case TODO:
			parts.add("[ ]");
			break;
		case DONE:
			parts.add("[x]");
			break;
		case FAILED:
			parts.add("[-]");	
		}
		
		if (completionDate != null)
			parts.add("DONE=" + formatTime(completionDate));
		
		// Adding priority
		if (priority != 0)
			parts.add(String.format("(%c)", priority));
		
		if (dueDate != null)
			parts.add("DUE=" + formatTime(dueDate));
	
		parts.add("MADE=" + formatTime(creationDate));
		
		parts.add("NAME=\"" + name.replace("\"", "\\\"") + "\"");
		
		if (!projects.isEmpty())
			parts.add("PROJS=" + String.join(",", projects));
			
		if (!contexts.isEmpty())
			parts.add("CONTEXTS=" + String.join(",", contexts));
	
		if (note != null && note != "")
			parts.add("NOTE=\"" + note + "\"");
			
		parts.add("EDIT=" + formatTime(editDate));
	
		return String.join(" ", parts);
	}
	
	private static DateTime convertTime(String t) {
		DateTimeFormatter timeFormat = DateTimeFormat.forPattern("YYYY.MM.dd@HH:mm");
		DateTimeFormatter dateFormat = DateTimeFormat.forPattern("YYYY.MM.dd");
		
		try {
			return timeFormat.parseDateTime(t);
		} catch (Exception e) {
			DateTime date = dateFormat.parseDateTime(t);
			// This is to make the task be overdue only after the day is over, and the
			// millis are just in case a user actually sets the same time.
			return date.plusHours(23).plusMinutes(59).plusMillis(666);
		}
	}
	
	private static String formatTime(DateTime time) {
		DateTimeFormatter timeFormat = DateTimeFormat.forPattern("YYYY.MM.dd@HH:mm");
		DateTimeFormatter dateFormat = DateTimeFormat.forPattern("YYYY.MM.dd");
		
		if (time.getHourOfDay() == 23 && time.getMinuteOfHour() == 59 
				&& time.getMillisOfSecond() == 666)
			return time.toString(dateFormat);
		else
			return time.toString(timeFormat);
	}
}
