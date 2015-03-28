package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StoredList<T extends Comparable<T>> extends ArrayList<T> {

	private File file;
	private boolean sorted;
	private Encoder<T> encoder;
	private Decoder<T> decoder;
	
	public StoredList(File file, boolean sorted, Encoder<T> encoder, Decoder<T> decoder) {
		this.file = file;
		this.sorted = sorted;
		this.encoder = encoder;
		this.decoder = decoder;
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
			add(decoder.decode(line));
		reader.close();
	}
	
	public void save() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (T obj : this) {
			String line = encoder.encode(obj);
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
	 * Gets the first element that makes the function true
	 * 
	 * @param func A function that takes an instance of <class>T</class> and returns a boolean
	 * @return the instance or null
	 */
	public T getFirst(Test<T> func) {
		for (T item : this)
			if (func.test(item))
				return item;
		
		return null;
	}
	
	/**
	 * Gets the last element that makes the function true
	 * 
		 */
	public T getLast(Test<T> func) {
		for (int i = size() - 1; i >= 0; i--)
			if (func.test(get(i)))
				return get(i);
		
		return null;
	}
	
	/**
	 * Returns all elements that make the function true
	 * 
	 * @param func A function that takes an instance of <class>T</class> and returns a boolean
	 * @return a List of instances of <class>T</class>
	 */
	public List<T> filter(Test<T> func) {
		List<T> list = new ArrayList<T>();
		
		for (T item : this)
			if (func.test(item))
				list.add(item);
		
		return list;
	}
	
	public interface Test<T> {
		boolean test(T a);
	}
	
	public interface Decoder<T> {
		T decode(String s);
	}
	
	public interface Encoder<T> {
		String encode(T t);
	}
}
