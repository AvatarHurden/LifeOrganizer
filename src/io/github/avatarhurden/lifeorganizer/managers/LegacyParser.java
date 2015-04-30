package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.DueDate;
import io.github.avatarhurden.lifeorganizer.objects.Task;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.tools.DateUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyParser {

	public static void convert() throws FileNotFoundException {
		TaskManager manager = new TaskManager();
		
		File todo = getTodoFile();
		File done = getDoneFile();
		
		BufferedReader reader = new BufferedReader(new FileReader(todo));
		while (true) {
			try {
				String line = reader.readLine();
				if (line == null)
					break;
				Task task = decode(manager, line, true);
				task.setArchived(false);
			} catch (Exception e ) {
				e.printStackTrace();
			}
		}
		try {
			reader.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		reader = new BufferedReader(new FileReader(done));
		while (true) {
			try {
				String line = reader.readLine();
				if (line == null)
					break;
				Task task = decode(manager, line, false);
				manager.moveToArchive(task);
			} catch (Exception e ) {}
		}
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	private static File getTodoFile() {
		String path = Config.get().getPropertyAndSave("todo_file", "todo.txt");
		if (!new File(path).isAbsolute()) {
			String folder = Config.get().getProperty("default_folder");
			path = new File(folder, path).getAbsolutePath();
		}
		return new File(path);
	}
	
	private static File getDoneFile() {
		String path = Config.get().getPropertyAndSave("done_file", "done.txt");
		if (!new File(path).isAbsolute()) {
			String folder = Config.get().getProperty("default_folder");
			path = new File(folder, path).getAbsolutePath();
		}
		return new File(path);
	}
	

	private static Task decode(TaskManager manager, String s, boolean active) {
		Task t = Task.createNew(manager);

		// Defining state
		Pattern stateP = Pattern.compile("^\\[(x| |-)\\] ");
		Matcher stateM = stateP.matcher(s);
		stateM.find();
		
		switch (stateM.group().charAt(1)) {
		case 'x':
			t.setStateValue(Task.State.DONE);
			break;
		case ' ':
			t.setStateValue(Task.State.TODO);
			break;
		case '-':
			t.setStateValue(Task.State.FAILED);
		}
		
		// Defining completion date
		Pattern doneP = Pattern.compile("DONE=(\\S*) ");
		Matcher doneM = doneP.matcher(s);	
		if (doneM.find())
			t.setCompletionDateValue(DateUtils.parseDateTime(doneM.group(1), "yyyy.MM.dd@HH:mm"));

		// Defining priority
		Pattern priP = Pattern.compile("\\(([A-Z])\\) ");
		Matcher priM = priP.matcher(s);
		if (priM.find())
			t.setPriorityValue(priM.group(1).charAt(0));
		
		// Defining due date
		Pattern dueP = Pattern.compile("DUE=(\\S*) ");
		Matcher dueM = dueP.matcher(s);	
		if (dueM.find())
			t.setDueDateValue(new DueDate(DateUtils.parseDateTime(
					dueM.group(1), "yyyy.MM.dd@HH:mm", "yyyy.MM.dd"), dueM.group(1).contains("@")));
		
		// Defining projects
		Pattern projP = Pattern.compile("PROJS=(\\S*) ");
		Matcher projM = projP.matcher(s);
		if (projM.find())
			for (String proj : projM.group(1).split(","))
				t.addProject(proj);
		
		// Defining contexts
		Pattern contP = Pattern.compile("CONTEXTS=(\\S*) ");
		Matcher contM = contP.matcher(s);
		if (contM.find())
			for (String cont : contM.group(1).split(","))
				t.addContext(cont);
		
		// Defining creationDate
		Pattern madeP = Pattern.compile("MADE=(\\S*) ");
		Matcher madeM = madeP.matcher(s);	
		madeM.find();
		t.setCreationDateValue(DateUtils.parseDateTime(madeM.group(1), "yyyy.MM.dd@HH:mm"));
		
		// Defining name
		// Accepts things between unescaped quotes (NAME="Read \"this book\" now")
		Pattern nameP = Pattern.compile("NAME=\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");
		Matcher nameM = nameP.matcher(s);
		nameM.find();
		t.setNameValue(nameM.group(1).replace("\\\"", "\""));
		
		// Defining note
		Pattern noteP = Pattern.compile("NOTE=\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");
		Matcher noteM = noteP.matcher(s);
		if (noteM.find())
			t.setNoteValue(noteM.group(1).replace("\\\\n", "\n").replace("\\\"", "\""));
				
		// Defining edtDate
		Pattern editP = Pattern.compile("EDIT=(\\S*)[\n]?$");
		Matcher editM = editP.matcher(s);	
		editM.find();
		t.setEditDateValue(DateUtils.parseDateTime(editM.group(1), "yyyy.MM.dd@HH:mm"));

		return t;
	}

	
}
