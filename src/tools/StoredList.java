package tools;

import java.io.File;
import java.util.ArrayList;

public class StoredList<T> extends ArrayList<T> {

	private File file;
	
	public StoredList(File file, boolean sorted) {
		this.file = file;
	}
	
}
