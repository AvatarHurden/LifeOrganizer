package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.util.Callback;

/**
 * <p>This class extends SimpleListProperty, and is made to allow for a List to be easily saved and read from a file.
 * 
 * <p>It also allows for sorting of its members, along with methods to facilitate filtering and getting the first or last member
 * that matches a specific criterion.
 * 
 * <p>There are autosave methods, based both on time delays and on number of modifications to the list or its objects.
 * 
 * @author Arthur
 * @date 18/04/2015
 * @param <T> 
 */
public class StoredList<T extends Comparable<T> & Property<T>> extends SimpleListProperty<T> {

	private File file;
	private boolean sorted;
	private Callback<T, String> encoder;
	private Callback<String, T> decoder;
	
	private Thread saveThread;
	private int editsToSave = 0, edits = 0;
	
	public StoredList(File file, boolean sorted, Callback<T, String> encoder, Callback<String, T> decoder) {
		super(FXCollections.observableArrayList());
		this.file = file;
		this.sorted = sorted;
		this.encoder = encoder;
		this.decoder = decoder;
		
		try {
			load();
		} catch (IOException e) {}
	}
	
	/**
	 * Sets the file that this storedList refers to. If <code>saveCurrrent</code>, the content of the list is saved to the new file.
	 * If not, the list is cleared and the content of the new file is loaded.
	 *  
	 * @param file
	 * @param saveCurrent 
	 * Whether to save the content of the list to the new file. If true, the file is overridden. If not, the content of the
	 * file is loaded into the list.
	 * @throws IOException If the file cannot be opened or saved to.
	 */
	public void setFile(File file, boolean saveCurrent) throws IOException {
		this.file = file;
		if (saveCurrent)
			save();
		else
			load();
	}
	
	public void load() throws IOException {
		clear();
		if (!file.exists())
			file.createNewFile();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null)
			add(decoder.call(line));
		reader.close();
	}
	
	public void save() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (T obj : this) {
			String line = encoder.call(obj);
			writer.write(line);
			if (!line.endsWith("\n"))
				writer.newLine();
		}
		writer.close();
	}
	
	/**
	 * <p>Sets the interval with which to save the content of the list to file. This creates a new Thread that loops continually and can
	 * only be interrupted by setting a new interval that is less than or equal to 0 or by calling the <code>close</code> method on this
	 * list.
	 * 
	 * @param millis The interval with which to save, in milliseconds.
	 * If negative or zero, autosaving is disabled. 
	 */
	public void setSaveInterval(long millis) {
		if (saveThread != null)
			saveThread.interrupt();
		if (millis <= 0)
			return;
		
		saveThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(millis);
						save();
					}
				} catch (InterruptedException | IOException e) {}
			}
		});
		saveThread.start();
	}
	
	/**
	 * <p>Set the amount of changes that are required on the list (or its content) to cause a save. Every time an object in the list fires
	 * a change event, the change count increases. When it reaches the amount specified, the <code>save</code> method is called. 
	 * <p>To disable this functionality, set the amount to 0 or less.
	 * 
	 * @param edits The amount of changes necessary to force a save. If negative or 0, autosave is disabled.
	 */
	public void setSaveOnEdits(int edits) {
		editsToSave = edits;
	}
	
	public boolean add(T s) {
		addListener(s);
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
	
	private void addListener(T object) {
		object.addListener(event -> {
			edits++;
			if (editsToSave > 0 && edits >= editsToSave)
				try {
					save();
					edits = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
		});
	}
	
	/**
	 * Gets the first element that makes the function true
	 * 
	 * @param func A function that takes an instance of T and returns a boolean
	 * @return the instance or null
	 */
	public T getFirst(Callback<T, Boolean> func) {
		for (T item : this)
			if (func.call(item))
				return item;
		
		return null;
	}
	
	/**
	 * Gets the last element that makes the function true
	 * 
	 * @param func A function that takes an instance of T and returns a boolean
	 * @return the instance or null
	 */
	public T getLast(Callback<T, Boolean> func) {
		for (int i = size() - 1; i >= 0; i--)
			if (func.call(get(i)))
				return get(i);
		
		return null;
	}
	
	/**
	 * Returns all elements that make the function true
	 * 
	 * @param func A function that takes an instance of T and returns a boolean
	 * @return a List of instances of T
	 */
	public List<T> filter(Callback<T, Boolean> func) {
		List<T> list = new ArrayList<T>();
		
		for (T item : this)
			if (func.call(item))
				list.add(item);
		
		return list;
	}

	/**
	 * Saves the content of the file and stops the time based autosave, if present. 
	 */
	public void close() {
		try {
			save();
		} catch (IOException e) {}
		finally {
			if (saveThread != null)
				saveThread.interrupt();
		}
	}
}
