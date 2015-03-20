package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public abstract class StoredList<T extends Comparable<T>> extends ArrayList<T> {

	private File file;
	private boolean sorted;
	
	public StoredList(File file, boolean sorted) {
		this.file = file;
		this.sorted = sorted;
		try {
			load();
		} catch (IOException e) {}
	}
	
	public void load() throws IOException {		
		if (!file.exists())
			file.createNewFile();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null)
			add(decode(line));
		reader.close();
	}
	
	public void save() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (T obj : this) {
			String line = encode(obj);
			writer.write(line);
			if (!line.endsWith("\n"))
				writer.newLine();
		}
		writer.close();
	}
	
	public boolean add(T s) {
		if (!sorted) {
			super.add(s);
			return true;
		}
		
		int i = 0;
		while (i < size() && get(i).compareTo(s) <= 0)
			i++;
		add(i, s);
		return true;
	}
	
	/**
	 * Takes a single line of the file (without the line termination) and converts it into a instance of <class>T</class>
	 * @param line
	 */
	abstract T decode(String line);
	/**
	 * Convert an instance of <class>T</class> into a line representation (without the line termination)
	 * @param object
	 */
	abstract String encode(T object);
	
	
}
